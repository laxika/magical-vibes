package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mortivore;
import com.github.laxika.magicalvibes.cards.n.NorwoodRanger;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LegionReconsecrator.class, GrizzlyBears.class, SavannahLions.class, WrathOfGod.class,
        Mortivore.class, NorwoodRanger.class})
class LegionReconsecratorTest extends BaseCardTest {

    @Test
    void attackExilesCreatureAndConjuresModifiedDuplicateIntoControllersGraveyard() {
        addReadyLegion();
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getId()));
        Card duplicate = gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Grizzly Bears"))
                .findFirst()
                .orElseThrow();
        assertThat(duplicate.getId()).isNotEqualTo(target.getId());
        assertThat(duplicate.getColors()).contains(CardColor.GREEN, CardColor.BLACK);
        assertThat(duplicate.getSubtypes()).contains(CardSubtype.SKELETON);
        assertThat(duplicate.getPower()).isEqualTo(3);
        assertThat(duplicate.getToughness()).isEqualTo(1);
    }

    @Test
    void attackMayChooseNoCreatureCard() {
        addReadyLegion();
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void deathReturnsAnotherCreatureWithPowerOrToughnessExactlyOne() {
        addReadyLegion();
        Card validTarget = new SavannahLions();
        Card invalidTarget = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(validTarget, invalidTarget));

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(validTarget.getId());
        assertThat(choice.validCardIds()).doesNotContain(invalidTarget.getId());

        harness.handleMultipleCardsChosen(player1, List.of(validTarget.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Savannah Lions");
    }

    @Test
    void deathCanReturnCreatureWithPowerOneAndToughnessGreaterThanOneOnlyFromYourGraveyard() {
        addReadyLegion();
        Card target = new NorwoodRanger();
        Card opponentsCard = new SavannahLions();
        harness.setGraveyard(player1, List.of(target));
        harness.setGraveyard(player2, List.of(opponentsCard));

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Norwood Ranger");
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsCard);
    }

    @Test
    void attackCanExileFromControllersGraveyardAndIgnoresNoncreatureCards() {
        addReadyLegion();
        Card target = new GrizzlyBears();
        Card noncreature = new WrathOfGod();
        harness.setGraveyard(player1, List.of(target, noncreature));

        declareAttackers(List.of(0));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(target.getId()).doesNotContain(noncreature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2).contains(noncreature)
                .doesNotContain(target);
    }

    @Test
    void attackDoesNotConjureWhenTargetLeavesGraveyardBeforeResolution() {
        addReadyLegion();
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player2, List.of());
        harness.setExile(player2, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
    }

    @Test
    void duplicateBasePowerAndToughnessOverrideCharacteristicDefiningAbilityInGraveyard() {
        addReadyLegion();
        Card target = new Mortivore();
        harness.setGraveyard(player2, List.of(target, new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        Card duplicate = gd.playerGraveyards.get(player1.getId()).getFirst();
        assertThat(harness.getGameQueryService().getEffectiveCardPower(gd, duplicate)).isEqualTo(3);
        assertThat(harness.getGameQueryService().getEffectiveCardToughness(gd, duplicate)).isEqualTo(1);
    }

    @Test
    void deathCanReturnDuplicateWithCharacteristicDefiningAbilityAndPreservesItsBaseStats() {
        addReadyLegion();
        Card target = new Mortivore();
        harness.setGraveyard(player2, List.of(target, new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        Card duplicate = gd.playerGraveyards.get(player1.getId()).getFirst();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(duplicate.getId());
        harness.handleMultipleCardsChosen(player1, List.of(duplicate.getId()));
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(duplicate.getId()))
                .findFirst().orElseThrow();
        assertThat(harness.getGameQueryService().getEffectivePower(gd, returned)).isEqualTo(3);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, returned)).isEqualTo(1);
        assertThat(returned.getCard().getSubtypes()).contains(CardSubtype.LHURGOYF, CardSubtype.SKELETON);
    }

    private void addReadyLegion() {
        Permanent legion = harness.addToBattlefieldAndReturn(player1, new LegionReconsecrator());
        legion.setSummoningSick(false);
    }
}
