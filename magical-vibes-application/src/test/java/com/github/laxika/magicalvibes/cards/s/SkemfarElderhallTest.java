package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

class SkemfarElderhallTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and taps for green mana")
    void entersTappedAndTapsForGreen() {
        harness.setHand(player1, List.of(new SkemfarElderhall()));
        harness.playLand(player1, 0);
        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();

        assertThat(land.isTapped()).isTrue();

        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrifice ability weakens an opposing creature and creates two Elf Warriors")
    void sacrificeAbilityWeakensCreatureAndCreatesTokens() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SkemfarElderhall());
        land.untap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        addManaForAbility();

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(countPermanents(player1, "Elf Warrior")).isEqualTo(2);
        harness.assertInGraveyard(player1, "Skemfar Elderhall");
    }

    @Test
    @DisplayName("May decline the target and still create two Elf Warriors")
    void mayDeclineTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SkemfarElderhall());
        land.untap();
        addManaForAbility();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elf Warrior")).isEqualTo(2);
        harness.assertInGraveyard(player1, "Skemfar Elderhall");
    }

    @Test
    @DisplayName("Can target only a creature an opponent controls")
    void cannotTargetOwnCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SkemfarElderhall());
        land.untap();
        Permanent ownCreature = addCreatureReady(player1, new AirElemental());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifice ability is sorcery speed only")
    void sacrificeAbilityIsSorcerySpeedOnly() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SkemfarElderhall());
        land.untap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        addManaForAbility();
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
