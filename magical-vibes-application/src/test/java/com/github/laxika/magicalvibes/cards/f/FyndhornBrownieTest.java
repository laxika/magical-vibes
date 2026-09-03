package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FyndhornBrownie.class, SnowCoveredForest.class})
class FyndhornBrownieTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability taps the Brownie")
    void activatingTapsBrownie() {
        Permanent brownie = addCreatureReady(player1, new FyndhornBrownie());
        Permanent target = addCreatureReady(player2, new FyndhornBrownie());
        addBrownieMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(brownie.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps a tapped creature")
    void untapsTappedCreature() {
        addCreatureReady(player1, new FyndhornBrownie());
        Permanent target = addCreatureReady(player2, new FyndhornBrownie());
        target.tap();
        addBrownieMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can untap own tapped creature")
    void canUntapOwnCreature() {
        addCreatureReady(player1, new FyndhornBrownie());
        Permanent ownCreature = addCreatureReady(player1, new FyndhornBrownie());
        ownCreature.tap();
        addBrownieMana(player1);

        harness.activateAbility(player1, 0, null, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new FyndhornBrownie());
        Permanent target = addCreatureReady(player2, new FyndhornBrownie());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate ability when the Brownie is already tapped")
    void cannotActivateWhenAlreadyTapped() {
        Permanent brownie = addCreatureReady(player1, new FyndhornBrownie());
        brownie.tap();
        Permanent target = addCreatureReady(player2, new FyndhornBrownie());
        addBrownieMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new FyndhornBrownie());
        Permanent land = new Permanent(new SnowCoveredForest());
        gd.playerBattlefields.get(player2.getId()).add(land);
        addBrownieMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addCreatureReady(player1, new FyndhornBrownie());
        Permanent target = addCreatureReady(player2, new FyndhornBrownie());
        addBrownieMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    private void addBrownieMana(Player player) {
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

}
