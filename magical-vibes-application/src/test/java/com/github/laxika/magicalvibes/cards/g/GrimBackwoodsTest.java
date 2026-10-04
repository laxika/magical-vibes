package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RussetWolves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrimBackwoods.class, RussetWolves.class, GrafdiggersCage.class})
class GrimBackwoodsTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for mana adds colorless mana")
    void tappingForManaAddsColorless() {
        Permanent backwoods = addReadyBackwoods(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(backwoods.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Draw ability sacrifices chosen creature and draws a card")
    void drawAbilitySacrificesChosenCreatureAndDraws() {
        Permanent backwoods = addReadyBackwoods(player1);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new RussetWolves());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new RussetWolves());
        addDrawAbilityMana(player1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, bear.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bear).contains(other);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bear.getCard());
        assertThat(backwoods.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Draw ability auto-sacrifices the only eligible creature")
    void drawAbilityAutoSacrificesOnlyCreature() {
        addReadyBackwoods(player1);
        harness.addToBattlefield(player1, new RussetWolves());
        addDrawAbilityMana(player1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Russet Wolves");
        harness.assertInGraveyard(player1, "Russet Wolves");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Cannot activate draw ability without enough mana")
    void cannotActivateDrawAbilityWithoutEnoughMana() {
        addReadyBackwoods(player1);
        harness.addToBattlefield(player1, new RussetWolves());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate draw ability without a creature to sacrifice")
    void cannotActivateDrawAbilityWithoutCreature() {
        addReadyBackwoods(player1);
        addDrawAbilityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must choose a creature to sacrifice");
    }

    @Test
    @DisplayName("Cannot activate draw ability when Grim Backwoods is tapped")
    void cannotActivateDrawAbilityWhenTapped() {
        Permanent backwoods = addReadyBackwoods(player1);
        backwoods.tap();
        harness.addToBattlefield(player1, new RussetWolves());
        addDrawAbilityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Non-creature permanents are not eligible for sacrifice")
    void nonCreaturePermanentsAreNotEligibleForSacrifice() {
        addReadyBackwoods(player1);
        harness.addToBattlefield(player1, new GrafdiggersCage());
        harness.addToBattlefield(player1, new RussetWolves());
        addDrawAbilityMana(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grafdigger's Cage");
        harness.assertNotOnBattlefield(player1, "Russet Wolves");
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        Permanent backwoods = addReadyBackwoods(player1);
        harness.addToBattlefield(player2, new RussetWolves());
        addDrawAbilityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must choose a creature to sacrifice");

        harness.assertOnBattlefield(player2, "Russet Wolves");
        assertThat(backwoods.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped summoning-sick creature can be sacrificed")
    void canSacrificeTappedSummoningSickCreature() {
        addReadyBackwoods(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RussetWolves());
        creature.setSummoningSick(true);
        creature.tap();
        addDrawAbilityMana(player1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertInGraveyard(player1, "Russet Wolves");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("A newly controlled noncreature land can tap for mana")
    void newlyControlledLandCanTapForMana() {
        Permanent backwoods = harness.addToBattlefieldAndReturn(player1, new GrimBackwoods());
        backwoods.setSummoningSick(true);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(backwoods.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyBackwoods(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GrimBackwoods());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void addDrawAbilityMana(Player player) {
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }
}
