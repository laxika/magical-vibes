package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
}
