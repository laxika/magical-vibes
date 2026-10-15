package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FyndhornBrownie.class, Forest.class})
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
    @DisplayName("Can untap itself")
    void canUntapItself() {
        Permanent brownie = addCreatureReady(player1, new FyndhornBrownie());
        addBrownieMana(player1);

        harness.activateAbility(player1, 0, null, brownie.getId());
        harness.passBothPriorities();

        assertThat(brownie.isTapped()).isFalse();
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
    @DisplayName("Cannot activate without green mana")
    void cannotActivateWithoutGreenMana() {
        addCreatureReady(player1, new FyndhornBrownie());
        Permanent target = addCreatureReady(player2, new FyndhornBrownie());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

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
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
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

    @Test
    @DisplayName("Cannot activate the tap ability with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        Permanent brownie = addCreatureReady(player1, new FyndhornBrownie());
        brownie.setSummoningSick(true);
        Permanent target = addCreatureReady(player2, new FyndhornBrownie());
        addBrownieMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(brownie.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An already untapped creature is a legal target")
    void canTargetUntappedCreature() {
        Permanent brownie = addCreatureReady(player1, new FyndhornBrownie());
        Permanent target = addCreatureReady(player2, new FyndhornBrownie());
        addBrownieMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(brownie.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability resolves after its source leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent brownie = addCreatureReady(player1, new FyndhornBrownie());
        Permanent target = addCreatureReady(player2, new FyndhornBrownie());
        target.tap();
        addBrownieMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(brownie);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void addBrownieMana(Player player) {
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

}
