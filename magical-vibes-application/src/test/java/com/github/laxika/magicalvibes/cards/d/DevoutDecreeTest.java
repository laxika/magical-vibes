package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BogRaiders;
import com.github.laxika.magicalvibes.cards.c.ChandraTheFirebrand;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.b.BloodMoon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DevoutDecreeTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling a black creature is followed by scry 1")
    void exilesBlackCreatureAndScries() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BogRaiders());
        Card top = new Forest();
        harness.setLibrary(player1, List.of(top, new Mountain()));
        castAt(target.getId());

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(top);
    }

    @Test
    @DisplayName("A red planeswalker is a legal target")
    void exilesRedPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraTheFirebrand());
        target.setCounterCount(CounterType.LOYALTY, 3);
        castAt(target.getId());

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
    }

    @Test
    @DisplayName("A non-black non-red creature cannot be targeted")
    void cannotTargetGreenCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> castAt(target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A red noncreature nonplaneswalker cannot be targeted")
    void cannotTargetRedEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BloodMoon());

        assertThatThrownBy(() -> castAt(target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castAt(UUID targetId) {
        harness.setHand(player1, List.of(new DevoutDecree()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, targetId);
    }
}
