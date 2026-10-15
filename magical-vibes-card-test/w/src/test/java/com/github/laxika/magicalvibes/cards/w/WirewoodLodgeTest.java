package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.i.IronfistCrusher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WirewoodLodge.class, ElvishWarrior.class, IronfistCrusher.class})
class WirewoodLodgeTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless mana adds one colorless mana")
    void tapForColorlessMana() {
        Permanent lodge = harness.addToBattlefieldAndReturn(player1, new WirewoodLodge());

        harness.activateAbility(player1, 0, null, null);

        assertThat(lodge.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Untaps a target Elf controlled by an opponent")
    void untapsTargetElf() {
        Permanent lodge = harness.addToBattlefieldAndReturn(player1, new WirewoodLodge());
        Permanent elf = addCreatureReady(player2, new ElvishWarrior());
        elf.tap();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 1, null, elf.getId());
        harness.passBothPriorities();

        assertThat(lodge.isTapped()).isTrue();
        assertThat(elf.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-Elf permanent")
    void cannotTargetNonElf() {
        harness.addToBattlefieldAndReturn(player1, new WirewoodLodge());
        Permanent nonElf = addCreatureReady(player2, new IronfistCrusher());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, nonElf.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an Elf");
    }

    @Test
    @DisplayName("Pays green mana and uses the stack to untap your own Elf")
    void paysGreenAndUntapsOwnElfOnResolution() {
        Permanent lodge = harness.addToBattlefieldAndReturn(player1, new WirewoodLodge());
        Permanent elf = addCreatureReady(player1, new ElvishWarrior());
        elf.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, elf.getId());

        assertThat(lodge.isTapped()).isTrue();
        assertThat(elf.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(elf.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An untapped Elf is a legal target")
    void canTargetUntappedElf() {
        Permanent lodge = harness.addToBattlefieldAndReturn(player1, new WirewoodLodge());
        Permanent elf = addCreatureReady(player2, new ElvishWarrior());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, elf.getId());
        harness.passBothPriorities();

        assertThat(lodge.isTapped()).isTrue();
        assertThat(elf.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate the untap ability without green mana")
    void cannotActivateWithoutGreenMana() {
        Permanent lodge = harness.addToBattlefieldAndReturn(player1, new WirewoodLodge());
        Permanent elf = addCreatureReady(player2, new ElvishWarrior());
        elf.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, elf.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(lodge.isTapped()).isFalse();
        assertThat(elf.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate either ability while the Lodge is tapped")
    void cannotActivateTappedLodge() {
        Permanent lodge = harness.addToBattlefieldAndReturn(player1, new WirewoodLodge());
        lodge.tap();
        Permanent elf = addCreatureReady(player2, new ElvishWarrior());
        elf.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, elf.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(elf.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The untap ability resolves after the Lodge leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent lodge = harness.addToBattlefieldAndReturn(player1, new WirewoodLodge());
        Permanent elf = addCreatureReady(player2, new ElvishWarrior());
        elf.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 1, null, elf.getId());

        gd.playerBattlefields.get(player1.getId()).remove(lodge);
        gd.playerGraveyards.get(player1.getId()).add(lodge.getCard());
        harness.passBothPriorities();

        assertThat(elf.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
