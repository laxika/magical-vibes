package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GaleConduitOfTheArcane.class, HolyDay.class, CounselOfTheSoratami.class,
        Mountain.class, Shock.class, GrizzlyBears.class, Swamp.class})
class GaleConduitOfTheArcaneTest extends BaseCardTest {

    @Test
    void castReturnsAnInstantOrSorceryFromGraveyard() {
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new GaleConduitOfTheArcane()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(instant.getId());
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(instant.getId()));
    }

    @Test
    void castDoesNotReturnAcreatureFromGraveyard() {
        Card creature = new GaleConduitOfTheArcane();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new GaleConduitOfTheArcane()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(creature.getId()));
    }

    @Test
    void stormFacePerpetuallyBoostsCreaturesAfterCastingAnInstant() {
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GaleConduitOfTheArcane(), new Mountain(), new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, 3, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent gale = findPermanent(player1, "Gale, Storm Conduit");
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, gale)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(3);
    }

    @Test
    void castReturnsASorceryAndDoesNotOfferOpponentsGraveyard() {
        Card sorcery = new CounselOfTheSoratami();
        Card opponentInstant = new HolyDay();
        harness.setGraveyard(player1, List.of(sorcery));
        harness.setGraveyard(player2, List.of(opponentInstant));
        harness.setHand(player1, List.of(new GaleConduitOfTheArcane()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(sorcery.getId());
        harness.handleMultipleCardsChosen(player1, List.of(sorcery.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Counsel of the Soratami");
        harness.assertInGraveyard(player2, "Holy Day");
    }

    @Test
    void castMustChooseATargetWhenAnEligibleCardExists() {
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new GaleConduitOfTheArcane()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.passBothPriorities();
        harness.assertInHand(player1, "Holy Day");
    }

    @Test
    void enteringWithoutBeingCastDoesNotReturnACard() {
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(new HolyDay()));

        harness.getBattlefieldEntryService().putPermanentOntoBattlefield(
                gd, player1.getId(), new Permanent(new GaleConduitOfTheArcane()));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotInHand(player1, "Holy Day");
        harness.assertInGraveyard(player1, "Holy Day");
    }

    @Test
    void holyFaceCreatesAFlyingPegasusForAnInstant() {
        specialize(0, new HolyDay());
        harness.setHand(player1, List.of(new HolyDay()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Pegasus")).isEqualTo(1);
        Permanent pegasus = findPermanent(player1, "Pegasus");
        assertThat(gqs.getEffectivePower(gd, pegasus)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, pegasus)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, pegasus, Keyword.FLYING)).isTrue();
    }

    @Test
    void temporalFaceDrawsBeforeDiscardingAndCanDiscardTheDrawnCard() {
        specialize(1, new CounselOfTheSoratami());
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new HolyDay()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void abyssalFaceMakesOnlyTheOpponentLoseLifeForASorcery() {
        specialize(2, new Swamp());
        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.setLibrary(player1, List.of(new HolyDay(), new HolyDay()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void primevalFaceCanPutTwoCountersOnAnOpponentsCreature() {
        specialize(4, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HolyDay()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    void specializedFaceDoesNotTriggerForOpponentsSpell() {
        specialize(2, new Swamp());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    void stormFaceAccumulatesBoostsAndAppliesThemToLaterCreatures() {
        specialize(3, new Mountain());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Gale, Storm Conduit")))
                .isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, laterCreature)).isEqualTo(2);
    }

    @Test
    void specializeCannotBeActivatedOutsideAMainPhase() {
        harness.addToBattlefield(player1, new GaleConduitOfTheArcane());
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Mountain");
        harness.assertOnBattlefield(player1, "Gale, Conduit of the Arcane");
    }

    @Test
    void specializedFaceDoesNotTriggerForACreatureSpell() {
        specialize(0, new HolyDay());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(countPermanents(player1, "Pegasus")).isZero();
    }

    private void specialize(int abilityIndex, Card discardedCard) {
        harness.addToBattlefield(player1, new GaleConduitOfTheArcane());
        harness.setHand(player1, List.of(discardedCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, abilityIndex, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
    }
}
