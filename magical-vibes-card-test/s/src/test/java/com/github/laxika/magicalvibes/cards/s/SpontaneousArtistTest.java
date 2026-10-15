package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.t.TerrorOfTheFairgrounds;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpontaneousArtist.class, TerrorOfTheFairgrounds.class})
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

    @Test
    void enteringAddsEnergyToExistingCountersOnlyForItsController() {
        gd.playerEnergyCounters.put(player1.getId(), 3);
        gd.playerEnergyCounters.put(player2.getId(), 2);
        harness.setHand(player1, List.of(new SpontaneousArtist()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(4);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(2);
    }

    @Test
    void tappedSummoningSickArtistCanGiveItselfHaste() {
        Permanent artist = harness.addToBattlefieldAndReturn(player1, new SpontaneousArtist());
        artist.setSummoningSick(true);
        artist.tap();
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, null, artist.getId());

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(artist.hasKeyword(Keyword.HASTE)).isFalse();
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(artist.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(artist.isTapped()).isTrue();
    }

    @Test
    void abilityResolvesAfterArtistLeavesBattlefield() {
        Permanent artist = addReadyArtist(player1);
        Permanent target = addCreatureReady(player2, new SpontaneousArtist());
        gd.playerEnergyCounters.put(player1.getId(), 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(artist);
        gd.playerGraveyards.get(player1.getId()).add(artist.getCard());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
    }

    @Test
    void energyIsNotRefundedWhenTargetLeavesBeforeResolution() {
        addReadyArtist(player1);
        Permanent target = addCreatureReady(player2, new SpontaneousArtist());
        gd.playerEnergyCounters.put(player1.getId(), 1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyArtist(Player player) {
        return addCreatureReady(player, new SpontaneousArtist());
    }

    private Permanent addReadyCreature(Player player) {
        return addCreatureReady(player, new TerrorOfTheFairgrounds());
    }
}
