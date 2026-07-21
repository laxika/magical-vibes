package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TormentOfVenomTest extends BaseCardTest {

    private static final String LOSE_LIFE = "Lose 3 life";

    @Test
    @DisplayName("Small target creature dies to the three -1/-1 counters and its controller loses 3 life")
    void smallCreatureDiesAndControllerLosesLife() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);

        castTormentOfVenom(bearsId);

        // 2/2 with three -1/-1 counters → -1/-1, dies to the trailing SBA.
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        // Only option was to lose life (the countered creature is excluded, empty hand) — no prompt.
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Surviving target creature keeps its three counters; with no other option its controller loses 3 life")
    void survivingTargetLeavesControllerOnlyLifeLoss() {
        Permanent target = addSurvivor(player2);
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);

        castTormentOfVenom(target.getId());

        // The targeted creature can't be sacrificed ("another nonland permanent") and the hand is
        // empty, so the controller loses 3 life with no prompt.
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Controller may sacrifice another nonland permanent instead of losing life")
    void maySacrificeAnotherNonlandPermanent() {
        Permanent target = addSurvivor(player2);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);

        castTormentOfVenom(target.getId());

        harness.handleListChoice(player2, ChoiceContext.TormentPenaltyChoice.SACRIFICE);
        harness.handlePermanentChosen(player2, bearsId);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        // The targeted creature is untouched by the sacrifice and still bears its counters.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Controller may discard a card instead of losing life")
    void mayDiscardACard() {
        Permanent target = addSurvivor(player2);
        harness.setHand(player2, List.of(new Forest()));
        harness.setLife(player2, 20);

        castTormentOfVenom(target.getId());

        harness.handleListChoice(player2, ChoiceContext.TormentPenaltyChoice.DISCARD);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Controller may choose to lose 3 life even with a permanent and a card available")
    void mayChooseToLoseLife() {
        Permanent target = addSurvivor(player2);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Forest()));
        harness.setLife(player2, 20);

        castTormentOfVenom(target.getId());

        harness.handleListChoice(player2, LOSE_LIFE);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player2, new Forest());
        UUID forestId = harness.getPermanentId(player2, "Forest");
        harness.setHand(player1, List.of(new TormentOfVenom()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forestId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    /** Air Elemental (4/4) survives three -1/-1 counters as a 1/1, so the punisher can be exercised. */
    private Permanent addSurvivor(Player owner) {
        harness.addToBattlefield(owner, new AirElemental());
        UUID id = harness.getPermanentId(owner, "Air Elemental");
        return gd.playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }

    private void castTormentOfVenom(UUID targetId) {
        harness.setHand(player1, List.of(new TormentOfVenom()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
