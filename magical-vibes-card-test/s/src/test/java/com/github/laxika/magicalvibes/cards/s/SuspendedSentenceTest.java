package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BurnishedHart;
import com.github.laxika.magicalvibes.cards.v.VilisBrokerOfBlood;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SuspendedSentence.class, BurnishedHart.class, VilisBrokerOfBlood.class})
class SuspendedSentenceTest extends BaseCardTest {

    @Test
    void destroysOpposingCreatureLosesLifeAndExilesWithSuspendCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BurnishedHart());
        SuspendedSentence card = new SuspendedSentence();
        harness.setHand(player1, List.of(card));
        addNormalMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Burnished Hart");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(card.getId(), player1.getId(), 3));
    }

    @Test
    void canOnlyTargetAnOpposingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BurnishedHart());
        harness.setHand(player1, List.of(new SuspendedSentence()));
        addNormalMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    void suspendCastsForFreeAndExilesTheSpellAgain() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BurnishedHart());
        SuspendedSentence card = new SuspendedSentence();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            resolveAllTriggers();
        }
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Burnished Hart");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(card.getId(), player1.getId(), 3));
    }

    @Test
    void destroyingVilisDoesNotTriggerItsLifeLossAbility() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VilisBrokerOfBlood());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new BurnishedHart(), new BurnishedHart(), new BurnishedHart()));
        harness.setHand(player1, List.of(new SuspendedSentence()));
        addNormalMana();

        harness.castAndResolveInstant(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Vilis, Broker of Blood");
        harness.assertLife(player2, 17);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
    }

    @Test
    void illegalTargetPreventsLifeLossAndSelfExile() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BurnishedHart());
        SuspendedSentence card = new SuspendedSentence();
        harness.setHand(player1, List.of(card));
        addNormalMana();

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Suspended Sentence");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
        assertThat(gd.suspendedSpellExiles).isEmpty();
    }

    @Test
    void canSuspendDuringOpponentsCombatWithoutALegalCreatureTarget() {
        SuspendedSentence card = new SuspendedSentence();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.passPriority(player2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertNotInHand(player1, "Suspended Sentence");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canDeclineCastingAfterLastTimeCounterIsRemoved() {
        SuspendedSentence card = new SuspendedSentence();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addToBattlefield(player2, new BurnishedHart());
        harness.activateHandAbility(player1, 0, null);

        advanceToUpkeep(player2);
        resolveAllTriggers();
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            resolveAllTriggers();
        }
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.suspendedSpellExiles).isEmpty();
        harness.assertOnBattlefield(player2, "Burnished Hart");
        harness.assertLife(player2, 20);
    }

    private void addNormalMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
