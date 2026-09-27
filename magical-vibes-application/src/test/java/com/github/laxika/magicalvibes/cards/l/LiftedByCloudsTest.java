package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DevotedRetainer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GlacialRay;
import com.github.laxika.magicalvibes.cards.h.Hinder;
import com.github.laxika.magicalvibes.cards.m.MossKami;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({LiftedByClouds.class, MossKami.class, Forest.class, GlacialRay.class,
        Hinder.class, DevotedRetainer.class})
class LiftedByCloudsTest extends BaseCardTest {

    @Test
    @DisplayName("Grants flying to target creature until end of turn")
    void grantsFlyingUntilEndOfTurn() {
        Permanent mossKami = harness.addToBattlefieldAndReturn(player1, new MossKami());
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new LiftedByClouds()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, mossKami.getId());

        assertThat(gqs.hasKeyword(gd, mossKami, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, mossKami, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Can target a creature an opponent controls")
    void canTargetOpponentCreature() {
        Permanent mossKami = harness.addToBattlefieldAndReturn(player2, new MossKami());
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new LiftedByClouds()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, mossKami.getId());

        assertThat(gqs.hasKeyword(gd, mossKami, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new LiftedByClouds()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot splice onto a non-Arcane spell")
    void rejectsNonArcaneHost() {
        DevotedRetainer retainer = new DevotedRetainer();
        Hinder hinder = new Hinder();
        LiftedByClouds lifted = new LiftedByClouds();
        harness.setHand(player2, List.of(retainer));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.setHand(player1, List.of(hinder, lifted));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, retainer.getId(), List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be spliced");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(hinder, lifted);
    }

    @Test
    @DisplayName("Splices onto an Arcane spell and stays in hand")
    void splicesOntoArcaneSpell() {
        Permanent mossKami = harness.addToBattlefieldAndReturn(player1, new MossKami());
        GlacialRay arcaneSpell = new GlacialRay();
        LiftedByClouds lifted = new LiftedByClouds();
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(arcaneSpell, lifted));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithSplice(player1, 0, mossKami.getId(), List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, mossKami, Keyword.FLYING)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lifted);
    }
}
