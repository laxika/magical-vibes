package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BelovedPrincess;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrashingDrawbridge.class, BelovedPrincess.class})
class CrashingDrawbridgeTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability grants haste to creatures you control")
    void grantsHasteToOwnCreatures() {
        Permanent drawbridge = addCreatureReady(player1, new CrashingDrawbridge());
        Permanent ownBears = addCreatureReady(player1, new BelovedPrincess());
        Permanent opponentBears = addCreatureReady(player2, new BelovedPrincess());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(drawbridge), null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, drawbridge, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownBears, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Granted haste wears off at end of turn")
    void hasteWearsOffAtEndOfTurn() {
        Permanent drawbridge = addCreatureReady(player1, new CrashingDrawbridge());
        Permanent ownBears = addCreatureReady(player1, new BelovedPrincess());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(drawbridge), null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, ownBears, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, drawbridge, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownBears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering before resolution gain haste, but later creatures do not")
    void selectsCreaturesAtResolution() {
        Permanent drawbridge = addCreatureReady(player1, new CrashingDrawbridge());
        harness.activateAbility(player1, 0, null, null);

        assertThat(drawbridge.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, drawbridge, Keyword.HASTE)).isFalse();
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new BelovedPrincess());
        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new BelovedPrincess());

        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("A summoning sick Drawbridge cannot activate its tap ability")
    void cannotActivateWhileSummoningSick() {
        Permanent drawbridge = harness.addToBattlefieldAndReturn(player1, new CrashingDrawbridge());
        drawbridge.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(drawbridge.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Drawbridge cannot activate again")
    void cannotActivateWhileTapped() {
        Permanent drawbridge = addCreatureReady(player1, new CrashingDrawbridge());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(drawbridge.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The activated ability resolves after Drawbridge leaves the battlefield")
    void resolvesWithoutSource() {
        Permanent drawbridge = addCreatureReady(player1, new CrashingDrawbridge());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BelovedPrincess());
        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(drawbridge);
        gd.playerGraveyards.get(player1.getId()).add(drawbridge.getCard());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Granted haste lets a newly entered Drawbridge activate its tap ability")
    void hasteEnablesSummoningSickDrawbridge() {
        addCreatureReady(player1, new CrashingDrawbridge());
        Permanent newDrawbridge = harness.addToBattlefieldAndReturn(player1, new CrashingDrawbridge());
        newDrawbridge.setSummoningSick(true);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new BelovedPrincess());

        harness.activateAbility(player1, 1, null, null);
        assertThat(newDrawbridge.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.HASTE)).isTrue();
    }
}
