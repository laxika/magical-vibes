package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JolraelVoiceOfZhalfir.class, Forest.class, GrizzlyBears.class})
class JolraelVoiceOfZhalfirTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of combat, a land you control becomes a Bird based on your hand size")
    void animatesTargetLandBasedOnHandSize() {
        addCreatureReady(player1, new JolraelVoiceOfZhalfir());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest()));

        advanceToCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(forest.getId(), player1.getId());

        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(3);
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(forest.getCard().hasType(CardType.LAND)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, forest))
                .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.BLUE);
        assertThat(gqs.hasKeyword(gd, forest, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The beginning-of-combat target is limited to a land you control")
    void onlyTargetsOwnLands() {
        addCreatureReady(player1, new JolraelVoiceOfZhalfir());
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());

        advanceToCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownForest.getId(), player1.getId())
                .doesNotContain(ownBear.getId(), opponentForest.getId());
    }

    @Test
    @DisplayName("The land animation ends at the end of the turn")
    void animationEndsAtEndOfTurn() {
        addCreatureReady(player1, new JolraelVoiceOfZhalfir());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new Forest()));

        advanceToCombat();
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        forest.resetModifiers();

        assertThat(forest.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, forest)).isFalse();
    }

    @Test
    @DisplayName("Combat damage from an animated land draws a card")
    void animatedLandCombatDamageDrawsCard() {
        addCreatureReady(player1, new JolraelVoiceOfZhalfir());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new Forest()));
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));

        advanceToCombat();
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(1));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    private void advanceToCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
