package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FavorableWinds;
import com.github.laxika.magicalvibes.cards.w.WanderingWolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlchemistsRefuge.class, WanderingWolf.class, FavorableWinds.class})
class AlchemistsRefugeTest extends BaseCardTest {

    private void addRefuge(Player player) {
        harness.addToBattlefield(player, new AlchemistsRefuge());
    }

    @Test
    @DisplayName("Mana ability taps for {C}")
    void manaAbilityAddsColorless() {
        addRefuge(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("After activating the flash ability, a creature can be cast at instant speed")
    void grantsFlashToCreatureSpell() {
        addRefuge(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new WanderingWolf()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Wandering Wolf");
    }

    @Test
    @DisplayName("Without activating the ability, a creature cannot be cast at instant speed")
    void noFlashWithoutActivation() {
        addRefuge(player1);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new WanderingWolf()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Only the activating player's spells gain flash")
    void onlyAffectsActivatingPlayer() {
        addRefuge(player2);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new WanderingWolf()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Flash permission wears off at end of turn")
    void flashWearsOffAtEndOfTurn() {
        addRefuge(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gd.playersWithFlashUntilEndOfTurn).contains(player1.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.playersWithFlashUntilEndOfTurn).doesNotContain(player1.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new WanderingWolf()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Flash ability cannot be activated without {G}{U}")
    void cannotActivateWithoutMana() {
        addRefuge(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flash permission begins only when the activated ability resolves")
    void permissionRequiresResolution() {
        addRefuge(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new WanderingWolf()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wandering Wolf");
    }

    @Test
    @DisplayName("The flash ability can be activated and used on an opponent's turn")
    void castsCreatureOnOpponentsTurn() {
        addRefuge(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new WanderingWolf()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wandering Wolf");
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Flash permission applies to multiple noncreature spells with a nonempty stack")
    void castsMultipleEnchantmentsInResponse() {
        addRefuge(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new FavorableWinds(), new FavorableWinds()));

        harness.castEnchantment(player1, 0);
        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof FavorableWinds)
                .hasSize(2);
    }

    @Test
    @DisplayName("Flash permission does not allow playing lands outside a main phase")
    void doesNotChangeLandTiming() {
        addRefuge(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new AlchemistsRefuge()));

        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Alchemist's Refuge");
    }

    @Test
    @DisplayName("A Refuge tapped for mana cannot also activate its flash ability")
    void cannotActivateFlashAfterTappingForMana() {
        addRefuge(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }
}
