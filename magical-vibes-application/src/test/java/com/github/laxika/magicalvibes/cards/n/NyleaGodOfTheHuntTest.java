package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.t.TravelingPhilosopher;
import com.github.laxika.magicalvibes.cards.v.VoyagingSatyr;
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

@CardUsed({NyleaGodOfTheHunt.class, TravelingPhilosopher.class, VoyagingSatyr.class})
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
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nylea, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Nylea gives a target creature +2/+2 until end of turn")
    void boostsTargetCreatureUntilEndOfTurn() {
        Permanent nylea = addNylea();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
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
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());
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
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Permanent target = nylea;
        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(nylea),
                0,
                null,
                target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Nylea stops being a creature when devotion falls below five")
    void stopsBeingCreatureWhenDevotionFalls() {
        Permanent nylea = addNylea();
        addGreenPermanents(3);
        Permanent satyr = harness.addToBattlefieldAndReturn(player1, new VoyagingSatyr());

        assertThat(gqs.isCreature(gd, nylea)).isTrue();
        assertThat(gqs.hasKeyword(gd, nylea, Keyword.TRAMPLE)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(satyr);

        assertThat(gqs.isCreature(gd, nylea)).isFalse();
        assertThat(gqs.isEnchantment(gd, nylea)).isTrue();
    }

    @Test
    @DisplayName("Nylea does not grant trample to opposing creatures or count their devotion")
    void excludesOpponentCreatures() {
        Permanent nylea = addNylea();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new VoyagingSatyr());
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player2, new VoyagingSatyr());
        }

        assertThat(gqs.isCreature(gd, nylea)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Nylea can boost herself when devotion makes her a creature")
    void canBoostHerselfWhileCreature() {
        Permanent nylea = addNylea();
        addGreenPermanents(4);
        int initialPower = gqs.getEffectivePower(gd, nylea);
        int initialToughness = gqs.getEffectiveToughness(gd, nylea);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(nylea), 0, null, nylea.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, nylea)).isEqualTo(initialPower + 2);
        assertThat(gqs.getEffectiveToughness(gd, nylea)).isEqualTo(initialToughness + 2);
    }

    @Test
    @DisplayName("A boost targeting Nylea does not resolve if she stops being a creature")
    void boostDoesNotResolveAfterTargetLosesCreatureType() {
        Permanent nylea = addNylea();
        addGreenPermanents(3);
        Permanent satyr = harness.addToBattlefieldAndReturn(player1, new VoyagingSatyr());
        int initialPower = gqs.getEffectivePower(gd, nylea);
        int initialToughness = gqs.getEffectiveToughness(gd, nylea);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(nylea), 0, null, nylea.getId());

        gd.playerBattlefields.get(player1.getId()).remove(satyr);
        assertThat(gqs.isCreature(gd, nylea)).isFalse();
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new VoyagingSatyr());

        assertThat(gqs.isCreature(gd, nylea)).isTrue();
        assertThat(gqs.getEffectivePower(gd, nylea)).isEqualTo(initialPower);
        assertThat(gqs.getEffectiveToughness(gd, nylea)).isEqualTo(initialToughness);
    }

    private Permanent addNylea() {
        return harness.addToBattlefieldAndReturn(player1, new NyleaGodOfTheHunt());
    }

    private void addGreenPermanents(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new VoyagingSatyr());
        }
    }
}
