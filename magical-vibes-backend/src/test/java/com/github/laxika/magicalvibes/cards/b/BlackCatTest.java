package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerRandomDiscardEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BlackCatTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ON_DEATH random-discard trigger targeting an opponent")
    void hasCorrectStructure() {
        BlackCat card = new BlackCat();

        assertThat(card.getEffects(EffectSlot.ON_DEATH)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DEATH).getFirst())
                .isInstanceOf(TargetPlayerRandomDiscardEffect.class);
        TargetPlayerRandomDiscardEffect deathEffect =
                (TargetPlayerRandomDiscardEffect) card.getEffects(EffectSlot.ON_DEATH).getFirst();
        assertThat(deathEffect.amount()).isEqualTo(1);
        assertThat(deathEffect.causedByOpponent()).isTrue();

        assertThat(card.getTargetFilter()).isInstanceOf(PlayerPredicateTargetFilter.class);
        PlayerPredicateTargetFilter filter = (PlayerPredicateTargetFilter) card.getTargetFilter();
        assertThat(filter.predicate()).isInstanceOf(PlayerRelationPredicate.class);
        assertThat(((PlayerRelationPredicate) filter.predicate()).relation()).isEqualTo(PlayerRelation.OPPONENT);
    }

    // ===== Death trigger =====

    @Test
    @DisplayName("When Black Cat dies, targeted opponent discards a card at random")
    void diesForcesOpponentRandomDiscard() {
        harness.addToBattlefield(player1, new BlackCat());

        // Kill Black Cat with Shock (player2 is active)
        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock(), new GrizzlyBears(), new GiantGrowth()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID catId = harness.getPermanentId(player1, "Black Cat");
        harness.castInstant(player2, 0, catId);
        harness.passBothPriorities(); // Shock resolves → cat dies → death trigger awaits target

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        // Only valid target should be the opponent (player2), not player1 (the controller)
        assertThat(gd.interaction.permanentChoice().validIds()).containsExactly(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // Resolve discard trigger

        // Player2 had 2 non-Shock cards in hand after casting Shock; one is discarded at random.
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(c -> !c.getName().equals("Shock"))
                .hasSize(1);
        assertThat(gd.gameLog).anyMatch(log -> log.contains("at random"));
    }

    @Test
    @DisplayName("Death trigger only offers opponents as valid targets")
    void targetFilterExcludesController() {
        harness.setHand(player2, List.of(new GrizzlyBears()));

        harness.addToBattlefield(player1, new BlackCat());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID catId = harness.getPermanentId(player1, "Black Cat");
        harness.castInstant(player2, 0, catId);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.interaction.permanentChoice().validIds())
                .doesNotContain(player1.getId())
                .containsExactly(player2.getId());
    }

    @Test
    @DisplayName("Randomly selects one card from an opponent with multiple cards in hand")
    void discardsOneOfMultipleCards() {
        harness.addToBattlefield(player1, new BlackCat());

        setupPlayer2Active();
        harness.setHand(player2, List.of(
                new Shock(),
                new GrizzlyBears(),
                new GiantGrowth(),
                new LightningBolt()
        ));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID catId = harness.getPermanentId(player1, "Black Cat");
        harness.castInstant(player2, 0, catId);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        // Started with 4 non-Shock cards (Shock was cast), so 3 in hand when trigger resolves,
        // one is discarded at random → 2 left, 1 in graveyard.
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(c -> !c.getName().equals("Shock"))
                .hasSize(1);
    }

    @Test
    @DisplayName("Opponent with empty hand results in no discard")
    void emptyHandDoesNothing() {
        harness.addToBattlefield(player1, new BlackCat());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID catId = harness.getPermanentId(player1, "Black Cat");
        harness.castInstant(player2, 0, catId);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        // Only Shock should be in graveyard, no additional random-discarded card
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(c -> !c.getName().equals("Shock"))
                .isEmpty();
    }

    // ===== Helpers =====

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
