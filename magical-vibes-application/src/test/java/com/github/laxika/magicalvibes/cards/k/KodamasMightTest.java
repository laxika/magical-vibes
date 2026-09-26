package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GlacialRay;
import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KodamasMight.class, WanderingOnes.class, GlacialRay.class, CounselOfTheSoratami.class})
class KodamasMightTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature +2/+2 until end of turn")
    void boostsTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WanderingOnes());
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new KodamasMight()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getEffectivePower()).isEqualTo(3);
        assertThat(creature.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at cleanup step")
    void boostWearsOffAtCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WanderingOnes());
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new KodamasMight()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(1);
        assertThat(creature.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Splices onto an Arcane spell and stays in hand")
    void splicesOntoArcaneSpell() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WanderingOnes());
        GlacialRay arcaneSpell = new GlacialRay();
        KodamasMight might = new KodamasMight();
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(arcaneSpell, might));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithSplice(player1, 0, creature.getId(), List.of(1));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(creature.getEffectivePower()).isEqualTo(3);
        assertThat(creature.getEffectiveToughness()).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(might);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new KodamasMight()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot splice onto a non-Arcane spell")
    void cannotSpliceOntoNonArcaneSpell() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new CounselOfTheSoratami(), new KodamasMight()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, null, List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be spliced");
    }
}
