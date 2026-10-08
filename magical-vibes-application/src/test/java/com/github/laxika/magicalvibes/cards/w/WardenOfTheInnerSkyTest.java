package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IzzetCluestone;
import com.github.laxika.magicalvibes.cards.m.MarketGnome;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WardenOfTheInnerSky.class, GrizzlyBears.class, IzzetCluestone.class, MarketGnome.class})
class WardenOfTheInnerSkyTest extends BaseCardTest {

    @Test
    @DisplayName("Three counters of any types grant flying and vigilance")
    void threeCountersGrantKeywords() {
        Permanent warden = addWarden();

        assertThat(gqs.hasKeyword(gd, warden, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, warden, Keyword.VIGILANCE)).isFalse();

        warden.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        assertThat(gqs.hasKeyword(gd, warden, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, warden, Keyword.VIGILANCE)).isFalse();

        warden.setCounterCount(CounterType.OIL, 1);

        assertThat(gqs.hasKeyword(gd, warden, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, warden, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Tapping three artifacts and/or creatures adds a counter and scries")
    void tapsThreePermanentsAddsCounterAndScries() {
        Permanent warden = addWarden();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IzzetCluestone());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(warden.isTapped()).isTrue();
        assertThat(artifact.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(warden.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("The activated ability requires sorcery timing")
    void activationRequiresSorceryTiming() {
        addWarden();
        harness.addToBattlefield(player1, new IzzetCluestone());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void losesKeywordsWhenCounterTotalDropsBelowThree() {
        Permanent warden = addWarden();
        warden.setCounterCount(CounterType.OIL, 3);

        assertThat(gqs.hasKeyword(gd, warden, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, warden, Keyword.VIGILANCE)).isTrue();

        warden.setCounterCount(CounterType.OIL, 2);

        assertThat(gqs.hasKeyword(gd, warden, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, warden, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void canTapSummoningSickCreaturesToPayCost() {
        Permanent warden = addWarden();
        Permanent second = harness.addToBattlefieldAndReturn(player1, new WardenOfTheInnerSky());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new WardenOfTheInnerSky());
        warden.setSummoningSick(true);
        second.setSummoningSick(true);
        third.setSummoningSick(true);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(warden.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isTrue();
        assertThat(warden.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(warden.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(third.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void artifactCreatureCountsAsOnlyOnePermanentForCost() {
        Permanent warden = addWarden();
        Permanent gnome = harness.addToBattlefieldAndReturn(player1, new MarketGnome());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents");

        assertThat(warden.isTapped()).isFalse();
        assertThat(gnome.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotUseTappedOrOpposingPermanentsForCost() {
        Permanent warden = addWarden();
        harness.addToBattlefield(player1, new WardenOfTheInnerSky());
        Permanent tapped = harness.addToBattlefieldAndReturn(player1, new WardenOfTheInnerSky());
        tapped.tap();
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new WardenOfTheInnerSky());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents");

        assertThat(warden.isTapped()).isFalse();
        assertThat(opposing.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void scryStillResolvesWhenSourceLeavesBattlefield() {
        Permanent warden = addWarden();
        harness.addToBattlefield(player1, new WardenOfTheInnerSky());
        harness.addToBattlefield(player1, new WardenOfTheInnerSky());
        WardenOfTheInnerSky topCard = new WardenOfTheInnerSky();
        harness.setLibrary(player1, List.of(topCard));

        harness.activateAbility(player1, 0, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(warden);
        gd.playerGraveyards.get(player1.getId()).add(warden.getCard());
        harness.passBothPriorities();

        assertThat(warden.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard);
    }

    @Test
    void tappedSourceCanActivateUsingThreeOtherArtifacts() {
        Permanent warden = addWarden();
        warden.tap();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MarketGnome());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MarketGnome());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new MarketGnome());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(warden.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void cannotActivateWhileAnotherAbilityIsOnStack() {
        addWarden();
        harness.addToBattlefield(player1, new WardenOfTheInnerSky());
        harness.addToBattlefield(player1, new WardenOfTheInnerSky());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addToBattlefield(player1, new WardenOfTheInnerSky());
        harness.addToBattlefield(player1, new WardenOfTheInnerSky());
        harness.addToBattlefield(player1, new WardenOfTheInnerSky());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void cannotActivateDuringOpponentsMainPhase() {
        addWarden();
        harness.addToBattlefield(player1, new WardenOfTheInnerSky());
        harness.addToBattlefield(player1, new WardenOfTheInnerSky());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private Permanent addWarden() {
        return harness.addToBattlefieldAndReturn(player1, new WardenOfTheInnerSky());
    }
}
