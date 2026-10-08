package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.b.BraveKinDuo;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VeteranGuardmouse.class, GiantGrowth.class, BraveKinDuo.class})
class VeteranGuardmouseTest extends BaseCardTest {

    @Test
    void controlledActivatedAbilityTriggersValiantAndCanBottomTheCard() {
        Permanent mouse = harness.addToBattlefieldAndReturn(player1, new VeteranGuardmouse());
        Permanent duo = harness.addToBattlefieldAndReturn(player1, new BraveKinDuo());
        duo.setSummoningSick(false);
        BraveKinDuo top = new BraveKinDuo();
        VeteranGuardmouse second = new VeteranGuardmouse();
        harness.setLibrary(player1, List.of(top, second));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, mouse.getId());
        harness.passBothPriorities();

        assertThat(mouse.getEffectivePower()).isEqualTo(4);
        assertThat(mouse.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, mouse, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, top);
        assertThat(mouse.getEffectivePower()).isEqualTo(5);
        assertThat(mouse.getEffectiveToughness()).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void emptyLibraryDoesNotPreventBoostOrFirstStrike() {
        Permanent mouse = harness.addToBattlefieldAndReturn(player1, new VeteranGuardmouse());
        Permanent duo = harness.addToBattlefieldAndReturn(player1, new BraveKinDuo());
        duo.setSummoningSick(false);
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, mouse.getId());
        harness.passBothPriorities();

        assertThat(mouse.getEffectivePower()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, mouse, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        assertThat(mouse.getEffectivePower()).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsTargetingDoesNotConsumeValiantForTheTurn() {
        Permanent mouse = harness.addToBattlefieldAndReturn(player1, new VeteranGuardmouse());
        harness.setLibrary(player1, List.of(new GiantGrowth()));
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, mouse.getId());
        harness.passBothPriorities();
        assertThat(mouse.getEffectivePower()).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, mouse, Keyword.FIRST_STRIKE)).isFalse();

        harness.castInstant(player1, 0, mouse.getId());
        harness.passBothPriorities();
        assertThat(mouse.getEffectivePower()).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, mouse, Keyword.FIRST_STRIKE)).isTrue();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();
        assertThat(mouse.getEffectivePower()).isEqualTo(10);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void valiantExpiresAndTriggersAgainDuringOpponentsTurn() {
        Permanent mouse = harness.addToBattlefieldAndReturn(player1, new VeteranGuardmouse());
        harness.setLibrary(player1, List.of(new GiantGrowth(), new GiantGrowth()));
        harness.setLibrary(player2, List.of(new GiantGrowth(), new GiantGrowth()));
        harness.setHand(player1, List.of(new GiantGrowth(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, mouse.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(mouse.getEffectivePower()).isEqualTo(3);
        assertThat(mouse.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, mouse, Keyword.FIRST_STRIKE)).isFalse();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, mouse.getId());
        harness.passBothPriorities();
        assertThat(mouse.getEffectivePower()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, mouse, Keyword.FIRST_STRIKE)).isTrue();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();
        assertThat(mouse.getEffectivePower()).isEqualTo(7);
        assertThat(gd.stack).isEmpty();
    }

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
