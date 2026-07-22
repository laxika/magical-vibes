package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CaptivatingVampire;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SorinImperiousBloodlordTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving puts planeswalker on battlefield with 4 loyalty")
    void resolvingEntersBattlefieldWithLoyalty() {
        harness.setHand(player1, List.of(new SorinImperiousBloodlord()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        Permanent sorin = findPermanent(player1, "Sorin, Imperious Bloodlord");
        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    // ===== +1: keywords + Vampire counter =====

    @Test
    @DisplayName("+1 grants deathtouch and lifelink to a controlled creature")
    void plusOneGrantsKeywords() {
        Permanent sorin = addReadySorin(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("+1 puts a +1/+1 counter when the target is a Vampire")
    void plusOnePutsCounterOnVampire() {
        Permanent sorin = addReadySorin(player1);
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new CaptivatingVampire());

        harness.activateAbility(player1, 0, 0, null, vampire.getId());
        harness.passBothPriorities();

        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, vampire, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, vampire, Keyword.LIFELINK)).isTrue();
        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("+1 cannot target an opponent's creature")
    void plusOneRejectsOpponentCreature() {
        addReadySorin(player1);
        Permanent opp = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opp.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== +1: may sacrifice Vampire → 3 damage + 3 life =====

    @Test
    @DisplayName("+1 sacrifice Vampire deals 3 damage and gains 3 life")
    void plusOneSacrificeDealsDamageAndGainsLife() {
        Permanent sorin = addReadySorin(player1);
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new CaptivatingVampire());
        int lifeBefore = gd.getLife(player1.getId());
        int oppLifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, vampire.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Captivating Vampire"));
        assertThat(gd.getLife(player2.getId())).isEqualTo(oppLifeBefore - 3);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("+1 declining sacrifice deals no damage and gains no life")
    void plusOneDeclineSacrificeDoesNothing() {
        addReadySorin(player1);
        harness.addToBattlefield(player1, new CaptivatingVampire());
        int lifeBefore = gd.getLife(player1.getId());
        int oppLifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(oppLifeBefore);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Captivating Vampire"));
    }

    @Test
    @DisplayName("+1 sacrifice only offers Vampires")
    void plusOneSacrificeOnlyOffersVampires() {
        addReadySorin(player1);
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new CaptivatingVampire());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactly(vampire.getId());
    }

    // ===== −3: put Vampire from hand =====

    @Test
    @DisplayName("−3 puts a Vampire creature from hand onto the battlefield")
    void minusThreePutsVampireFromHand() {
        Permanent sorin = addReadySorin(player1);
        sorin.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new CaptivatingVampire()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Captivating Vampire"));
    }

    @Test
    @DisplayName("−3 declining puts nothing from hand")
    void minusThreeDeclinePutsNothing() {
        Permanent sorin = addReadySorin(player1);
        sorin.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new CaptivatingVampire()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("−3 only offers Vampire creature cards")
    void minusThreeOnlyOffersVampireCreatures() {
        Permanent sorin = addReadySorin(player1);
        sorin.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new CaptivatingVampire(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(0);
    }

    private Permanent addReadySorin(Player player) {
        SorinImperiousBloodlord card = new SorinImperiousBloodlord();
        Permanent perm = new Permanent(card);
        perm.setCounterCount(CounterType.LOYALTY, 4);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
