package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AlloyMyr;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.g.GaeasCradle;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.m.MyrSuperion;
import com.github.laxika.magicalvibes.cards.t.Turnabout;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VorinclexVoiceOfHunger.class, Forest.class, Mountain.class, GaeasCradle.class,
        MyrSuperion.class, Humble.class, Turnabout.class, AlloyMyr.class})
class VorinclexVoiceOfHungerTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping own Forest produces double green mana")
    void doublesOwnLandMana() {
        harness.addToBattlefield(player1, new VorinclexVoiceOfHunger());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        // 1 from Forest + 1 from Vorinclex trigger = 2
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapping own Mountain produces double red mana")
    void doublesOwnMountainMana() {
        harness.addToBattlefield(player1, new VorinclexVoiceOfHunger());
        harness.addToBattlefield(player1, new Mountain());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple lands each get doubled")
    void doublesMultipleLands() {
        harness.addToBattlefield(player1, new VorinclexVoiceOfHunger());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);
        harness.tapPermanent(player1, 2);

        // 2 Forests * 2 mana each = 4
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(4);
    }

    @Test
    @DisplayName("Mana doubling does not apply to opponent's lands")
    void doesNotDoubleOpponentMana() {
        harness.addToBattlefield(player1, new VorinclexVoiceOfHunger());
        harness.addToBattlefield(player2, new Forest());

        harness.tapPermanent(player2, 0);
        resolveAllTriggers();

        // Opponent should get only 1 green mana (no doubling from player1's Vorinclex)
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent's tapped land doesn't untap during their next untap step")
    void opponentLandDoesntUntap() {
        harness.addToBattlefield(player1, new VorinclexVoiceOfHunger());
        harness.addToBattlefield(player2, new Forest());

        // Opponent taps their Forest
        harness.tapPermanent(player2, 0);
        resolveAllTriggers();

        Permanent forest = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(forest.isTapped()).isTrue();
        assertThat(forest.getSkipUntapCount()).isGreaterThan(0);

        // Advance to player2's turn (untap step)
        harness.performUntapStep(player2);

        // Forest should still be tapped (skipped untap)
        assertThat(forest.isTapped()).isTrue();
        // Flag should now be cleared
        assertThat(forest.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Opponent's land untaps normally on the second untap step after tapping")
    void opponentLandUntapsOnSecondUntapStep() {
        harness.addToBattlefield(player1, new VorinclexVoiceOfHunger());
        harness.addToBattlefield(player2, new Forest());

        // Opponent taps their Forest
        harness.tapPermanent(player2, 0);
        resolveAllTriggers();

        Permanent forest = gd.playerBattlefields.get(player2.getId()).getFirst();

        // First untap step — land should stay tapped, flag cleared
        harness.performUntapStep(player2);
        assertThat(forest.isTapped()).isTrue();
        assertThat(forest.getSkipUntapCount()).isZero();

        harness.performUntapStep(player1);
        harness.performUntapStep(player2);
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Controller's tapped land does not get the untap lock")
    void controllerLandNotLocked() {
        harness.addToBattlefield(player1, new VorinclexVoiceOfHunger());
        harness.addToBattlefield(player1, new Forest());

        // Controller taps their own Forest
        harness.tapPermanent(player1, 1);

        Permanent forest = gd.playerBattlefields.get(player1.getId()).get(1);
        assertThat(forest.getSkipUntapCount()).isZero();

        // Advance to player1's turn — Forest should untap normally
        harness.performUntapStep(player1);
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Multiple opponent lands tapped all get locked")
    void multipleOpponentLandsLocked() {
        harness.addToBattlefield(player1, new VorinclexVoiceOfHunger());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Mountain());

        harness.tapPermanent(player2, 0);
        resolveAllTriggers();
        harness.tapPermanent(player2, 1);
        resolveAllTriggers();

        Permanent forest = gd.playerBattlefields.get(player2.getId()).get(0);
        Permanent mountain = gd.playerBattlefields.get(player2.getId()).get(1);

        assertThat(forest.getSkipUntapCount()).isGreaterThan(0);
        assertThat(mountain.getSkipUntapCount()).isGreaterThan(0);

        // Advance to opponent's untap step
        harness.performUntapStep(player2);

        // Both should still be tapped
        assertThat(forest.isTapped()).isTrue();
        assertThat(mountain.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Controller gets double mana while opponent lands get locked")
    void bothAbilitiesWorkTogether() {
        harness.addToBattlefield(player1, new VorinclexVoiceOfHunger());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        // Controller taps — should get double mana, no lock
        harness.tapPermanent(player1, 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);

        Permanent controllerForest = gd.playerBattlefields.get(player1.getId()).get(1);
        assertThat(controllerForest.getSkipUntapCount()).isZero();

        // Opponent taps — should get normal mana, land gets locked
        harness.tapPermanent(player2, 0);
        resolveAllTriggers();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);

        Permanent opponentForest = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(opponentForest.getSkipUntapCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Vorinclex leaving battlefield stops both abilities")
    void removingVorinclexStopsEffects() {
        harness.addToBattlefield(player1, new VorinclexVoiceOfHunger());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        // Remove Vorinclex from battlefield
        gd.playerBattlefields.get(player1.getId()).removeFirst();

        // Controller taps — should get only 1 mana
        harness.tapPermanent(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);

        // Opponent taps — should not get locked
        harness.tapPermanent(player2, 0);
        resolveAllTriggers();
        Permanent opponentForest = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(opponentForest.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Opponent land restriction waits for its ordinary triggered ability to resolve")
    void opponentRestrictionUsesTheStack() {
        harness.addToBattlefield(player1, new VorinclexVoiceOfHunger());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.tapPermanent(player2, 0);

        assertThat(forest.getSkipUntapCount()).isZero();
        assertThat(gd.stack.size() + gd.pendingManaAbilityTriggers.size()).isEqualTo(1);
        resolveAllTriggers();
        harness.performUntapStep(player2);
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Repeated taps before the next untap step restrict only that one step")
    void repeatedTapsDoNotSkipMultipleUntapSteps() {
        harness.addToBattlefield(player1, new VorinclexVoiceOfHunger());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.tapPermanent(player2, 0);
        resolveAllTriggers();

        harness.setHand(player2, List.of(new Turnabout()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castAndResolveInstant(player2, 0, player2.getId());
        harness.handleListChoice(player2, "UNTAP_LAND");
        assertThat(forest.isTapped()).isFalse();

        harness.tapPermanent(player2, 0);
        resolveAllTriggers();
        harness.performUntapStep(player2);
        assertThat(forest.isTapped()).isTrue();
        harness.performUntapStep(player1);
        harness.performUntapStep(player2);
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An existing land restriction survives Vorinclex leaving the battlefield")
    void resolvedRestrictionSurvivesSourceLeaving() {
        harness.addToBattlefield(player1, new VorinclexVoiceOfHunger());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.tapPermanent(player2, 0);
        resolveAllTriggers();

        gd.playerBattlefields.get(player1.getId()).removeFirst();
        harness.performUntapStep(player2);
        assertThat(forest.isTapped()).isTrue();
        harness.performUntapStep(player1);
        harness.performUntapStep(player2);
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping lands with a spell does not trigger either ability")
    void tappingWithoutProducingManaDoesNotTrigger() {
        harness.addToBattlefield(player1, new VorinclexVoiceOfHunger());
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Turnabout(), new Turnabout()));
        harness.addMana(player1, ManaColor.BLUE, 8);

        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.handleListChoice(player1, "TAP_LAND");
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleListChoice(player1, "TAP_LAND");

        assertThat(ownForest.isTapped()).isTrue();
        assertThat(opponentForest.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
        resolveAllTriggers();
        harness.performUntapStep(player2);
        assertThat(opponentForest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A land producing multiple mana receives just one additional mana")
    void addsOneManaRatherThanDoublingQuantity() {
        harness.addToBattlefield(player1, new VorinclexVoiceOfHunger());
        harness.addToBattlefield(player1, new MyrSuperion());
        harness.addToBattlefield(player1, new GaeasCradle());

        harness.activateAbility(player1, 2, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingManaAbilityTriggers).isEmpty();
    }

    @Test
    @DisplayName("An opponent land producing no mana still receives the untap restriction")
    void zeroManaProductionStillLocksOpponentLand() {
        harness.addToBattlefield(player1, new VorinclexVoiceOfHunger());
        Permanent cradle = harness.addToBattlefieldAndReturn(player2, new GaeasCradle());

        harness.activateAbility(player2, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
        harness.performUntapStep(player2);
        assertThat(cradle.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Nonland mana abilities do not trigger either land-tap ability")
    void nonlandManaDoesNotTrigger() {
        harness.addToBattlefield(player1, new VorinclexVoiceOfHunger());
        Permanent ownMyr = addCreatureReady(player1, new AlloyMyr());
        Permanent opponentMyr = addCreatureReady(player2, new AlloyMyr());

        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, "GREEN");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, "GREEN");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        resolveAllTriggers();

        harness.performUntapStep(player2);
        assertThat(opponentMyr.isTapped()).isFalse();
        harness.performUntapStep(player1);
        assertThat(ownMyr.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Vorinclex cannot trigger either land-tap ability while it has lost all abilities")
    void losingAbilitiesStopsBothTriggers() {
        Permanent vorinclex = harness.addToBattlefieldAndReturn(player1, new VorinclexVoiceOfHunger());
        harness.addToBattlefield(player1, new Forest());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player2, List.of(new Humble()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player2, 0, vorinclex.getId());

        harness.tapPermanent(player1, 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        harness.tapPermanent(player2, 0);
        resolveAllTriggers();
        harness.performUntapStep(player2);
        assertThat(opponentForest.isTapped()).isFalse();
    }
}
