package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ElvishFuryTest extends BaseCardTest {

    @Test
    @DisplayName("Elvish Fury gives the target creature +2/+2")
    void boostsTarget() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ElvishFury()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent bears = bears(player1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ElvishFury()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = bears(player1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Without buyback the spell goes to the graveyard")
    void withoutBuybackGoesToGraveyard() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ElvishFury()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(handNames(player1)).isEmpty();
        assertThat(graveyardNames(player1)).containsExactly("Elvish Fury");
    }

    @Test
    @DisplayName("Paying buyback {4} returns the spell to hand and still boosts")
    void buybackReturnsToHand() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ElvishFury()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstantWithBuyback(player1, 0, targetId);
        assertThat(gd.stack.getFirst().isBuyback()).isTrue();

        harness.passBothPriorities();

        assertThat(graveyardNames(player1)).doesNotContain("Elvish Fury");
        assertThat(handNames(player1)).containsExactly("Elvish Fury");
        assertThat(gqs.getEffectivePower(gd, bears(player1))).isEqualTo(4);
    }

    @Test
    @DisplayName("Paying buyback without enough mana rewinds the cast")
    void buybackWithoutManaRewinds() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ElvishFury()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstantWithBuyback(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);

        assertThat(handNames(player1)).containsExactly("Elvish Fury");
    }

    @Test
    @DisplayName("Elvish Fury cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new ElvishFury()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent bears(Player player) {
        return findPermanent(player, "Grizzly Bears");
    }

    private List<String> handNames(Player player) {
        return gd.playerHands.get(player.getId()).stream().map(c -> c.getName()).toList();
    }

    private List<String> graveyardNames(Player player) {
        return gd.playerGraveyards.get(player.getId()).stream().map(c -> c.getName()).toList();
    }
}
