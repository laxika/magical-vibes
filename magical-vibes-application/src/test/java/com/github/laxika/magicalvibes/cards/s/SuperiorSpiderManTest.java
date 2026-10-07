package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.cards.p.ProfessionalWrestler;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SuperiorSpiderMan.class, GrizzlyBears.class, Clone.class,
        ProfessionalWrestler.class, SpiderSense.class, SelflessPoliceCaptain.class})
class SuperiorSpiderManTest extends BaseCardTest {

    @Test
    void copiesCreatureCardFromAnyGraveyardAndExilesItAfterTheReflexiveTriggerResolves() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        harness.castFromHand(player1, new SuperiorSpiderMan(), "{2}{U}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        Permanent superior = findPermanent(player1, "Superior Spider-Man");
        assertThat(superior.getCard().getName()).isEqualTo("Superior Spider-Man");
        assertThat(superior.getCard().getPower()).isEqualTo(4);
        assertThat(superior.getCard().getToughness()).isEqualTo(4);
        assertThat(superior.getCard().getSubtypes())
                .contains(CardSubtype.SPIDER, CardSubtype.HUMAN, CardSubtype.HERO);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(bears);
        assertThat(gd.findExiledCard(bears.getId())).isNull();

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(bears);
        assertThat(gd.findExiledCard(bears.getId())).isNotNull();
    }

    @Test
    void decliningMindSwapLeavesTheCreatureCardInTheGraveyard() {
        ProfessionalWrestler wrestler = new ProfessionalWrestler();
        harness.setGraveyard(player1, List.of(wrestler));
        harness.castFromHand(player1, new SuperiorSpiderMan(), "{2}{U}{B}");
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Superior Spider-Man");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(wrestler);
        assertThat(gd.findExiledCard(wrestler.getId())).isNull();
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    void entersWithoutCopyingWhenNeitherGraveyardContainsACreature() {
        SpiderSense instant = new SpiderSense();
        harness.setGraveyard(player1, List.of(instant));
        harness.setGraveyard(player2, List.of());
        harness.castFromHand(player1, new SuperiorSpiderMan(), "{2}{U}{B}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Superior Spider-Man");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(instant);
        assertThat(gd.findExiledCard(instant.getId())).isNull();
    }

    @Test
    void copiesItsControllersCreatureAndRetainsItsOtherSubtypesAndEntryAbility() {
        ProfessionalWrestler wrestler = new ProfessionalWrestler();
        harness.setGraveyard(player1, List.of(wrestler));
        harness.castFromHand(player1, new SuperiorSpiderMan(), "{2}{U}{B}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(wrestler.getId()));

        Permanent superior = findPermanent(player1, "Superior Spider-Man");
        assertThat(superior.getCard().getSubtypes()).contains(
                CardSubtype.HUMAN, CardSubtype.WARRIOR, CardSubtype.PERFORMER,
                CardSubtype.SPIDER, CardSubtype.HERO);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(gd.findExiledCard(wrestler.getId())).isNotNull();
    }

    @Test
    void counteringTheReflexiveAbilityDoesNotUndoTheCopy() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        harness.setHand(player2, List.of(new SpiderSense()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castFromHand(player1, new SuperiorSpiderMan(), "{2}{U}{B}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, gd.stack.getLast().getTargetableId());
        resolveAllTriggers();

        Permanent superior = findPermanent(player1, "Superior Spider-Man");
        assertThat(superior.getCard().getSubtypes()).contains(CardSubtype.BEAR);
        assertThat(superior.getCard().getPower()).isEqualTo(4);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(bears);
        assertThat(gd.findExiledCard(bears.getId())).isNull();
    }

    @Test
    void copiedEntryCountersAreAddedOnTopOfTheFourFourBase() {
        SelflessPoliceCaptain captain = new SelflessPoliceCaptain();
        harness.setGraveyard(player2, List.of(captain));
        harness.castFromHand(player1, new SuperiorSpiderMan(), "{2}{U}{B}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(captain.getId()));

        Permanent superior = findPermanent(player1, "Superior Spider-Man");
        assertThat(superior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, superior)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, superior)).isEqualTo(5);
        resolveAllTriggers();
        assertThat(gd.findExiledCard(captain.getId())).isNotNull();
    }

    @Test
    void copyingCloneOffersItsCopyReplacementBeforeEntering() {
        Clone clone = new Clone();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(clone));
        harness.castFromHand(player1, new SuperiorSpiderMan(), "{2}{U}{B}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(clone.getId()));

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        Permanent copiedBears = findPermanent(player1, "Grizzly Bears");
        assertThat(copiedBears.getCard().getPower()).isEqualTo(2);
        assertThat(copiedBears.getCard().getToughness()).isEqualTo(2);
        assertThat(copiedBears.getCard().getSubtypes())
                .containsExactly(CardSubtype.BEAR);
        assertThat(gd.findExiledCard(clone.getId())).isNotNull();
    }
}
