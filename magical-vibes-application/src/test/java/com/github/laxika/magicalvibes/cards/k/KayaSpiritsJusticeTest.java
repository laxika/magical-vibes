package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KayaSpiritsJustice.class, Forest.class, GrizzlyBears.class})
class KayaSpiritsJusticeTest extends BaseCardTest {

    @Test
    @DisplayName("+2 surveils, then mandatorily exiles a card from a graveyard")
    void plusTwoExilesARequiredGraveyardCard() {
        Permanent kaya = addReadyKaya(3);
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player2, List.of(forest));

        activate(kaya, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(forest);
    }

    @Test
    @DisplayName("+1 creates a white and black flying Spirit")
    void plusOneCreatesSpirit() {
        Permanent kaya = addReadyKaya(3);

        activate(kaya, 1);
        harness.passBothPriorities();

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(spirit.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(spirit.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(spirit.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("-2 exiles the controlled creature and at most one creature per opponent")
    void minusTwoExilesControlledAndOpponentCreatures() {
        Permanent kaya = addReadyKaya(3);
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        activateWithTargets(kaya, List.of(ownCreature.getId(), opponentCreature.getId()));
        harness.passPriority(player1);
        harness.passPriority(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ownCreature.getCard());
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opponentCreature.getCard());
    }

    @Test
    @DisplayName("The exile trigger copies a chosen creature card onto a controlled token")
    void exileTriggerCopiesCreatureToTokenWithFlying() {
        Permanent kaya = addReadyKaya(3);
        activate(kaya, 1);
        harness.passBothPriorities();
        Permanent token = findPermanent(player1, "Spirit");
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        kaya.setLoyaltyActivationsThisTurn(0);

        activateWithTargets(kaya, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getCard().getId()));
        harness.handlePermanentChosen(player1, token.getId());
        harness.passBothPriorities();

        assertThat(token.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    private Permanent addReadyKaya(int loyalty) {
        Permanent kaya = new Permanent(new KayaSpiritsJustice());
        kaya.setCounterCount(CounterType.LOYALTY, loyalty);
        kaya.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(kaya);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return kaya;
    }

    private void activate(Permanent kaya, int abilityIndex) {
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(kaya), abilityIndex,
                null, null);
    }

    private void activateWithTargets(Permanent kaya, List<java.util.UUID> targetIds) {
        harness.activateAbilityWithMultiTargets(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(kaya), 2, targetIds);
    }
}
