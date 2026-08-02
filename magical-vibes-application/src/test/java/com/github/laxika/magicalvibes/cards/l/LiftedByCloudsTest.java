package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LiftedByCloudsTest extends BaseCardTest {

    @Test
    @DisplayName("Grants flying to target creature until end of turn")
    void grantsFlyingUntilEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new LiftedByClouds()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
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
    @DisplayName("Splices onto an Arcane spell and stays in hand")
    void splicesOntoArcaneSpell() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Card arcaneShock = new Shock().createRuntimeCopy();
        arcaneShock.setSubtypes(List.of(CardSubtype.ARCANE));
        LiftedByClouds lifted = new LiftedByClouds();
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(arcaneShock, lifted));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithSplice(player1, 0, giant.getId(), List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, giant, Keyword.FLYING)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lifted);
    }
}
