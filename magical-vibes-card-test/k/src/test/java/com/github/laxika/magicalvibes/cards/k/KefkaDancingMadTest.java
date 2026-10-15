package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KefkaDancingMad.class, SolRing.class, Island.class})
class KefkaDancingMadTest extends BaseCardTest {

    @Test
    @DisplayName("Kefka has indestructible during its controller's turn only")
    void indestructibleDuringControllerTurnOnly() {
        Permanent kefka = addCreatureReady(player1, new KefkaDancingMad());
        kefka.setMarkedDamage(6);

        harness.forceActivePlayer(player1);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kefka);

        harness.forceActivePlayer(player2);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(kefka);
    }

    @Test
    @DisplayName("Kefka exiles a random opposing graveyard card and makes its owner lose mana value life")
    void exilesAndCastsOpposingGraveyardCard() {
        addCreatureReady(player1, new KefkaDancingMad());
        SolRing spell = new SolRing();
        spell.setOwnerId(player2.getId());
        harness.setGraveyard(player2, List.of(spell));

        resolveControllerEndStep();

        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));

        assertThat(gd.findExiledCard(spell.getId())).isNull();
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(spell.getId());
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());

        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Sol Ring");
        harness.assertNotOnBattlefield(player2, "Sol Ring");
    }

    @Test
    @DisplayName("Declining a spell leaves it exiled without permission to cast it later")
    void declinedSpellCannotBeCastLater() {
        addCreatureReady(player1, new KefkaDancingMad());
        SolRing spell = new SolRing();
        spell.setOwnerId(player2.getId());
        harness.setGraveyard(player2, List.of(spell));

        resolveControllerEndStep();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
    }

    @Test
    @DisplayName("An opposing land is exiled but cannot be played and causes no life loss")
    void opposingLandIsOnlyExiled() {
        addCreatureReady(player1, new KefkaDancingMad());
        Island land = new Island();
        harness.setGraveyard(player2, List.of(land));

        resolveControllerEndStep();

        assertThat(gd.findExiledCard(land.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An empty opposing graveyard does not exile cards from the controller's graveyard")
    void emptyOpposingGraveyardLeavesOwnGraveyardUntouched() {
        addCreatureReady(player1, new KefkaDancingMad());
        SolRing ownCard = new SolRing();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of());

        resolveControllerEndStep();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownCard);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Only one card is randomly exiled from the opposing graveyard")
    void exilesExactlyOneOfSeveralOpposingCards() {
        addCreatureReady(player1, new KefkaDancingMad());
        Island first = new Island();
        Island second = new Island();
        harness.setGraveyard(player2, List.of(first, second));

        resolveControllerEndStep();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.exiledCards).hasSize(1);
        assertThat(gd.exiledCards.getFirst().card().getId()).isIn(first.getId(), second.getId());
        assertThat(gd.playerGraveyards.get(player2.getId()).getFirst().getId())
                .isNotEqualTo(gd.exiledCards.getFirst().card().getId());
    }

    @Test
    @DisplayName("Kefka does not exile anything at an opponent's end step")
    void doesNotTriggerDuringOpponentsEndStep() {
        addCreatureReady(player1, new KefkaDancingMad());
        SolRing spell = new SolRing();
        harness.setGraveyard(player2, List.of(spell));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.passUntil(player2, TurnStep.END_STEP);
            resolveAllTriggers();
        });

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(spell);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void resolveControllerEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.passUntil(player1, TurnStep.END_STEP);
            resolveAllTriggers();
        });
    }
}
