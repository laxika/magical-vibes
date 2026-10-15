package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GreensideWatcher;
import com.github.laxika.magicalvibes.cards.a.AerialManeuver;
import com.github.laxika.magicalvibes.cards.h.HolyMantle;
import com.github.laxika.magicalvibes.cards.g.GruulKeyrune;
import com.github.laxika.magicalvibes.cards.g.GruulGuildgate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TreasuryThrull.class, GreensideWatcher.class, AerialManeuver.class, HolyMantle.class,
        GruulKeyrune.class, GruulGuildgate.class})
class TreasuryThrullTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking returns a creature card from the graveyard to hand")
    void attackReturnsCreatureCardToHand() {
        harness.setGraveyard(player1, List.of(new GreensideWatcher()));
        addCreatureReady(player1, new TreasuryThrull());

        declareAttackers(List.of(0));
        chooseAttackTarget(0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotInGraveyard(player1, "Greenside Watcher");
        harness.assertInHand(player1, "Greenside Watcher");
    }

    @Test
    @DisplayName("Artifact and enchantment cards are also legal returns")
    void artifactAndEnchantmentAreLegal() {
        harness.setGraveyard(player1, List.of(new GruulKeyrune(), new HolyMantle()));
        addCreatureReady(player1, new TreasuryThrull());

        declareAttackers(List.of(0));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).cards())
                .containsExactlyInAnyOrderElementsOf(gd.playerGraveyards.get(player1.getId()));
        chooseAttackTarget(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Holy Mantle");
    }

    @Test
    @DisplayName("Instants and lands in the graveyard are not legal returns")
    void nonMatchingCardsAreSkipped() {
        harness.setGraveyard(player1, List.of(new AerialManeuver(), new GruulGuildgate()));
        addCreatureReady(player1, new TreasuryThrull());

        declareAttackers(List.of(0));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Aerial Maneuver");
        harness.assertInGraveyard(player1, "Gruul Guildgate");
    }

    @Test
    @DisplayName("Declining the attack trigger leaves the graveyard untouched")
    void decliningLeavesGraveyard() {
        harness.setGraveyard(player1, List.of(new GreensideWatcher()));
        addCreatureReady(player1, new TreasuryThrull());

        declareAttackers(List.of(0));
        chooseAttackTarget(0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Greenside Watcher");
    }

    @Test
    @DisplayName("Extort drains the opponent for 1 when the {W/B} is paid")
    void extortDrainsOpponent() {
        harness.addToBattlefield(player1, new TreasuryThrull());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castFromHand(player1, new GreensideWatcher(), "{1}{G}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Declining extort leaves life totals unchanged")
    void decliningExtortDoesNothing() {
        harness.addToBattlefield(player1, new TreasuryThrull());

        harness.castFromHand(player1, new GreensideWatcher(), "{1}{G}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void attackReturnsNoncreatureArtifactToHand() {
        harness.setGraveyard(player1, List.of(new GruulKeyrune()));
        addCreatureReady(player1, new TreasuryThrull());

        declareAttackers(List.of(0));
        chooseAttackTarget(0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Gruul Keyrune");
        harness.assertNotInGraveyard(player1, "Gruul Keyrune");
    }

    @Test
    void cannotTargetOpponentsGraveyard() {
        harness.setGraveyard(player2, List.of(new GreensideWatcher()));
        addCreatureReady(player1, new TreasuryThrull());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Greenside Watcher");
    }

    @Test
    void cannotSwitchCardsWhenTargetLeavesGraveyard() {
        Card target = new GreensideWatcher();
        Card other = new GruulKeyrune();
        harness.setGraveyard(player1, List.of(target, other));
        addCreatureReady(player1, new TreasuryThrull());

        declareAttackers(List.of(0));
        chooseAttackTarget(0);
        harness.setGraveyard(player1, List.of(other));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Gruul Keyrune");
        harness.assertNotInHand(player1, "Gruul Keyrune");
        harness.assertNotInHand(player1, "Greenside Watcher");
    }

    @Test
    void extortPaymentDecisionWaitsForResolution() {
        harness.addToBattlefield(player1, new TreasuryThrull());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromHand(player1, new GreensideWatcher(), "{1}{G}");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    void opponentSpellDoesNotTriggerExtort() {
        harness.addToBattlefield(player1, new TreasuryThrull());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new GreensideWatcher(), "{1}{G}");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void chooseAttackTarget(int index) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1,
                List.of(gd.playerGraveyards.get(player1.getId()).get(index).getId()));
    }
}
