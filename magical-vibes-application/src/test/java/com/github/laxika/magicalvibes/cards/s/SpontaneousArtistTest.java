package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpontaneousArtistTest extends BaseCardTest {

    @Test
    void entersWithOneEnergyCounter() {
        harness.setHand(player1, List.of(new SpontaneousArtist()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void paysEnergyToGiveAnyTargetCreatureHaste() {
        addReadyArtist(player1);
        Permanent target = addReadyCreature(player2);
        gd.playerEnergyCounters.put(player1.getId(), 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    void hasteIsRemovedAtEndOfTurn() {
        addReadyArtist(player1);
        Permanent target = addReadyCreature(player1);
        gd.playerEnergyCounters.put(player1.getId(), 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    void cannotActivateWithoutEnergy() {
        addReadyArtist(player1);
        Permanent target = addReadyCreature(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one energy counter");
    }

    private Permanent addReadyArtist(Player player) {
        Permanent permanent = new Permanent(new SpontaneousArtist());
        permanent.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent permanent = new Permanent(new FugitiveWizard());
        permanent.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
