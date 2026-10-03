package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DesertedTemple.class, Forest.class, LlanowarElves.class})
class DesertedTempleTest extends BaseCardTest {

    @Test
    @DisplayName("Mana ability adds one colorless mana")
    void manaAbilityAddsColorlessMana() {
        harness.addToBattlefield(player1, new DesertedTemple());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanent(player1, "Deserted Temple").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps target land")
    void untapsTargetLand() {
        Permanent temple = harness.addToBattlefieldAndReturn(player1, new DesertedTemple());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, forest.getId());
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isFalse();
        assertThat(temple.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target a land controlled by an opponent")
    void untapsOpponentLand() {
        harness.addToBattlefield(player1, new DesertedTemple());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, forest.getId());
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Can target itself")
    void untapsItself() {
        Permanent temple = harness.addToBattlefieldAndReturn(player1, new DesertedTemple());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, temple.getId());
        harness.passBothPriorities();

        assertThat(temple.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        harness.addToBattlefield(player1, new DesertedTemple());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, elf.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("An untapped land can be targeted and tapped for mana before resolution")
    void untappedTargetCanProduceManaBeforeResolution() {
        Permanent temple = harness.addToBattlefieldAndReturn(player1, new DesertedTemple());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, forest.getId());

        assertThat(temple.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.tapPermanent(player1, 1);
        assertThat(forest.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(forest.isTapped()).isFalse();
        harness.tapPermanent(player1, 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Untapping a land that is already untapped still pays the activation costs")
    void alreadyUntappedLandRemainsUntapped() {
        Permanent temple = harness.addToBattlefieldAndReturn(player1, new DesertedTemple());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, forest.getId());
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isFalse();
        assertThat(temple.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("A tapped Temple cannot activate its untap ability")
    void cannotActivateUntapAbilityWhileTapped() {
        Permanent temple = harness.addToBattlefieldAndReturn(player1, new DesertedTemple());
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, temple.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(temple.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
