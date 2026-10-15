package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.n.NervousGardener;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HedgeWhisperer.class, Forest.class, NervousGardener.class, Murder.class})
class HedgeWhispererTest extends BaseCardTest {

    @Test
    void collectingEvidenceAnimatesAControlledLandWhileHedgeWhispererRemainsTapped() {
        Permanent hedgeWhisperer = addReadyHedgeWhisperer();
        Permanent forest = addReadyForest();
        List<Card> evidence = List.of(new NervousGardener(), new NervousGardener());
        harness.setGraveyard(player1, evidence);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.handleMultipleCardsChosen(player1, evidence.stream().map(Card::getId).toList());
        harness.passBothPriorities();

        assertThat(hedgeWhisperer.isTapped()).isTrue();
        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(5);
        assertThat(gqs.getEffectiveColors(gd, forest)).containsExactly(CardColor.GREEN);
        assertThat(gqs.effectiveCreatureSubtypes(gd, forest))
                .contains(CardSubtype.PLANT, CardSubtype.BOAR);
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(evidence);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(5);
    }

    @Test
    void animationEndsWhenHedgeWhispererBecomesUntapped() {
        Permanent hedgeWhisperer = addReadyHedgeWhisperer();
        Permanent forest = addReadyForest();
        List<Card> evidence = List.of(new NervousGardener(), new NervousGardener());
        harness.setGraveyard(player1, evidence);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.handleMultipleCardsChosen(player1, evidence.stream().map(Card::getId).toList());
        harness.passBothPriorities();

        advanceToNextTurnWithMayChoice(player2, true);

        assertThat(hedgeWhisperer.isTapped()).isFalse();
        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isFalse();
        assertThat(gqs.getEffectiveColors(gd, forest)).doesNotContain(CardColor.GREEN);
        assertThat(gqs.effectiveCreatureSubtypes(gd, forest))
                .doesNotContain(CardSubtype.PLANT, CardSubtype.BOAR);
    }

    @Test
    void cannotTargetAnOpponentsLand() {
        addReadyHedgeWhisperer();
        Permanent opponentsForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setGraveyard(player1, List.of(new NervousGardener(), new NervousGardener()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentsForest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land you control");
    }

    @Test
    void decliningUntapKeepsTheLandAnimated() {
        Permanent hedgeWhisperer = addReadyHedgeWhisperer();
        Permanent forest = addReadyForest();
        List<Card> evidence = List.of(new NervousGardener(), new NervousGardener());
        harness.setGraveyard(player1, evidence);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.handleMultipleCardsChosen(player1, evidence.stream().map(Card::getId).toList());
        harness.passBothPriorities();
        advanceToNextTurnWithMayChoice(player2, false);

        assertThat(hedgeWhisperer.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isTrue();
    }

    @Test
    void insufficientEvidenceSelectionDoesNotPayTheCost() {
        Permanent hedgeWhisperer = addReadyHedgeWhisperer();
        Permanent forest = addReadyForest();
        List<Card> evidence = List.of(new NervousGardener(), new NervousGardener());
        harness.setGraveyard(player1, evidence);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, forest.getId());

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(evidence.getFirst().getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(hedgeWhisperer.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(evidence);
        assertThat(gqs.isCreature(gd, forest)).isFalse();
    }

    @Test
    void cannotActivateOutsideAMainPhase() {
        Permanent hedgeWhisperer = addReadyHedgeWhisperer();
        Permanent forest = addReadyForest();
        harness.setGraveyard(player1, List.of(new NervousGardener(), new NervousGardener()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(hedgeWhisperer.isTapped()).isFalse();
    }

    @Test
    void removingTheSourceBeforeResolutionDoesNotAnimateTheLand() {
        Permanent hedgeWhisperer = addReadyHedgeWhisperer();
        Permanent forest = addReadyForest();
        List<Card> evidence = List.of(new NervousGardener(), new NervousGardener());
        harness.setGraveyard(player1, evidence);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.handleMultipleCardsChosen(player1, evidence.stream().map(Card::getId).toList());
        harness.castAndResolveInstant(player2, 0, hedgeWhisperer.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hedge Whisperer");
        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(evidence);
    }

    private Permanent addReadyHedgeWhisperer() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new HedgeWhisperer());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addReadyForest() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new Forest());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void advanceToNextTurnWithMayChoice(Player currentActivePlayer, boolean acceptUntap) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.handleMayAbilityChosen(newActivePlayer, acceptUntap);
    }
}
