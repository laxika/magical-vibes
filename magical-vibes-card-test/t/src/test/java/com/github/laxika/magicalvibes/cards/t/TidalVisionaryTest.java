package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarKnight;
import com.github.laxika.magicalvibes.cards.n.NomadicElf;
import com.github.laxika.magicalvibes.cards.r.Repulse;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TidalVisionary.class, NomadicElf.class, LlanowarKnight.class, Forest.class, Repulse.class})
class TidalVisionaryTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature becomes the chosen color until end of turn")
    void targetCreatureBecomesChosenColor() {
        Permanent visionary = addReadyVisionary();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NomadicElf());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");

        assertThat(visionary.isTapped()).isTrue();
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.RED);
    }

    @Test
    @DisplayName("The chosen color wears off at end of turn")
    void chosenColorWearsOffAtEndOfTurn() {
        addReadyVisionary();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NomadicElf());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.RED);

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.RED);
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.GREEN);
    }

    @Test
    void canTargetItselfAndOffersAllFiveColors() {
        Permanent visionary = addReadyVisionary();

        harness.activateAbility(player1, 0, 0, null, visionary.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        harness.handleListChoice(player1, "BLACK");

        assertThat(gqs.getEffectiveColors(gd, visionary)).containsExactly(CardColor.BLACK);
        assertThat(visionary.isTapped()).isTrue();
    }

    @Test
    void replacesAllColorsOfAMulticoloredCreature() {
        addReadyVisionary();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarKnight());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.BLUE);
    }

    @Test
    void targetBecomesIllegalWhenSourceBecomesBlack() {
        Permanent visionary = addReadyVisionary();
        addCreatureReady(player1, new TidalVisionary());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarKnight());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.activateAbility(player1, 1, 0, null, visionary.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");
        assertThat(gqs.getEffectiveColors(gd, visionary)).containsExactly(CardColor.BLACK);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
    }

    @Test
    void mostRecentlyResolvedColorChangeWins() {
        addReadyVisionary();
        addCreatureReady(player1, new TidalVisionary());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NomadicElf());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.activateAbility(player1, 1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.RED);

        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.WHITE);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent visionary = harness.addToBattlefieldAndReturn(player1, new TidalVisionary());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, visionary.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(visionary.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateAgainWhileTapped() {
        Permanent visionary = addReadyVisionary();
        harness.activateAbility(player1, 0, 0, null, visionary.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, visionary.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");
    }

    @Test
    void cannotTargetANoncreaturePermanent() {
        Permanent visionary = addReadyVisionary();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(visionary.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotChooseAColorWhenTargetLeavesBeforeResolution() {
        Permanent visionary = addReadyVisionary();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NomadicElf());
        harness.setHand(player2, List.of(new Repulse()));
        harness.setLibrary(player2, List.of(new NomadicElf()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.assertInHand(player2, "Nomadic Elf");
        harness.assertNotOnBattlefield(player2, "Nomadic Elf");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(visionary.isTapped()).isTrue();
    }

    @Test
    void resolvesAfterVisionaryLeavesTheBattlefield() {
        Permanent visionary = addReadyVisionary();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NomadicElf());
        harness.setHand(player2, List.of(new Repulse()));
        harness.setLibrary(player2, List.of(new NomadicElf()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.castInstant(player2, 0, visionary.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Tidal Visionary");
        harness.assertNotOnBattlefield(player1, "Tidal Visionary");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.RED);
    }

    private Permanent addReadyVisionary() {
        return addCreatureReady(player1, new TidalVisionary());
    }
}
