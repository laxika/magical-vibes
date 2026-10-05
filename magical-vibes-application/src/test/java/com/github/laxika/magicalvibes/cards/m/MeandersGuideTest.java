package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MeandersGuide.class, MerfolkOfThePearlTrident.class, GrizzlyBears.class, HillGiant.class})
class MeandersGuideTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping another Merfolk returns a creature with mana value 3 or less")
    void tappingAnotherMerfolkReturnsCheapCreature() {
        addCreatureReady(player1, new MeandersGuide());
        Permanent merfolk = addCreatureReady(player1, new MerfolkOfThePearlTrident());
        addCreatureReady(player1, new MerfolkOfThePearlTrident());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, merfolk.getId());
        harness.passBothPriorities();

        assertThat(merfolk.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the tap does not return a creature")
    void decliningTapDoesNothing() {
        addCreatureReady(player1, new MeandersGuide());
        Permanent merfolk = addCreatureReady(player1, new MerfolkOfThePearlTrident());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(merfolk.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A card without a matching creature target stays in the graveyard")
    void filtersGraveyardTargets() {
        addCreatureReady(player1, new MeandersGuide());
        addCreatureReady(player1, new MerfolkOfThePearlTrident());
        harness.setGraveyard(player1, List.of(new HillGiant()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    @Test
    @DisplayName("The attacking Meanders Guide cannot be tapped as the other Merfolk")
    void requiresAnotherMerfolk() {
        addCreatureReady(player1, new MeandersGuide());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Reanimation waits for the reflexive trigger to resolve after tapping")
    void tappingCreatesASeparateReanimationTrigger() {
        addCreatureReady(player1, new MeandersGuide());
        Permanent merfolk = addCreatureReady(player1, new MeandersGuide());
        harness.setGraveyard(player1, List.of(new MeandersGuide()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(merfolk.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Meanders Guide");
        assertThat(countPermanents(player1, "Meanders Guide")).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Meanders Guide");
        assertThat(countPermanents(player1, "Meanders Guide")).isEqualTo(3);
    }

    @Test
    @DisplayName("A summoning-sick Merfolk can be tapped and mana value three can be returned")
    void canTapSummoningSickMerfolkAndReturnManaValueThree() {
        addCreatureReady(player1, new MeandersGuide());
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new MeandersGuide());
        harness.setGraveyard(player1, List.of(new MeandersGuide()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(merfolk.isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Meanders Guide");
        assertThat(countPermanents(player1, "Meanders Guide")).isEqualTo(3);
    }

    @Test
    @DisplayName("An already tapped Merfolk cannot pay for reanimation")
    void cannotTapAlreadyTappedMerfolk() {
        addCreatureReady(player1, new MeandersGuide());
        Permanent merfolk = addCreatureReady(player1, new MeandersGuide());
        merfolk.tap();
        harness.setGraveyard(player1, List.of(new MeandersGuide()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Meanders Guide");
        assertThat(countPermanents(player1, "Meanders Guide")).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's untapped Merfolk cannot pay for reanimation")
    void cannotTapOpponentsMerfolk() {
        addCreatureReady(player1, new MeandersGuide());
        Permanent merfolk = addCreatureReady(player2, new MeandersGuide());
        harness.setGraveyard(player1, List.of(new MeandersGuide()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(merfolk.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Meanders Guide");
    }

    @Test
    @DisplayName("Tapping is allowed without a legal target and cannot return an opponent's card")
    void canTapWithoutTargetsButCannotReturnOpponentsCard() {
        addCreatureReady(player1, new MeandersGuide());
        Permanent merfolk = addCreatureReady(player1, new MeandersGuide());
        harness.setGraveyard(player2, List.of(new MeandersGuide()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(merfolk.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Meanders Guide");
        harness.assertNotOnBattlefield(player2, "Meanders Guide");
        assertThat(countPermanents(player1, "Meanders Guide")).isEqualTo(2);
    }

    @Test
    @CardUsed({MeandersGuide.class, Plains.class})
    @DisplayName("A noncreature card with low mana value cannot be returned")
    void cannotReturnNoncreatureCard() {
        addCreatureReady(player1, new MeandersGuide());
        Permanent merfolk = addCreatureReady(player1, new MeandersGuide());
        harness.setGraveyard(player1, List.of(new Plains()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(merfolk.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Plains");
        harness.assertNotOnBattlefield(player1, "Plains");
    }
}
