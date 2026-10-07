package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ExpeditionRaptor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpawnbinderMage.class, ExpeditionRaptor.class})
class SpawnbinderMageTest extends BaseCardTest {

    @Test
    @DisplayName("Cohort taps an Ally and the target creature")
    void cohortTapsAllyAndTargetCreature() {
        Permanent mage = addCreatureReady(player1, new SpawnbinderMage());
        Permanent ally = addCreatureReady(player1, new SpawnbinderMage());
        Permanent target = addCreatureReady(player2, new ExpeditionRaptor());

        harness.activateAbility(player1, battlefieldIndex(mage), 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(mage.isTapped()).isTrue();
        assertThat(ally.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cohort cannot be activated without another untapped Ally")
    void cannotActivateWithoutAnotherUntappedAlly() {
        Permanent mage = addCreatureReady(player1, new SpawnbinderMage());
        Permanent nonAlly = addCreatureReady(player1, new ExpeditionRaptor());
        Permanent target = addCreatureReady(player2, new ExpeditionRaptor());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(mage), 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
        assertThat(mage.isTapped()).isFalse();
        assertThat(nonAlly.isTapped()).isFalse();
    }

    @Test
    void summoningSickAllyCanPayCostBeforeTargetIsTapped() {
        Permanent mage = addCreatureReady(player1, new SpawnbinderMage());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new SpawnbinderMage());
        ally.setSummoningSick(true);
        Permanent target = addCreatureReady(player2, new ExpeditionRaptor());

        harness.activateAbility(player1, battlefieldIndex(mage), 0, null, target.getId());

        assertThat(mage.isTapped()).isTrue();
        assertThat(ally.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void summoningSickMageCannotActivate() {
        Permanent mage = harness.addToBattlefieldAndReturn(player1, new SpawnbinderMage());
        mage.setSummoningSick(true);
        Permanent ally = addCreatureReady(player1, new SpawnbinderMage());
        Permanent target = addCreatureReady(player2, new ExpeditionRaptor());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(mage), 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(mage.isTapped()).isFalse();
        assertThat(ally.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void tappedAllyCannotPayCost() {
        Permanent mage = addCreatureReady(player1, new SpawnbinderMage());
        Permanent ally = addCreatureReady(player1, new SpawnbinderMage());
        ally.tap();
        Permanent target = addCreatureReady(player2, new ExpeditionRaptor());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(mage), 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");

        assertThat(mage.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void opponentsAllyCannotPayCost() {
        Permanent mage = addCreatureReady(player1, new SpawnbinderMage());
        Permanent opposingAlly = addCreatureReady(player2, new SpawnbinderMage());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(mage), 0, null, opposingAlly.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");

        assertThat(mage.isTapped()).isFalse();
        assertThat(opposingAlly.isTapped()).isFalse();
    }

    @Test
    void canTargetOwnAlreadyTappedCreature() {
        Permanent mage = addCreatureReady(player1, new SpawnbinderMage());
        Permanent ally = addCreatureReady(player1, new SpawnbinderMage());
        Permanent target = addCreatureReady(player1, new ExpeditionRaptor());
        target.tap();

        harness.activateAbility(player1, battlefieldIndex(mage), 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(mage.isTapped()).isTrue();
        assertThat(ally.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
