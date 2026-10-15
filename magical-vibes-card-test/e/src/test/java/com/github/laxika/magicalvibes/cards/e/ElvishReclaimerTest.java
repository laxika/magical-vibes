package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.t.TempleOfMystery;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElvishReclaimer.class, Forest.class, GreenwoodSentinel.class, Island.class, Plains.class, TempleOfMystery.class})
class ElvishReclaimerTest extends BaseCardTest {

    @Nested
    @DisplayName("Graveyard threshold")
    @CardUsed({ElvishReclaimer.class, Forest.class, GreenwoodSentinel.class, Island.class, Plains.class, TempleOfMystery.class})
    class GraveyardThresholdTests {

        @Test
        @DisplayName("Gets +2/+2 with three land cards in its controller's graveyard")
        void boostsWithThreeLandCardsInGraveyard() {
            Permanent reclaimer = addReclaimer(player1);
            int basePower = gqs.getEffectivePower(gd, reclaimer);
            int baseToughness = gqs.getEffectiveToughness(gd, reclaimer);

            harness.setGraveyard(player1, List.of(new Forest(), new Island(), new Plains()));

            assertThat(gqs.getEffectivePower(gd, reclaimer)).isEqualTo(basePower + 2);
            assertThat(gqs.getEffectiveToughness(gd, reclaimer)).isEqualTo(baseToughness + 2);
        }

        @Test
        @DisplayName("Does not boost with fewer than three land cards")
        void doesNotBoostBelowThreshold() {
            Permanent reclaimer = addReclaimer(player1);
            int basePower = gqs.getEffectivePower(gd, reclaimer);

            harness.setGraveyard(player1, List.of(new Forest(), new Island(), new GreenwoodSentinel()));

            assertThat(gqs.getEffectivePower(gd, reclaimer)).isEqualTo(basePower);
        }

        @Test
        @DisplayName("Does not count land cards in an opponent's graveyard")
        void ignoresOpponentGraveyard() {
            Permanent reclaimer = addReclaimer(player1);
            int basePower = gqs.getEffectivePower(gd, reclaimer);

            harness.setGraveyard(player2, List.of(new Forest(), new Island(), new Plains()));

            assertThat(gqs.getEffectivePower(gd, reclaimer)).isEqualTo(basePower);
        }
    }

    @Nested
    @DisplayName("Search activated ability")
    @CardUsed({ElvishReclaimer.class, Forest.class, GreenwoodSentinel.class, Island.class, Plains.class, TempleOfMystery.class})
    class SearchAbilityTests {

        @Test
        @DisplayName("Sacrifices a land and puts a fetched land onto the battlefield tapped")
        void sacrificesLandAndFetchesLandTapped() {
            addReclaimer(player1);
            harness.addToBattlefield(player1, new Forest());
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            harness.setLibrary(player1, List.of(new Island(), new GreenwoodSentinel()));

            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
            harness.handleCardChosen(player1, 0);

            harness.assertInGraveyard(player1, "Forest");
            Permanent island = findPermanent(player1, "Island");
            assertThat(island).isNotNull();
            assertThat(island.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Cannot activate without a land to sacrifice")
        void cannotActivateWithoutLand() {
            addReclaimer(player1);
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Test
    void losesBonusWhenGraveyardDropsBelowThreeLands() {
        Permanent reclaimer = addReclaimer(player1);
        int basePower = gqs.getEffectivePower(gd, reclaimer);
        int baseToughness = gqs.getEffectiveToughness(gd, reclaimer);
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new TempleOfMystery()));

        assertThat(gqs.getEffectivePower(gd, reclaimer)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, reclaimer)).isEqualTo(baseToughness + 2);

        harness.setGraveyard(player1, List.of(new Forest(), new TempleOfMystery()));

        assertThat(gqs.getEffectivePower(gd, reclaimer)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, reclaimer)).isEqualTo(baseToughness);
    }

    @Test
    void sacrificePaysCostAndEnablesBonusBeforeSearchResolves() {
        Permanent reclaimer = addReclaimer(player1);
        int basePower = gqs.getEffectivePower(gd, reclaimer);
        int baseToughness = gqs.getEffectiveToughness(gd, reclaimer);
        harness.setGraveyard(player1, List.of(new Island(), new Plains()));
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new Island()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(reclaimer.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Forest");
        assertThat(countPermanents(player1, "Forest")).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, reclaimer)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, reclaimer)).isEqualTo(baseToughness + 2);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        assertThat(findPermanent(player1, "Island").isTapped()).isTrue();
    }

    @Test
    void mayFailToFindEvenWithLandInLibrary() {
        addReclaimer(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new Island()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Forest");
        assertThat(countPermanents(player1, "Island")).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void resolvesWithNoLandInLibrary() {
        addReclaimer(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new GreenwoodSentinel()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Greenwood Sentinel")).isZero();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new ElvishReclaimer());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotSacrificeOpponentsLand() {
        addReclaimer(player1);
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithOnlyOneMana() {
        addReclaimer(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent reclaimer = addReclaimer(player1);
        reclaimer.tap();
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canSearchAnEmptyLibrary() {
        addReclaimer(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReclaimer(Player player) {
        return addCreatureReady(player, new ElvishReclaimer());
    }
}
