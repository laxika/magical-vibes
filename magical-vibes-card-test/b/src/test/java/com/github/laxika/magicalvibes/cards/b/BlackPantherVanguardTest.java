package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DragoonsWyvern;
import com.github.laxika.magicalvibes.cards.c.CaptainAmericaWingsOfFreedom;
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

@CardUsed({BlackPantherVanguard.class, CaptainAmericaWingsOfFreedom.class, DragoonsWyvern.class})
class BlackPantherVanguardTest extends BaseCardTest {

    private static final String SOLDIER = "Create a 1/1 white Soldier creature token.";
    private static final String BOOST = "Creatures you control get +1/+1 until end of turn.";

    @Test
    @DisplayName("A nontoken Hero entering lets you create a Soldier token")
    void heroEntryCreatesSoldierToken() {
        addPanther();

        castCaptainAmerica();
        chooseMode(SOLDIER);

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(1);
    }

    @Test
    @DisplayName("A nontoken Hero entering can boost all creatures until end of turn")
    void heroEntryBoostsCreatures() {
        Permanent panther = addPanther();

        castCaptainAmerica();
        chooseMode(BOOST);

        assertThat(gqs.getEffectivePower(gd, panther)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, panther)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, panther)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, panther)).isEqualTo(4);
    }

    @Test
    @DisplayName("A non-Hero creature and a Hero token do not trigger the ability")
    void ignoresNonHeroAndTokenEntries() {
        addPanther();

        harness.setHand(player1, List.of(new DragoonsWyvern()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Hero")).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Black Panther does not trigger for its own entry")
    void ignoresItsOwnEntry() {
        harness.enterBattlefieldAndReturn(player1, new BlackPantherVanguard());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
    }

    @Test
    @DisplayName("An opposing Hero does not trigger Black Panther")
    void ignoresOpposingHeroEntry() {
        addPanther();
        harness.enterBattlefieldAndReturn(player2, new CaptainAmericaWingsOfFreedom());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
    }

    @Test
    @DisplayName("The mode must be chosen before opponents can respond to the trigger")
    void choosesModeWhenTriggerGoesOnStack() {
        addPanther();
        castCaptainAmerica();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, SOLDIER);
        assertThat(countPermanents(player1, "Soldier")).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Soldier")).isEqualTo(1);
    }

    @Test
    @DisplayName("The boost includes non-Hero allies but excludes opponents and later creatures")
    void boostsOnlyCreaturesControlledAtResolution() {
        addPanther();
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new DragoonsWyvern());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new DragoonsWyvern());
        castCaptainAmerica();
        chooseMode(BOOST);

        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(2);
        Permanent captain = findPermanent(player1, "Captain America, Wings of Freedom");
        assertThat(gqs.getEffectivePower(gd, captain)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, captain)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(1);

        Permanent later = harness.enterBattlefieldAndReturn(player1, new DragoonsWyvern());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, later)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, later)).isEqualTo(1);
    }

    private Permanent addPanther() {
        return harness.addToBattlefieldAndReturn(player1, new BlackPantherVanguard());
    }

    private void castCaptainAmerica() {
        harness.setHand(player1, List.of(new CaptainAmericaWingsOfFreedom()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void chooseMode(String mode) {
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, mode);
        harness.passBothPriorities();
    }
}
