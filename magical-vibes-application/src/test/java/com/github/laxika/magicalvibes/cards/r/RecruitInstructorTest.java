package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AngelfireIgnition;
import com.github.laxika.magicalvibes.cards.b.BargeIn;
import com.github.laxika.magicalvibes.cards.b.BecomeBrutes;
import com.github.laxika.magicalvibes.cards.b.BoonOfSafety;
import com.github.laxika.magicalvibes.cards.c.CheekyHouseMouse;
import com.github.laxika.magicalvibes.cards.c.CrumbAndGetIt;
import com.github.laxika.magicalvibes.cards.d.DefiantStrike;
import com.github.laxika.magicalvibes.cards.e.Embercleave;
import com.github.laxika.magicalvibes.cards.f.FeatherOfFlight;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.m.MabelsMettle;
import com.github.laxika.magicalvibes.cards.m.MightOfTheMeek;
import com.github.laxika.magicalvibes.cards.m.MomentOfHeroism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.u.UnleashFury;
import com.github.laxika.magicalvibes.cards.w.WarSqueak;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RecruitInstructor.class, AngelfireIgnition.class, BargeIn.class, BecomeBrutes.class,
        BoonOfSafety.class, CheekyHouseMouse.class, CrumbAndGetIt.class, DefiantStrike.class,
        Embercleave.class, FeatherOfFlight.class, MabelsMettle.class, MightOfTheMeek.class,
        MomentOfHeroism.class, UnleashFury.class, WarSqueak.class, GiantGrowth.class})
class RecruitInstructorTest extends BaseCardTest {

    @Test
    void attackingOffersThreeRandomSpellbookCardsAndAddsTheChosenCardToHand() {
        Permanent instructor = addCreatureReady(player1, new RecruitInstructor());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(instructor)));
        resolveAllTriggers();

        PendingInteraction.SpellbookCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookCardChoice.class);
        assertThat(choice.spellbookCards()).hasSize(3);
        assertThat(choice.spellbookCards())
                .extracting(Card::getName)
                .allMatch(Set.of(
                        "Angelfire Ignition", "Barge In", "Become Brutes", "Boon of Safety",
                        "Cheeky House-Mouse", "Crumb and Get It", "Defiant Strike", "Embercleave",
                        "Feather of Flight", "Mabel's Mettle", "Might of the Meek", "Moment of Heroism",
                        "Unleash Fury", "War Squeak")::contains);

        harness.handleMultipleCardsChosen(player1, List.of(choice.validCardIds().getFirst()));

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .contains(choice.spellbookCards().getFirst().getName());
    }

    @Test
    void valiantCreatesOnlyOneMouseTheFirstTimeEachTurn() {
        Permanent instructor = addCreatureReady(player1, new RecruitInstructor());
        harness.setHand(player1, List.of(new GiantGrowth(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, instructor.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castInstant(player1, 0, instructor.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Mouse")).isEqualTo(1);
    }
}
