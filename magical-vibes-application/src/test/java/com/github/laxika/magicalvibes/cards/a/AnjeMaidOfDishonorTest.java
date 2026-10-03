package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BloodPetalCelebrant;
import com.github.laxika.magicalvibes.cards.d.DoomedDissenter;
import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AnjeMaidOfDishonor.class, BloodPetalCelebrant.class, DoomedDissenter.class, Conspiracy.class})
class AnjeMaidOfDishonorTest extends BaseCardTest {

    @Test
    @DisplayName("Anje entering creates a Blood token")
    void selfEntryCreatesBloodToken() {
        harness.setHand(player1, List.of(new AnjeMaidOfDishonor()));
        addAnjeMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Blood")).isEqualTo(1);
    }

    @Test
    @DisplayName("A Vampire entering under your control creates a Blood token")
    void vampireEntryCreatesBloodToken() {
        addReadyAnje();
        harness.setHand(player1, List.of(new BloodPetalCelebrant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Blood")).isEqualTo(1);
    }

    @Test
    @DisplayName("Anje's Vampire trigger fires only once each turn")
    void vampireTriggerFiresOnlyOnceEachTurn() {
        addReadyAnje();
        harness.setHand(player1, List.of(new BloodPetalCelebrant(), new BloodPetalCelebrant()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Blood")).isEqualTo(1);
    }

    @Test
    @DisplayName("A non-Vampire entering does not trigger Anje")
    void nonVampireEntryDoesNotCreateBloodToken() {
        addReadyAnje();
        harness.setHand(player1, List.of(new DoomedDissenter()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Blood")).isZero();
    }

    @Test
    @DisplayName("Anje's ability sacrifices a creature and drains each opponent")
    void abilitySacrificesCreatureAndDrains() {
        addReadyAnje();
        harness.addToBattlefield(player1, new DoomedDissenter());
        activateAbilityWithSacrifice();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        harness.assertInGraveyard(player1, "Doomed Dissenter");
    }

    @Test
    @DisplayName("Anje's ability sacrifices a Blood token")
    void abilitySacrificesBloodToken() {
        harness.setHand(player1, List.of(new AnjeMaidOfDishonor()));
        addAnjeMana();
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        activateAbilityWithSacrifice();

        assertThat(countPermanents(player1, "Blood")).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Anje cannot sacrifice itself as another creature")
    void abilityCannotSacrificeAnjeAlone() {
        addReadyAnje();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A noncreature nontoken Blood permanent cannot pay Anje's cost")
    void cannotSacrificeNontokenBloodPermanent() {
        harness.setHand(player1, List.of(new AnjeMaidOfDishonor()));
        addAnjeMana();
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent blood = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Blood"))
                .findFirst().orElseThrow();
        // Model a nontoken artifact that has copied a Blood token.
        Card nontokenCopy = blood.getCard().createRuntimeCopy();
        nontokenCopy.setToken(false);
        blood.setCard(nontokenCopy);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Blood");
    }

    @Test
    void opponentVampireDoesNotTriggerAnje() {
        addReadyAnje();
        harness.enterBattlefieldAndReturn(player2, new BloodPetalCelebrant());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Blood")).isZero();
    }

    @Test
    void nonVampireDoesNotConsumeOncePerTurnTrigger() {
        addReadyAnje();
        harness.enterBattlefieldAndReturn(player1, new DoomedDissenter());
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player1, new BloodPetalCelebrant());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Blood")).isEqualTo(1);
    }

    @Test
    void triggerResetsOnOpponentsTurn() {
        addReadyAnje();
        harness.enterBattlefieldAndReturn(player1, new BloodPetalCelebrant());
        resolveAllTriggers();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.enterBattlefieldAndReturn(player1, new BloodPetalCelebrant());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Blood")).isEqualTo(2);
    }

    @Test
    void secondEntryBeforeTriggerResolvesDoesNotTriggerAgain() {
        addReadyAnje();
        harness.enterBattlefieldAndReturn(player1, new BloodPetalCelebrant());
        harness.enterBattlefieldAndReturn(player1, new BloodPetalCelebrant());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Blood")).isEqualTo(1);
    }

    @Test
    @DisplayName("Anje's own entry triggers even when Conspiracy makes it a non-Vampire")
    void selfEntryTriggersWhenCreatureTypeIsReplaced() {
        Permanent conspiracy = harness.addToBattlefieldAndReturn(player1, new Conspiracy());
        conspiracy.setChosenSubtype(CardSubtype.GOBLIN);
        harness.setHand(player1, List.of(new AnjeMaidOfDishonor()));
        addAnjeMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Blood")).isEqualTo(1);
    }

    private void addReadyAnje() {
        Permanent anje = harness.addToBattlefieldAndReturn(player1, new AnjeMaidOfDishonor());
        anje.setSummoningSick(false);
    }

    private void addAnjeMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void activateAbilityWithSacrifice() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
    }

}
