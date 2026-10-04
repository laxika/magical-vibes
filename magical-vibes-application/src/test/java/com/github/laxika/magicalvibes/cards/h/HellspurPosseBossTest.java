package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.s.SterlingHound;
import com.github.laxika.magicalvibes.cards.v.VaultPlunderer;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HellspurPosseBoss.class, VaultPlunderer.class, SterlingHound.class})
class HellspurPosseBossTest extends BaseCardTest {

    @Test
    @DisplayName("Other outlaws you control have haste")
    void grantsHasteToOtherOutlawsYouControl() {
        Permanent outlaw = harness.addToBattlefieldAndReturn(player1, new VaultPlunderer());
        Permanent boss = harness.addToBattlefieldAndReturn(player1, new HellspurPosseBoss());

        assertThat(gqs.hasKeyword(gd, outlaw, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, boss, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Haste is not granted to non-outlaws or opposing outlaws")
    void restrictsHasteToOtherOutlawsYouControl() {
        Permanent opposingOutlaw = harness.addToBattlefieldAndReturn(player2, new VaultPlunderer());
        Permanent nonOutlaw = harness.addToBattlefieldAndReturn(player1, new SterlingHound());
        harness.addToBattlefield(player1, new HellspurPosseBoss());

        assertThat(gqs.hasKeyword(gd, opposingOutlaw, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, nonOutlaw, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Entering the battlefield creates two Mercenary tokens")
    void createsTwoMercenaryTokens() {
        castBoss();

        List<Permanent> mercenaries = findPermanents(player1, "Mercenary");
        assertThat(mercenaries).hasSize(2);
        assertThat(mercenaries.getFirst().getCard().isToken()).isTrue();
        assertThat(mercenaries.getLast().getCard().isToken()).isTrue();
        assertThat(gqs.hasKeyword(gd, mercenaries.getFirst(), Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Mercenary tokens can boost a creature you control at sorcery speed")
    void mercenaryTokenBoostsCreatureYouControl() {
        Permanent bear = addCreatureReady(player1, new SterlingHound());
        castBoss();
        Permanent mercenary = findPermanents(player1, "Mercenary").getFirst();
        mercenary.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int mercenaryIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);
        harness.activateAbility(player1, mercenaryIndex, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(mercenary.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mercenary tokens cannot activate their ability outside sorcery speed")
    void mercenaryTokenRequiresSorcerySpeed() {
        Permanent bear = addCreatureReady(player1, new SterlingHound());
        castBoss();
        Permanent mercenary = findPermanents(player1, "Mercenary").getFirst();
        mercenary.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        int mercenaryIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);

        assertThatThrownBy(() -> harness.activateAbility(player1, mercenaryIndex, 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void newMercenaryCanTapImmediatelyAndBoostExpiresAtEndOfTurn() {
        castBoss();
        Permanent boss = findPermanent(player1, "Hellspur Posse Boss");
        Permanent mercenary = findPermanents(player1, "Mercenary").getFirst();

        assertThat(mercenary.isSummoningSick()).isTrue();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mercenary),
                0, null, boss.getId());
        resolveAllTriggers();

        assertThat(mercenary.isTapped()).isTrue();
        assertThat(boss.getPowerModifier()).isEqualTo(1);
        assertThat(boss.getToughnessModifier()).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(boss.getPowerModifier()).isZero();
    }

    @Test
    void mercenaryCannotTargetOpponentsCreature() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new SterlingHound());
        castBoss();
        Permanent mercenary = findPermanents(player1, "Mercenary").getFirst();

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mercenary), 0, null, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mercenary.isTapped()).isFalse();
        assertThat(opponent.getPowerModifier()).isZero();
    }

    @Test
    void mercenaryCannotActivateWhileAnotherAbilityIsOnStack() {
        castBoss();
        Permanent boss = findPermanent(player1, "Hellspur Posse Boss");
        List<Permanent> mercenaries = findPermanents(player1, "Mercenary");
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mercenaries.getFirst()),
                0, null, boss.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mercenaries.getLast()), 0, null, boss.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        assertThat(mercenaries.getLast().isTapped()).isFalse();
        resolveAllTriggers();
    }

    @Test
    void newMercenaryLosesAbilityToTapWhenBossLeaves() {
        castBoss();
        Permanent boss = findPermanent(player1, "Hellspur Posse Boss");
        List<Permanent> mercenaries = findPermanents(player1, "Mercenary");
        gd.playerBattlefields.get(player1.getId()).remove(boss);

        assertThat(gqs.hasKeyword(gd, mercenaries.getFirst(), Keyword.HASTE)).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mercenaries.getFirst()),
                0, null, mercenaries.getLast().getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mercenaries.getFirst().isTapped()).isFalse();
    }

    @Test
    void mercenaryCannotActivateDuringOpponentsMainPhase() {
        castBoss();
        Permanent mercenary = findPermanents(player1, "Mercenary").getFirst();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mercenary),
                0, null, mercenary.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void entryTriggerCreatesTokensEvenWhenBossLeavesBeforeResolution() {
        harness.setHand(player1, List.of(new HellspurPosseBoss()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent boss = findPermanent(player1, "Hellspur Posse Boss");
        gd.playerBattlefields.get(player1.getId()).remove(boss);
        resolveAllTriggers();

        List<Permanent> mercenaries = findPermanents(player1, "Mercenary");
        assertThat(mercenaries).hasSize(2);
        assertThat(gqs.hasKeyword(gd, mercenaries.getFirst(), Keyword.HASTE)).isFalse();
    }

    @Test
    void twoBossesGiveEachOtherHaste() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new HellspurPosseBoss());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new HellspurPosseBoss());

        assertThat(gqs.hasKeyword(gd, first, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.HASTE)).isTrue();
    }

    private void castBoss() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new HellspurPosseBoss()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
