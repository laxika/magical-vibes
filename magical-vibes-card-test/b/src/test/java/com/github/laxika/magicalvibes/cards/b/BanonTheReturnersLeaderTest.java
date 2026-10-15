package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TormentingVoice;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BanonTheReturnersLeader.class, Forest.class, GrizzlyBears.class, Shock.class, TormentingVoice.class})
class BanonTheReturnersLeaderTest extends BaseCardTest {

    @Test
    @DisplayName("casts a creature discarded from hand this turn")
    void castsCreaturePutIntoGraveyardFromNonBattlefield() {
        harness.addToBattlefield(player1, new BanonTheReturnersLeader());
        Card tormentingVoice = new TormentingVoice();
        Card creature = new GrizzlyBears();
        harness.setHand(player1, List.of(tormentingVoice, creature));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();

        int creatureIndex = gd.playerGraveyards.get(player1.getId()).indexOf(creature);
        assertThat(creatureIndex).isGreaterThanOrEqualTo(0);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.clearPriorityPassed();
        harness.castFromGraveyard(player1, creatureIndex);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("does not cast a creature put into the graveyard from the battlefield")
    void rejectsCreaturePutIntoGraveyardFromBattlefield() {
        harness.addToBattlefield(player1, new BanonTheReturnersLeader());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThatThrownBy(() -> harness.castFromGraveyard(player1,
                gd.playerGraveyards.get(player1.getId()).indexOf(creature.getCard())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("may pay and discard to draw when attacking")
    void attackingMayPayToDiscardAndDraw() {
        addCreatureReady(player1, new BanonTheReturnersLeader());
        Card discarded = new GrizzlyBears();
        Card drawn = new Forest();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.handleMayAbilityChosen(player1, true);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
            harness.handleCardChosen(player1, 0);
        });

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
    }

    @Test
    void cannotPayAttackCostWithAnEmptyHand() {
        addCreatureReady(player1, new BanonTheReturnersLeader());
        harness.setHand(player1, List.of());
        Card drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.passBothPriorities();
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
                harness.handleMayAbilityChosen(player1, true);
            }
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
            assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
        });
    }

    @Test
    void mayDeclineAttackCost() {
        addCreatureReady(player1, new BanonTheReturnersLeader());
        Card kept = new GrizzlyBears();
        Card drawn = new Forest();
        harness.setHand(player1, List.of(kept));
        harness.setLibrary(player1, List.of(drawn));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.passBothPriorities();
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.handleMayAbilityChosen(player1, false);
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
            assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
            assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
        });
    }

    @Test
    void cannotDiscardOrDrawWithoutPayingMana() {
        addCreatureReady(player1, new BanonTheReturnersLeader());
        Card kept = new GrizzlyBears();
        Card drawn = new Forest();
        harness.setHand(player1, List.of(kept));
        harness.setLibrary(player1, List.of(drawn));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
            assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
        });
    }

    @Test
    void triggersOnceForMultipleAttackersWithoutBanonAttacking() {
        harness.addToBattlefield(player1, new BanonTheReturnersLeader());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Card discarded = new Forest();
        Card drawn = new Forest();
        Card remaining = new Forest();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn, remaining));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(1, 2));
            assertThat(gd.stack).hasSize(1);
            harness.passBothPriorities();
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.handleMayAbilityChosen(player1, true);
            harness.handleCardChosen(player1, 0);
            assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
            assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
        });
    }

    @Test
    void prayIsLimitedToOneCreatureSpellEachTurn() {
        harness.addToBattlefield(player1, new BanonTheReturnersLeader());
        Card first = discardCreatureWithVoice();
        Card second = discardCreatureWithVoice();
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.castFromGraveyard(player1, first.getId());
            harness.passBothPriorities();
            assertThatThrownBy(() -> harness.castFromGraveyard(player1, second.getId()))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(gd.playerGraveyards.get(player1.getId())).contains(second);
            harness.assertOnBattlefield(player1, "Grizzly Bears");
        });
    }

    @Test
    void prayRequiresNormalCreatureTimingAndMana() {
        harness.addToBattlefield(player1, new BanonTheReturnersLeader());
        Card creature = discardCreatureWithVoice();
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        harness.castFromGraveyard(player1, creature.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void rejectsOldGraveyardCardsAndNoncreatureSpells() {
        harness.addToBattlefield(player1, new BanonTheReturnersLeader());
        Card oldCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(oldCreature));
        discardCreatureWithVoice();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, oldCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        int voiceIndex = gd.playerGraveyards.get(player1.getId()).stream()
                .filter(TormentingVoice.class::isInstance)
                .mapToInt(card -> gd.playerGraveyards.get(player1.getId()).indexOf(card))
                .findFirst().orElseThrow();
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, voiceIndex))
                .isInstanceOf(IllegalStateException.class);
    }

    private Card discardCreatureWithVoice() {
        Card creature = new GrizzlyBears();
        harness.setHand(player1, List.of(new TormentingVoice(), creature));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.castSorceryWithDiscard(player1, 0, 1);
            harness.passBothPriorities();
        });
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
        return creature;
    }
}
