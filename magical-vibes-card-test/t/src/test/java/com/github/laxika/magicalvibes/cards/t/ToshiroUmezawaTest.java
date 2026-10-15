package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaSpike;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ToshiroUmezawa.class, CruelEdict.class, GrizzlyBears.class, LavaSpike.class, Shock.class})
class ToshiroUmezawaTest extends BaseCardTest {

    /** Player1 edicts away player2's only creature, firing Toshiro's trigger. */
    private void killOpponentCreature() {
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The targeted instant must be offered for casting during the trigger's resolution")
    void offersInstantDuringTriggerResolution() {
        harness.addToBattlefield(player1, new ToshiroUmezawa());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Card shock = new Shock();
        harness.setGraveyard(player1, new ArrayList<>(List.of(shock)));
        harness.addMana(player1, ManaColor.RED, 1);

        killOpponentCreature();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        assertThat(gd.graveyardCardCastPermissionsUntilEndOfTurn).isEmpty();
    }

    @Test
    @DisplayName("Only instant cards in the controller's own graveyard are legal targets")
    void onlyOwnInstantsAreTargetable() {
        harness.addToBattlefield(player1, new ToshiroUmezawa());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Card ownInstant = new Shock();
        Card ownSorcery = new LavaSpike();
        Card opponentInstant = new Shock();
        harness.setGraveyard(player1, new ArrayList<>(List.of(ownInstant, ownSorcery)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(opponentInstant)));

        killOpponentCreature();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ownInstant.getId());
    }

    @Test
    @DisplayName("Trigger is skipped when there is no instant card to target")
    void triggerSkippedWithoutLegalTarget() {
        harness.addToBattlefield(player1, new ToshiroUmezawa());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, new ArrayList<>(List.of(new LavaSpike())));

        killOpponentCreature();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The controller's own creature dying does not trigger the ability")
    void doesNotTriggerOnOwnCreatureDeath() {
        harness.addToBattlefield(player1, new ToshiroUmezawa());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, new ArrayList<>(List.of(new Shock())));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.graveyardCardCastPermissionsUntilEndOfTurn).isEmpty();
    }

    @Test
    @DisplayName("Bushido 1 gives Toshiro +1/+1 when it becomes blocked")
    void bushidoOnBecomingBlocked() {
        Permanent toshiro = addCreatureReady(player1, new ToshiroUmezawa());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(toshiro.getPowerModifier()).isEqualTo(1);
        assertThat(toshiro.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Bushido 1 gives Toshiro +1/+1 when it blocks")
    void bushidoOnBlocking() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent toshiro = addCreatureReady(player2, new ToshiroUmezawa());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(toshiro.getPowerModifier()).isEqualTo(1);
        assertThat(toshiro.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("An instant not cast during resolution cannot be cast later in the turn")
    void cannotCastLaterAfterDeclining() {
        harness.addToBattlefield(player1, new ToshiroUmezawa());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Card shock = new Shock();
        harness.setGraveyard(player1, new ArrayList<>(List.of(shock)));
        harness.addMana(player1, ManaColor.RED, 1);

        killOpponentCreature();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, false);
        }

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock);
        assertThat(gd.graveyardCardCastPermissionsUntilEndOfTurn).isEmpty();
    }

    @Test
    @DisplayName("An unblocked attacker does not get a bushido bonus")
    void noBushidoWhenUnblocked() {
        Permanent toshiro = addCreatureReady(player1, new ToshiroUmezawa());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(toshiro.getPowerModifier()).isZero();
        assertThat(toshiro.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Being blocked by two creatures triggers bushido only once")
    void bushidoOnlyOnceForMultipleBlockers() {
        Permanent toshiro = addCreatureReady(player1, new ToshiroUmezawa());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(toshiro.getPowerModifier()).isEqualTo(1);
        assertThat(toshiro.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("A targeted instant that leaves the graveyard before resolution cannot be cast")
    void targetLeavingGraveyardPreventsCast() {
        harness.addToBattlefield(player1, new ToshiroUmezawa());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Card shock = new Shock();
        harness.setGraveyard(player1, new ArrayList<>(List.of(shock)));

        killOpponentCreature();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.setGraveyard(player1, new ArrayList<>());
        harness.setExile(player1, List.of(shock));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.graveyardCardCastPermissionsUntilEndOfTurn).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock);
    }
}
