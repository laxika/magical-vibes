package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KirdChieftainTest extends BaseCardTest {

    @Test
    @DisplayName("Is 4/4 while its controller controls a Forest")
    void boostedWithForest() {
        harness.addToBattlefield(player1, new KirdChieftain());
        harness.addToBattlefield(player1, new Forest());

        Permanent chieftain = findPermanent(player1, "Kird Chieftain");
        assertThat(gqs.getEffectivePower(gd, chieftain)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, chieftain)).isEqualTo(4);
    }

    @Test
    @DisplayName("Is 3/3 with no Forest")
    void noBoostWithoutForest() {
        harness.addToBattlefield(player1, new KirdChieftain());
        harness.addToBattlefield(player1, new Island());

        Permanent chieftain = findPermanent(player1, "Kird Chieftain");
        assertThat(gqs.getEffectivePower(gd, chieftain)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, chieftain)).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's Forest does not grant the boost")
    void opponentForestDoesNotCount() {
        harness.addToBattlefield(player1, new KirdChieftain());
        harness.addToBattlefield(player2, new Forest());

        Permanent chieftain = findPermanent(player1, "Kird Chieftain");
        assertThat(gqs.getEffectivePower(gd, chieftain)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, chieftain)).isEqualTo(3);
    }

    @Test
    @DisplayName("Loses the boost as soon as the Forest leaves the battlefield")
    void losesBoostWhenForestLeaves() {
        harness.addToBattlefield(player1, new KirdChieftain());
        harness.addToBattlefield(player1, new Forest());

        Permanent chieftain = findPermanent(player1, "Kird Chieftain");
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
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        Permanent target = findPermanent(player1, "Grizzly Bears");
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
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        Permanent target = findPermanent(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Ability can target a creature an opponent controls")
    void abilityCanTargetOpponentCreature() {
        harness.addToBattlefield(player1, new KirdChieftain());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        Permanent target = findPermanent(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }
}
