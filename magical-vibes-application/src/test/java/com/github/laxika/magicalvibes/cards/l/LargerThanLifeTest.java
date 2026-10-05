package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.s.ServantOfTheConduit;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LargerThanLife.class, ServantOfTheConduit.class})
class LargerThanLifeTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving gives +4/+4 and trample to the target creature")
    void resolvingBoostsAndGrantsTrample() {
        castLargerThanLifeOnCreature();

        Permanent creature = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(creature.getPowerModifier()).isEqualTo(4);
        assertThat(creature.getToughnessModifier()).isEqualTo(4);
        assertThat(creature.getEffectivePower()).isEqualTo(6);
        assertThat(creature.getEffectiveToughness()).isEqualTo(6);
        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Boost and trample wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        castLargerThanLifeOnCreature();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent creature = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Can target an opponent's creature without affecting other creatures")
    void canTargetOpponentsCreature() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new ServantOfTheConduit());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ServantOfTheConduit());
        harness.setHand(player1, List.of(new LargerThanLife()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(6);
        assertThat(target.getEffectiveToughness()).isEqualTo(6);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(own.getPowerModifier()).isZero();
        assertThat(own.getToughnessModifier()).isZero();
        assertThat(own.hasKeyword(Keyword.TRAMPLE)).isFalse();
        harness.assertInGraveyard(player1, "Larger Than Life");
    }

    @Test
    @DisplayName("Repeated casts stack their boosts and both expire")
    void repeatedCastsStack() {
        castLargerThanLifeOnCreature();
        Permanent target = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new LargerThanLife()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(10);
        assertThat(target.getEffectiveToughness()).isEqualTo(10);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("A target leaving before resolution does not transfer effects to another creature")
    void removedTargetDoesNotAffectOtherCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ServantOfTheConduit());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new ServantOfTheConduit());
        harness.setHand(player1, List.of(new LargerThanLife()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(other.hasKeyword(Keyword.TRAMPLE)).isFalse();
        harness.assertInGraveyard(player1, "Larger Than Life");
    }

    private void castLargerThanLifeOnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ServantOfTheConduit());
        harness.setHand(player1, List.of(new LargerThanLife()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
