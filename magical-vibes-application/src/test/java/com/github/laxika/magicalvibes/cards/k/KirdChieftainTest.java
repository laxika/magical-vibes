package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KirdChieftain.class, Forest.class, Island.class, RuneclawBear.class})
class KirdChieftainTest extends BaseCardTest {

    @Test
    @DisplayName("Is 4/4 while its controller controls a Forest")
    void boostedWithForest() {
        Permanent chieftain = harness.addToBattlefieldAndReturn(player1, new KirdChieftain());
        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, chieftain)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, chieftain)).isEqualTo(4);
    }

    @Test
    @DisplayName("Is 3/3 with no Forest")
    void noBoostWithoutForest() {
        Permanent chieftain = harness.addToBattlefieldAndReturn(player1, new KirdChieftain());
        harness.addToBattlefield(player1, new Island());

        assertThat(gqs.getEffectivePower(gd, chieftain)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, chieftain)).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's Forest does not grant the boost")
    void opponentForestDoesNotCount() {
        Permanent chieftain = harness.addToBattlefieldAndReturn(player1, new KirdChieftain());
        harness.addToBattlefield(player2, new Forest());

        assertThat(gqs.getEffectivePower(gd, chieftain)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, chieftain)).isEqualTo(3);
    }

    @Test
    @DisplayName("Loses the boost as soon as the Forest leaves the battlefield")
    void losesBoostWhenForestLeaves() {
        Permanent chieftain = harness.addToBattlefieldAndReturn(player1, new KirdChieftain());
        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, chieftain)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Forest"));

        assertThat(gqs.getEffectivePower(gd, chieftain)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, chieftain)).isEqualTo(3);
    }

    @Test
    @DisplayName("Ability gives target creature +2/+2 and trample")
    void abilityPumpsAndGrantsTrample() {
        harness.addToBattlefield(player1, new KirdChieftain());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Ability boost and trample wear off at end of turn")
    void abilityWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new KirdChieftain());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Multiple Forests grant only one +1/+1 bonus")
    void multipleForestsDoNotStack() {
        Permanent chieftain = harness.addToBattlefieldAndReturn(player1, new KirdChieftain());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, chieftain)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, chieftain)).isEqualTo(4);
    }

    @Test
    @DisplayName("Can activate twice while summoning sick and target itself without a Forest")
    void repeatedSelfActivationsStackWithoutForest() {
        Permanent chieftain = harness.addToBattlefieldAndReturn(player1, new KirdChieftain());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, chieftain.getId());
        harness.activateAbility(player1, 0, null, chieftain.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, chieftain)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, chieftain)).isEqualTo(7);
        assertThat(chieftain.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(chieftain.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Activated ability resolves after its source leaves the battlefield")
    void abilitySurvivesSourceLeaving() {
        Permanent chieftain = harness.addToBattlefieldAndReturn(player1, new KirdChieftain());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(chieftain);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Five generic mana cannot replace the green activation cost")
    void activationRequiresGreenMana() {
        Permanent chieftain = harness.addToBattlefieldAndReturn(player1, new KirdChieftain());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, chieftain.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, chieftain)).isEqualTo(3);
        assertThat(chieftain.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Ability cannot target a noncreature Forest")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new KirdChieftain());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability can target a creature an opponent controls")
    void abilityCanTargetOpponentCreature() {
        harness.addToBattlefield(player1, new KirdChieftain());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }
}
