package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KodamasMightTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature +2/+2 until end of turn")
    void boostsTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new KodamasMight()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost wears off at cleanup step")
    void boostWearsOffAtCleanup() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new KodamasMight()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Splices onto an Arcane spell and stays in hand")
    void splicesOntoArcaneSpell() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card arcaneShock = new Shock().createRuntimeCopy();
        arcaneShock.setSubtypes(List.of(CardSubtype.ARCANE));
        KodamasMight might = new KodamasMight();
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(arcaneShock, might));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithSplice(player1, 0, bears.getId(), List.of(1));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        assertThat(bears.getEffectiveToughness()).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(might);
    }
}
