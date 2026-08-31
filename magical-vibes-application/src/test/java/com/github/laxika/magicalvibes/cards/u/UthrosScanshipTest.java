package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UthrosScanship.class, Forest.class, GrizzlyBears.class, Island.class})
class UthrosScanshipTest extends BaseCardTest {

    @Test
    @DisplayName("Entering draws two cards, then makes its controller discard a card")
    void entersDrawsTwoThenDiscards() {
        GrizzlyBears discarded = new GrizzlyBears();
        Forest firstDraw = new Forest();
        Island secondDraw = new Island();
        harness.setHand(player1, new ArrayList<>(List.of(new UthrosScanship(), discarded)));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        addScanshipMana();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        int discardedIndex = gd.playerHands.get(player1.getId()).indexOf(discarded);
        harness.handleCardChosen(player1, discardedIndex);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).contains(firstDraw, secondDraw);
    }

    @Test
    @DisplayName("Station uses the tapped creature's power and grants flying at eight charge counters")
    void stationUsesTappedCreaturePowerAndUnlocksFlying() {
        Permanent scanship = harness.addToBattlefieldAndReturn(player1, new UthrosScanship());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(scanship), null, null);
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(scanship.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(gqs.isCreature(gd, scanship)).isFalse();
        assertThat(gqs.hasKeyword(gd, scanship, Keyword.FLYING)).isFalse();

        scanship.setCounterCount(CounterType.CHARGE, 8);

        assertThat(gqs.isCreature(gd, scanship)).isTrue();
        assertThat(gqs.hasKeyword(gd, scanship, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Station requires another untapped creature")
    void stationRequiresAnotherUntappedCreature() {
        Permanent scanship = harness.addToBattlefieldAndReturn(player1, new UthrosScanship());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(scanship), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addScanshipMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
