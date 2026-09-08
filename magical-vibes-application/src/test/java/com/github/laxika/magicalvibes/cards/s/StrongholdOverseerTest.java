package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(StrongholdOverseer.class)
class StrongholdOverseerTest extends BaseCardTest {

    @Test
    @DisplayName("The ability boosts creatures with shadow and weakens creatures without shadow")
    void boostsShadowAndWeakensNonShadowCreatures() {
        Permanent source = addCreatureReady(player1, new StrongholdOverseer());
        Permanent ownShadow = harness.addToBattlefieldAndReturn(player1, shadowCreature(2, 2));
        Permanent opponentShadow = harness.addToBattlefieldAndReturn(player2, shadowCreature(3, 3));
        Permanent ownNonShadow = harness.addToBattlefieldAndReturn(player1, creature(4, 4));
        Permanent opponentNonShadow = harness.addToBattlefieldAndReturn(player2, creature(5, 5));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, ownShadow)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentShadow)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, ownNonShadow)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentNonShadow)).isEqualTo(4);
    }

    @Test
    @DisplayName("The ability's power changes last only until end of turn")
    void powerChangesWearOffAtEndOfTurn() {
        Permanent source = addCreatureReady(player1, new StrongholdOverseer());
        Permanent nonShadow = harness.addToBattlefieldAndReturn(player2, creature(4, 4));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, nonShadow)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, nonShadow)).isEqualTo(4);
    }

    private Card shadowCreature(int power, int toughness) {
        Card card = creature(power, toughness);
        card.setKeywords(Set.of(Keyword.SHADOW));
        return card;
    }

    private Card creature(int power, int toughness) {
        Card card = new Card();
        card.setName("Test Creature");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.BLACK);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
