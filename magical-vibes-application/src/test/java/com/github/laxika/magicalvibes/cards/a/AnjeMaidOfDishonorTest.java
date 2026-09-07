package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VampireOfTheDireMoon;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AnjeMaidOfDishonor.class, VampireOfTheDireMoon.class, GrizzlyBears.class})
class AnjeMaidOfDishonorTest extends BaseCardTest {

    @Test
    @DisplayName("Anje entering creates a Blood token")
    void selfEntryCreatesBloodToken() {
        harness.setHand(player1, List.of(new AnjeMaidOfDishonor()));
        addAnjeMana();

        harness.castCreature(player1, 0);
        resolveAllStack();

        assertThat(countPermanents(player1, "Blood")).isEqualTo(1);
    }

    @Test
    @DisplayName("A Vampire entering under your control creates a Blood token")
    void vampireEntryCreatesBloodToken() {
        addReadyAnje();
        harness.setHand(player1, List.of(new VampireOfTheDireMoon()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        resolveAllStack();

        assertThat(countPermanents(player1, "Blood")).isEqualTo(1);
    }

    @Test
    @DisplayName("Anje's Vampire trigger fires only once each turn")
    void vampireTriggerFiresOnlyOnceEachTurn() {
        addReadyAnje();
        harness.setHand(player1, List.of(new VampireOfTheDireMoon(), new VampireOfTheDireMoon()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        resolveAllStack();
        harness.castCreature(player1, 0);
        resolveAllStack();

        assertThat(countPermanents(player1, "Blood")).isEqualTo(1);
    }

    @Test
    @DisplayName("A non-Vampire entering does not trigger Anje")
    void nonVampireEntryDoesNotCreateBloodToken() {
        addReadyAnje();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        resolveAllStack();

        assertThat(countPermanents(player1, "Blood")).isZero();
    }

    @Test
    @DisplayName("Anje's ability sacrifices a creature and drains each opponent")
    void abilitySacrificesCreatureAndDrains() {
        addReadyAnje();
        harness.addToBattlefield(player1, new GrizzlyBears());
        activateAbilityWithSacrifice();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Anje's ability sacrifices a Blood token")
    void abilitySacrificesBloodToken() {
        addReadyAnje();
        addBloodToken(player1);
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

    private Permanent addReadyAnje() {
        Permanent anje = new Permanent(new AnjeMaidOfDishonor());
        anje.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(anje);
        return anje;
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
        harness.passBothPriorities();
    }

    private void resolveAllStack() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }

    private void addBloodToken(com.github.laxika.magicalvibes.model.Player player) {
        Card bloodCard = new Card();
        bloodCard.setName("Blood");
        bloodCard.setType(CardType.ARTIFACT);
        bloodCard.setManaCost("");
        bloodCard.setToken(true);
        bloodCard.setSubtypes(List.of(CardSubtype.BLOOD));
        bloodCard.addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new DiscardCardTypeCost(null, null), new SacrificeSelfCost(), new DrawCardEffect()),
                "{1}, {T}, Discard a card, Sacrifice this token: Draw a card."
        ));
        Permanent blood = new Permanent(bloodCard);
        blood.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(blood);
    }
}
