package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MyFollowersAscend.class, GrizzlyBears.class})
class MyFollowersAscendTest extends BaseCardTest {

    @Test
    void putsCountersAndTemporaryKeywordsOnAControlledCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        resolveScheme();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void createsAVigilantColorlessScarecrowWhenYouControlNoCreature() {
        addCreatureReady(player2, new GrizzlyBears());

        resolveScheme();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent token = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
        assertThat(gqs.isArtifact(gd, token)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, token))
                .contains(CardSubtype.SCARECROW);
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void chosenCreatureReceivesAllBenefitsAndOtherCreaturesReceiveNone() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent chosen = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());

        resolveScheme();
        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId()));

        assertThat(chosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, chosen, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, chosen, Keyword.VIGILANCE)).isTrue();
        for (Permanent other : List.of(first, opponent)) {
            assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
            assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
            assertThat(gqs.hasKeyword(gd, other, Keyword.VIGILANCE)).isFalse();
        }
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(first, chosen);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void createsTokenWhenLastCreatureLeavesBeforeResolution() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        queueScheme();
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent token = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, token)).isEmpty();
        assertThat(gqs.isCreature(gd, token)).isTrue();
        assertThat(gqs.isArtifact(gd, token)).isTrue();
        assertThat(token.isTapped()).isFalse();
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void benefitsCreatureThatEntersBeforeResolutionInsteadOfCreatingToken() {
        queueScheme();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(creature);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    private void resolveScheme() {
        queueScheme();
        harness.passBothPriorities();
    }

    private void queueScheme() {
        Card scheme = new MyFollowersAscend();
        gd.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                scheme,
                player1.getId(),
                scheme.getName(),
                scheme.getEffects(EffectSlot.SPELL)));
    }
}
