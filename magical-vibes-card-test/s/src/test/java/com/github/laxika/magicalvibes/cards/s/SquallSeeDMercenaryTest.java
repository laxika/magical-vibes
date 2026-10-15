package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SquallSeeDMercenary.class, GrizzlyBears.class, Plains.class, HolyDay.class, StoneGolem.class})
class SquallSeeDMercenaryTest extends BaseCardTest {

    @Test
    @DisplayName("Squall gains double strike when it attacks alone")
    void squallAttackingAloneGainsDoubleStrike() {
        Permanent squall = addCreatureReady(player1, new SquallSeeDMercenary());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        assertThat(gqs.hasKeyword(gd, squall, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Neither creature gains double strike when two creatures attack")
    void multipleAttackersDoNotGainDoubleStrike() {
        Permanent squall = addCreatureReady(player1, new SquallSeeDMercenary());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            resolveAllTriggers();
        });

        assertThat(gqs.hasKeyword(gd, squall, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's creature attacking alone does not gain double strike")
    void opponentsAttackerDoesNotGainDoubleStrike() {
        addCreatureReady(player1, new SquallSeeDMercenary());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(0));
            resolveAllTriggers();
        });

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Combat damage returns a land from your graveyard but cannot target an opponent's card")
    void combatDamageReturnsLandFromOwnGraveyard() {
        Card ownLand = new Plains();
        Card opponentsLand = new Plains();
        harness.setGraveyard(player1, List.of(ownLand));
        harness.setGraveyard(player2, List.of(opponentsLand));
        addCreatureReady(player1, new SquallSeeDMercenary());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(ownLand.getId());

        harness.handleMultipleCardsChosen(player1, List.of(ownLand.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(ownLand.getId()) && !permanent.isTapped());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsLand);
    }

    @Test
    @DisplayName("A creature attacking alone gains double strike until end of turn")
    void creatureAttackingAloneGainsDoubleStrike() {
        Permanent squall = addCreatureReady(player1, new SquallSeeDMercenary());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, squall, Keyword.DOUBLE_STRIKE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Combat damage returns a targeted permanent card with mana value 3 or less")
    void combatDamageReturnsEligiblePermanent() {
        Card legalCreature = new GrizzlyBears();
        Card legalLand = new Plains();
        Card nonPermanent = new HolyDay();
        Card tooExpensive = new StoneGolem();
        harness.setGraveyard(player1, List.of(legalCreature, legalLand, nonPermanent, tooExpensive));
        addCreatureReady(player1, new SquallSeeDMercenary());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(legalCreature.getId(), legalLand.getId());

        harness.handleMultipleCardsChosen(player1, List.of(legalCreature.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(legalCreature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(legalLand, nonPermanent, tooExpensive);
    }

    @Test
    @DisplayName("Combat damage does not return an ineligible graveyard card")
    void noEligiblePermanentDoesNotPrompt() {
        Card nonPermanent = new HolyDay();
        Card tooExpensive = new StoneGolem();
        harness.setGraveyard(player1, List.of(nonPermanent, tooExpensive));
        addCreatureReady(player1, new SquallSeeDMercenary());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonPermanent, tooExpensive);
    }
}
