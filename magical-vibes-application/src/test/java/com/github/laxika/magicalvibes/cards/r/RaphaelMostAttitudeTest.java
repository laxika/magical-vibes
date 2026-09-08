package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RaphaelMostAttitude.class, Divination.class, Forest.class, GrizzlyBears.class})
class RaphaelMostAttitudeTest extends BaseCardTest {

    @Test
    void allianceMayExileTopCardOfYourLibraryWithRaphael() {
        Permanent raphael = addRaphael();
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getCardsExiledByPermanent(raphael.getId())).containsExactly(topCard);
    }

    @Test
    void decliningAllianceLeavesTopCardInYourLibrary() {
        addRaphael();
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void attackingAllowsPlayingOnePreviouslyExiledCardUntilEndOfTurn() {
        Permanent raphael = addRaphael();
        Card spell = new Divination();
        Card land = new Forest();
        gd.addToExile(player1.getId(), spell, raphael.getId());
        gd.addToExile(player1.getId(), land, raphael.getId());

        assertThat(harness.getCastingPermissionService().getCastableExiledCardIds(gd, player1.getId()))
                .isEmpty();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(harness.getCastingPermissionService().getCastableExiledCardIds(gd, player1.getId()))
                .containsExactlyInAnyOrder(spell.getId(), land.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(raphael.getId())).containsExactly(land);
        assertThat(harness.getCastingPermissionService().getCastableExiledCardIds(gd, player1.getId()))
                .isEmpty();
    }

    @Test
    void attackPlayPermissionExpiresAtEndOfTurn() {
        Permanent raphael = addRaphael();
        Card exiled = new GrizzlyBears();
        gd.addToExile(player1.getId(), exiled, raphael.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(harness.getCastingPermissionService().getCastableExiledCardIds(gd, player1.getId()))
                .contains(exiled.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(harness.getCastingPermissionService().getCastableExiledCardIds(gd, player1.getId()))
                .doesNotContain(exiled.getId());
        assertThat(gd.getCardsExiledByPermanent(raphael.getId())).containsExactly(exiled);
    }

    private Permanent addRaphael() {
        Permanent raphael = harness.addToBattlefieldAndReturn(player1, new RaphaelMostAttitude());
        raphael.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return raphael;
    }
}
