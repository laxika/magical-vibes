package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConsumingVortexTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target creature to its owner's hand")
    void returnsTargetCreatureToOwnersHand() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ConsumingVortex()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        assertThat(gd.playerHands.get(player2.getId())).anyMatch(GrizzlyBears.class::isInstance);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ConsumingVortex()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Splices onto an Arcane spell and stays in hand")
    void splicesOntoArcaneSpell() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card arcaneShock = new Shock().createRuntimeCopy();
        arcaneShock.setSubtypes(List.of(CardSubtype.ARCANE));
        ConsumingVortex vortex = new ConsumingVortex();
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(arcaneShock, vortex));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castWithSplice(player1, 0, bears.getId(), List.of(1));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(vortex);
    }
}
