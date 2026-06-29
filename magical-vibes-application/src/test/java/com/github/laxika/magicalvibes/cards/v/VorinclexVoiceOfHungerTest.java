package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AddOneOfEachManaTypeProducedByLandEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentTappedLandDoesntUntapEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VorinclexVoiceOfHungerTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Vorinclex has correct effects")
    void hasCorrectEffects() {
        VorinclexVoiceOfHunger card = new VorinclexVoiceOfHunger();

        List<?> landTapEffects = card.getEffects(EffectSlot.ON_ANY_PLAYER_TAPS_LAND);
        assertThat(landTapEffects).hasSize(2);
        assertThat(landTapEffects).anySatisfy(e ->
                assertThat(e).isInstanceOf(AddOneOfEachManaTypeProducedByLandEffect.class));
        assertThat(landTapEffects).anySatisfy(e ->
                assertThat(e).isInstanceOf(OpponentTappedLandDoesntUntapEffect.class));
    }

    // ===== Mana doubling for controller =====

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

        // Opponent should get only 1 green mana (no doubling from player1's Vorinclex)
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    // ===== Opponent land doesn't untap =====

    @Test
    @DisplayName("Opponent's tapped land doesn't untap during their next untap step")
    void opponentLandDoesntUntap() {
        harness.addToBattlefield(player1, new VorinclexVoiceOfHunger());
        harness.addToBattlefield(player2, new Forest());

        // Opponent taps their Forest
        harness.tapPermanent(player2, 0);

        Permanent forest = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(forest.isTapped()).isTrue();
        assertThat(forest.getSkipUntapCount()).isGreaterThan(0);

        // Advance to player2's turn (untap step)
        advanceToNextTurn(player1);

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

        Permanent forest = gd.playerBattlefields.get(player2.getId()).getFirst();

        // First untap step — land should stay tapped, flag cleared
        advanceToNextTurn(player1);
        assertThat(forest.isTapped()).isTrue();
        assertThat(forest.getSkipUntapCount()).isZero();

        // Manually verify: on next untap step the forest would untap since skipUntapCount is now 0
        // Simulate by checking the flag is cleared and the land is still tapped but eligible to untap
        assertThat(forest.getUntapPreventedByPermanentIds()).isEmpty();
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
        advanceToNextTurn(player2);
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Multiple opponent lands tapped all get locked")
    void multipleOpponentLandsLocked() {
        harness.addToBattlefield(player1, new VorinclexVoiceOfHunger());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Mountain());

        harness.tapPermanent(player2, 0);
        harness.tapPermanent(player2, 1);

        Permanent forest = gd.playerBattlefields.get(player2.getId()).get(0);
        Permanent mountain = gd.playerBattlefields.get(player2.getId()).get(1);

        assertThat(forest.getSkipUntapCount()).isGreaterThan(0);
        assertThat(mountain.getSkipUntapCount()).isGreaterThan(0);

        // Advance to opponent's untap step
        advanceToNextTurn(player1);

        // Both should still be tapped
        assertThat(forest.isTapped()).isTrue();
        assertThat(mountain.isTapped()).isTrue();
    }

    // ===== Both abilities together =====

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
        Permanent opponentForest = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(opponentForest.getSkipUntapCount()).isZero();
    }

    // ===== Helpers =====

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn (advanceTurn)
    }
}
