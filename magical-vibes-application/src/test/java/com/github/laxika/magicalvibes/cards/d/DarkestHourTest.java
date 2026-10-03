package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoblinRaider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HoppingAutomaton;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.SleightOfMind;
import com.github.laxika.magicalvibes.cards.w.WildDogs;
import com.github.laxika.magicalvibes.cards.w.WornPowerstone;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarkestHour.class, Forest.class, GoblinRaider.class, GrizzlyBears.class, HoppingAutomaton.class, RagingGoblin.class, WildDogs.class, WornPowerstone.class})
class DarkestHourTest extends BaseCardTest {

    @Test
    @DisplayName("Your creatures become black, replacing their colors")
    void recolorsOwnCreatures() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        harness.addToBattlefield(player1, new DarkestHour());

        assertThat(gqs.getEffectiveColors(gd, goblin)).containsExactly(CardColor.BLACK);
        assertThat(gqs.getEffectiveColors(gd, goblin)).doesNotContain(CardColor.RED);
    }

    @Test
    @DisplayName("Your creatures become black, replacing their colors")
    void recolorsOwnCreaturesUpstreamReview() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinRaider());
        harness.addToBattlefield(player1, new DarkestHour());

        assertThat(gqs.getEffectiveColors(gd, goblin)).containsExactly(CardColor.BLACK);
        assertThat(gqs.getEffectiveColors(gd, goblin)).doesNotContain(CardColor.RED);
    }

    @Test
    @DisplayName("Opponent's creatures also become black")
    void recolorsOpponentCreatures() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new DarkestHour());

        assertThat(gqs.getEffectiveColors(gd, bear)).containsExactly(CardColor.BLACK);
    }

    @Test
    @DisplayName("Opponent's creatures also become black")
    void recolorsOpponentCreaturesUpstreamReview() {
        Permanent dog = harness.addToBattlefieldAndReturn(player2, new WildDogs());
        harness.addToBattlefield(player1, new DarkestHour());

        assertThat(gqs.getEffectiveColors(gd, dog)).containsExactly(CardColor.BLACK);
    }

    @Test
    @DisplayName("Noncreature permanents are not recolored")
    void doesNotRecolorNoncreatures() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new DarkestHour());

        assertThat(gqs.getEffectiveColors(gd, forest)).doesNotContain(CardColor.BLACK);
    }

    @Test
    @DisplayName("Noncreature permanents are not recolored")
    void doesNotRecolorNoncreaturesUpstreamReview() {
        Permanent powerstone = harness.addToBattlefieldAndReturn(player1, new WornPowerstone());
        harness.addToBattlefield(player1, new DarkestHour());

        assertThat(gqs.getEffectiveColors(gd, powerstone)).doesNotContain(CardColor.BLACK);
    }

    @Test
    @DisplayName("Creatures entering after Darkest Hour are also black")
    void recolorsCreaturesEnteringLater() {
        harness.addToBattlefield(player1, new DarkestHour());
        Permanent goblin = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());

        assertThat(gqs.getEffectiveColors(gd, goblin)).containsExactly(CardColor.BLACK);
    }

    @Test
    @DisplayName("Creatures entering after Darkest Hour becomes black")
    void recolorsCreaturesEnteringLaterUpstreamReview() {
        harness.addToBattlefield(player1, new DarkestHour());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinRaider());

        assertThat(gqs.getEffectiveColors(gd, goblin)).containsExactly(CardColor.BLACK);
    }

    @Test
    @DisplayName("Full flow: cast and resolve, then all creatures are black")
    void fullFlow() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        harness.setHand(player1, List.of(new DarkestHour()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, goblin)).containsExactly(CardColor.BLACK);
    }

    @Test
    @DisplayName("Full flow: cast and resolve, then all creatures are black")
    void fullFlowUpstreamReview() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinRaider());
        harness.setHand(player1, List.of(new DarkestHour()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, goblin)).containsExactly(CardColor.BLACK);
    }

    @Test
    @DisplayName("Colorless creatures become black")
    void recolorsColorlessCreatures() {
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new HoppingAutomaton());
        harness.addToBattlefield(player1, new DarkestHour());

        assertThat(gqs.getEffectiveColors(gd, automaton)).containsExactly(CardColor.BLACK);
    }

    @Test
    @CardUsed({Disenchant.class})
    @DisplayName("Removing Darkest Hour restores both players' creatures' original colors")
    void colorsReturnWhenDarkestHourLeaves() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinRaider());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent darkestHour = harness.addToBattlefieldAndReturn(player1, new DarkestHour());
        assertThat(gqs.getEffectiveColors(gd, goblin)).containsExactly(CardColor.BLACK);
        assertThat(gqs.getEffectiveColors(gd, bear)).containsExactly(CardColor.BLACK);
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, darkestHour.getId());

        harness.assertInGraveyard(player1, "Darkest Hour");
        assertThat(gqs.getEffectiveColors(gd, goblin)).containsExactly(CardColor.RED);
        assertThat(gqs.getEffectiveColors(gd, bear)).containsExactly(CardColor.GREEN);
    }

    @Test
    @CardUsed({Opalescence.class, SleightOfMind.class})
    @DisplayName("An animated Darkest Hour is affected by its own text-changed color setting")
    void recolorsItselfWhenAnimatedAndTextChanged() {
        harness.addToBattlefield(player1, new Opalescence());
        Permanent darkestHour = harness.addToBattlefieldAndReturn(player1, new DarkestHour());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SleightOfMind()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, darkestHour.getId());
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "BLUE");

        assertThat(gqs.getEffectiveColors(gd, bear)).containsExactly(CardColor.BLUE);
        assertThat(gqs.getEffectiveColors(gd, darkestHour)).containsExactly(CardColor.BLUE);
    }

    @Test
    @CardUsed({SleightOfMind.class})
    @DisplayName("Text-changing Darkest Hour recolors creatures but not the unanimated enchantment")
    void textChangeOnlyRecolorsCreatures() {
        Permanent darkestHour = harness.addToBattlefieldAndReturn(player1, new DarkestHour());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinRaider());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SleightOfMind()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, darkestHour.getId());
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "BLUE");

        assertThat(gqs.getEffectiveColors(gd, goblin)).containsExactly(CardColor.BLUE);
        assertThat(gqs.getEffectiveColors(gd, bear)).containsExactly(CardColor.BLUE);
        assertThat(gqs.getEffectiveColors(gd, darkestHour)).containsExactly(CardColor.BLACK);
    }
}
