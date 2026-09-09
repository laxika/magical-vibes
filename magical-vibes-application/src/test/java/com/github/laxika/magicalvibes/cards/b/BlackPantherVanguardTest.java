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
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, mode);
        harness.passBothPriorities();
    }
}
