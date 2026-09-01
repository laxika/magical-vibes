package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VeteranGuardmouse.class, GiantGrowth.class})
class VeteranGuardmouseTest extends BaseCardTest {

    @Test
    void valiantBoostsGrantsFirstStrikeAndScries() {
        Permanent mouse = harness.addToBattlefieldAndReturn(player1, new VeteranGuardmouse());
        harness.setLibrary(player1, List.of(new GiantGrowth(), new GiantGrowth()));
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, mouse.getId());
        harness.passBothPriorities();

        assertThat(mouse.getEffectivePower()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, mouse, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();

        assertThat(mouse.getEffectivePower()).isEqualTo(7);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void valiantTriggersOnlyOnceEachTurn() {
        Permanent mouse = harness.addToBattlefieldAndReturn(player1, new VeteranGuardmouse());
        harness.setLibrary(player1, List.of(new GiantGrowth(), new GiantGrowth()));
        harness.setHand(player1, List.of(new GiantGrowth(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, mouse.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();
        int powerAfterFirstSpell = mouse.getEffectivePower();

        harness.castInstant(player1, 0, mouse.getId());
        harness.passBothPriorities();

        assertThat(powerAfterFirstSpell).isEqualTo(7);
        assertThat(mouse.getEffectivePower()).isEqualTo(powerAfterFirstSpell + 3);
    }

    @Test
    void valiantDoesNotTriggerForOpponentsSpell() {
        Permanent mouse = harness.addToBattlefieldAndReturn(player1, new VeteranGuardmouse());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, mouse.getId());
        harness.passBothPriorities();

        assertThat(mouse.getEffectivePower()).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, mouse, Keyword.FIRST_STRIKE)).isFalse();
    }
}
