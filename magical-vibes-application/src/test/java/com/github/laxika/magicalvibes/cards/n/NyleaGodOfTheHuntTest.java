package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NyleaGodOfTheHuntTest extends BaseCardTest {

    @Test
    @DisplayName("Nylea is not a creature below five devotion to green")
    void isNotCreatureBelowDevotionThreshold() {
        Permanent nylea = addNylea();
        addGreenPermanents(3);

        assertThat(gqs.isCreature(gd, nylea)).isFalse();
        assertThat(gqs.isEnchantment(gd, nylea)).isTrue();
    }

    @Test
    @DisplayName("Nylea becomes a creature at five devotion to green")
    void becomesCreatureAtDevotionThreshold() {
        Permanent nylea = addNylea();
        addGreenPermanents(4);

        assertThat(gqs.isCreature(gd, nylea)).isTrue();
    }

    @Test
    @DisplayName("Other creatures you control have trample")
    void grantsTrampleToOtherCreatures() {
        Permanent nylea = addNylea();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nylea, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Nylea gives a target creature +2/+2 until end of turn")
    void boostsTargetCreatureUntilEndOfTurn() {
        Permanent nylea = addNylea();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(nylea), 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Nylea can target an opponent's creature")
    void canTargetOpponentCreature() {
        Permanent nylea = addNylea();
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(nylea), 0, null, opponentBears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Nylea cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent nylea = addNylea();
        harness.addToBattlefield(player1, new NyleaGodOfTheHunt());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Permanent target = gd.playerBattlefields.get(player1.getId()).get(1);
        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(nylea),
                0,
                null,
                target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addNylea() {
        return harness.addToBattlefieldAndReturn(player1, new NyleaGodOfTheHunt());
    }

    private void addGreenPermanents(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new LlanowarElves());
        }
    }
}
