package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BruseTarlBoorishHerder.class, GrizzlyBears.class})
class BruseTarlBoorishHerderTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and grants target creature double strike and lifelink")
    void entersAndGrantsKeywords() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BruseTarlBoorishHerder()));
        addBruseMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(target.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Attack trigger grants both keywords only to a creature you control")
    void attackGrantsKeywordsToOwnCreature() {
        addCreatureReady(player1, new BruseTarlBoorishHerder());
        Permanent ownTarget = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingTarget = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownTarget.getId()).doesNotContain(opposingTarget.getId());

        harness.handlePermanentChosen(player1, ownTarget.getId());
        harness.passBothPriorities();

        assertThat(ownTarget.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(ownTarget.hasKeyword(Keyword.LIFELINK)).isTrue();
        assertThat(opposingTarget.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(opposingTarget.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Granted keywords wear off at end of turn")
    void grantedKeywordsWearOffAtEndOfTurn() {
        addCreatureReady(player1, new BruseTarlBoorishHerder());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(target.hasKeyword(Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(target.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    private void addBruseMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
