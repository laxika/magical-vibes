package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SonicScrewdriver;
import com.github.laxika.magicalvibes.cards.t.Tardis;
import com.github.laxika.magicalvibes.cards.t.TheWarDoctor;
import com.github.laxika.magicalvibes.cards.t.TwiceUponATimeUnlikelyMeeting;
import com.github.laxika.magicalvibes.cards.u.UnlikelyMeeting;
import com.github.laxika.magicalvibes.cards.v.VillageRites;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RyanSinclair.class, Forest.class, GrizzlyBears.class, HillGiant.class,
        SonicScrewdriver.class, Tardis.class, TheWarDoctor.class,
        TwiceUponATimeUnlikelyMeeting.class, UnlikelyMeeting.class, VillageRites.class})
class RyanSinclairTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking exiles through lands and offers the first nonland within Ryan's power")
    void offersFirstEligibleNonland() {
        Forest land = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        readyRyan();
        harness.setLibrary(player1, List.of(land, bears));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(bears);
        assertThat(search.params().destination())
                .isEqualTo(LibrarySearchDestination.CAST_WITHOUT_PAYING);
    }

    @Test
    @DisplayName("Stops at the first nonland even when that card is too expensive")
    void stopsAtFirstNonland() {
        Forest land = new Forest();
        HillGiant tooExpensive = new HillGiant();
        GrizzlyBears laterCard = new GrizzlyBears();
        readyRyan();
        harness.setLibrary(player1, List.of(land, tooExpensive, laterCard));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(land, tooExpensive, laterCard);
    }

    @Test
    @DisplayName("The offered card is cast without paying mana")
    void castsOfferedCardForFree() {
        GrizzlyBears bears = new GrizzlyBears();
        readyRyan();
        harness.setLibrary(player1, List.of(bears));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == bears);
    }

    @Test
    void decliningReturnsExiledCardsBelowTheUntouchedLibrary() {
        Forest land = new Forest();
        Tardis offered = new Tardis();
        Forest untouched = new Forest();
        readyRyan();
        harness.setLibrary(player1, List.of(land, offered, untouched));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3).startsWith(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(land, offered);
        assertThat(gd.findExiledCard(land.getId())).isNull();
        assertThat(gd.findExiledCard(offered.getId())).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void allLandLibraryIsReturnedWithoutOfferingACast() {
        Forest first = new Forest();
        Forest second = new Forest();
        readyRyan();
        harness.setLibrary(player1, List.of(first, second));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void emptyLibraryDoesNotOfferACast() {
        readyRyan();
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void usesPowerAtResolutionRatherThanAtAttack() {
        Permanent ryan = addCreatureReady(player1, new RyanSinclair());
        SonicScrewdriver screwdriver = new SonicScrewdriver();
        harness.setLibrary(player1, List.of(screwdriver));

        declareAttackers(List.of(0));
        ryan.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sonic Screwdriver");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void eachExiledCardTriggersTheWarDoctorEvenWhenCastingIsDeclined() {
        readyRyan();
        Permanent doctor = harness.addToBattlefieldAndReturn(player1, new TheWarDoctor());
        Forest first = new Forest();
        Forest second = new Forest();
        Tardis offered = new Tardis();
        harness.setLibrary(player1, List.of(first, second, offered));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleCardChosen(player1, -1);
        resolveAllTriggers();

        assertThat(doctor.getCounterCount(CounterType.TIME)).isEqualTo(3);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(first, second, offered);
    }

    @Test
    void offeredSpellIsCastFromExile() {
        readyRyan();
        Tardis offered = new Tardis();
        harness.setLibrary(player1, List.of(offered));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getSourceZone()).isEqualTo(Zone.EXILE);
    }

    @Test
    void offersAnEligibleAdventureEvenWhenTheNonAdventureManaValueIsTooHigh() {
        Permanent ryan = addCreatureReady(player1, new RyanSinclair());
        ryan.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        TwiceUponATimeUnlikelyMeeting adventure = new TwiceUponATimeUnlikelyMeeting();
        harness.setLibrary(player1, List.of(adventure));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        PendingInteraction.LibrarySearch choice =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(choice).isNotNull();
        assertThat(choice.params().cards()).containsExactly(adventure);
        harness.handleCardChosen(player1, -1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(adventure);
    }

    private void readyRyan() {
        addCreatureReady(player1, new RyanSinclair());
    }

    @Test
    void mandatorySacrificeCostMustBePaidBeforeTheSpellIsCast() {
        readyRyan();
        harness.addToBattlefield(player1, new TheWarDoctor());
        VillageRites rites = new VillageRites();
        harness.setLibrary(player1, List.of(rites));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);

        boolean ritesCast = gd.stack.stream().anyMatch(entry -> entry.getCard() == rites);
        boolean bothCreaturesRemain = gd.playerBattlefields.get(player1.getId()).size() == 2;
        assertThat(ritesCast && bothCreaturesRemain)
                .as("Village Rites cannot be cast while leaving both potential sacrifices untouched")
                .isFalse();
    }
}
