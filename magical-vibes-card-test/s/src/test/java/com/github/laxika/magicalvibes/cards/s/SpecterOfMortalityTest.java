package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CandyGrapple;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

@CardUsed({SpecterOfMortality.class, Forest.class, GiantSpider.class, HillGiant.class, CandyGrapple.class})
class SpecterOfMortalityTest extends BaseCardTest {

    @Test
    @DisplayName("Exiled creature cards determine the -X/-X applied to every other creature")
    void exiledCreatureCountDeterminesOtherCreatureDebuff() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        GiantSpider graveyardSpider = new GiantSpider();
        HillGiant graveyardGiant = new HillGiant();
        harness.setGraveyard(player1, List.of(graveyardSpider, graveyardGiant));

        castSpecter();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(graveyardSpider.getId(), graveyardGiant.getId()));
        harness.passBothPriorities();

        Permanent specter = findPermanent(player1, "Specter of Mortality");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getId())
                .containsExactlyInAnyOrder(graveyardSpider.getId(), graveyardGiant.getId());
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, specter)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, specter)).isEqualTo(3);
    }

    @Test
    @DisplayName("The controller may exile no creature cards and then nothing is weakened")
    void exilingNoCardsDoesNotWeakenCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        GiantSpider graveyardSpider = new GiantSpider();
        harness.setGraveyard(player1, List.of(graveyardSpider));

        castSpecter();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Only creature cards are offered and the debuff wears off at end of turn")
    void onlyCreatureCardsAreOfferedAndDebuffExpires() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        GiantSpider graveyardSpider = new GiantSpider();
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(graveyardSpider, forest));

        castSpecter();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(graveyardSpider.getId());
        harness.handleMultipleCardsChosen(player1, List.of(graveyardSpider.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(forest);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void canExileItselfIfItDiesBeforeItsEntersTriggerResolves() {
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, new SpecterOfMortality());
        SpecterOfMortality specter = new SpecterOfMortality();
        harness.setHand(player1, List.of(specter));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new CandyGrapple()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Specter of Mortality"));
        harness.assertInGraveyard(player1, "Specter of Mortality");
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(specter.getId());
        harness.handleMultipleCardsChosen(player1, List.of(specter.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(specter);
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
    }

    @Test
    void mayExileOnlySomeCardsAndNeverOffersOpponentsGraveyard() {
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, new SpecterOfMortality());
        SpecterOfMortality selected = new SpecterOfMortality();
        SpecterOfMortality unselected = new SpecterOfMortality();
        SpecterOfMortality opponentCard = new SpecterOfMortality();
        harness.setGraveyard(player1, List.of(selected, unselected));
        harness.setGraveyard(player2, List.of(opponentCard));

        castSpecter();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(selected.getId(), unselected.getId());
        harness.handleMultipleCardsChosen(player1, List.of(selected.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(selected);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(unselected);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
    }

    @Test
    void debuffUsesASeparateTriggerAndDoesNotAffectLaterEntrants() {
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, new SpecterOfMortality());
        SpecterOfMortality first = new SpecterOfMortality();
        SpecterOfMortality second = new SpecterOfMortality();
        SpecterOfMortality third = new SpecterOfMortality();
        harness.setGraveyard(player1, List.of(first, second, third));

        castSpecter();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId(), third.getId()));

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Specter of Mortality");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Specter of Mortality");
        harness.assertInGraveyard(player2, "Specter of Mortality");
        harness.assertOnBattlefield(player1, "Specter of Mortality");
        Permanent laterCreature = harness.addToBattlefieldAndReturn(player2, new SpecterOfMortality());
        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, laterCreature)).isEqualTo(3);
    }

    @Test
    void emptyGraveyardCreatesNoChoiceOrReflexiveTrigger() {
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, new SpecterOfMortality());
        harness.setGraveyard(player1, List.of());

        castSpecter();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(3);
    }

    private void castSpecter() {
        harness.setHand(player1, List.of(new SpecterOfMortality()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
