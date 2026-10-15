package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForceOfVirtue.class, GrizzlyBears.class, SuntailHawk.class, Opalescence.class})
class ForceOfVirtueTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control get +1/+1, but opponents' creatures do not")
    void boostsOnlyOwnCreatures() {
        harness.addToBattlefield(player1, new ForceOfVirtue());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can be cast during an opponent's turn by exiling a white card")
    void castsWithWhiteCardAlternateCostDuringOpponentsTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ForceOfVirtue(), new SuntailHawk()));
        harness.castInstantWithAlternateExileFromHand(player1, 0, (UUID) null, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName())
                .containsExactly("Suntail Hawk");
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
    }

    @Test
    @DisplayName("The alternate cost is unavailable during your own turn")
    void alternateCostUnavailableDuringOwnTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ForceOfVirtue(), new SuntailHawk()));
        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(player1, 0, (UUID) null, 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flash permits paying the normal mana cost during an opponent's upkeep")
    void castsForManaDuringOpponentsUpkeep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new ForceOfVirtue(), "{2}{W}{W}");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Force of Virtue")).isEqualTo(1);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("The normal mana cost remains available during your own turn")
    void castsForManaDuringOwnTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new ForceOfVirtue(), "{2}{W}{W}");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Force of Virtue")).isEqualTo(1);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("A nonwhite card cannot pay the alternate cost")
    void rejectsNonwhiteExileCard() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ForceOfVirtue(), new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(player1, 0, (UUID) null, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(countPermanents(player1, "Force of Virtue")).isZero();
    }

    @Test
    @DisplayName("Force of Virtue cannot exile itself to pay its alternate cost")
    void cannotExileSpellItself() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ForceOfVirtue()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(player1, 0, (UUID) null, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Another Force of Virtue can pay the alternate cost even before the spell's hand index")
    void exilesAnotherCopyBeforeSpellIndex() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        ForceOfVirtue payment = new ForceOfVirtue();
        harness.setHand(player1, List.of(payment, new ForceOfVirtue()));

        harness.castInstantWithAlternateExileFromHand(player1, 1, (UUID) null, 0);
        assertThat(gd.exiledCards).extracting(entry -> entry.card()).containsExactly(payment);
        assertThat(countPermanents(player1, "Force of Virtue")).isZero();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Force of Virtue")).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Multiple copies boost creatures entering later")
    void copiesStackForLaterCreatures() {
        harness.addToBattlefield(player1, new ForceOfVirtue());
        harness.addToBattlefield(player1, new ForceOfVirtue());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @CardUsed({ForceOfVirtue.class, Opalescence.class})
    @DisplayName("Force of Virtue boosts itself when Opalescence makes it a creature")
    void boostsItselfWhenAnimated() {
        Permanent force = harness.addToBattlefieldAndReturn(player1, new ForceOfVirtue());
        harness.addToBattlefield(player1, new Opalescence());

        assertThat(gqs.isCreature(gd, force)).isTrue();
        assertThat(gqs.getEffectivePower(gd, force)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, force)).isEqualTo(5);
    }

    @Test
    @DisplayName("The static bonus is removed when Force of Virtue leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        Permanent force = harness.addToBattlefieldAndReturn(player1, new ForceOfVirtue());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(force);

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
    }
}
