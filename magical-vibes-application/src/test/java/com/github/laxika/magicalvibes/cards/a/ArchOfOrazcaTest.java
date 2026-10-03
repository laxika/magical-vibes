package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.e.EntrancingMelody;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArchOfOrazca.class, Forest.class, GrizzlyBears.class, EntrancingMelody.class})
class ArchOfOrazcaTest extends BaseCardTest {

    @Test
    @DisplayName("Ascend grants the city's blessing when Arch of Orazca enters as the tenth permanent")
    void ascendGrantsBlessing() {
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player1, List.of(new ArchOfOrazca()));

        harness.playLand(player1, 0);

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
    }

    @Test
    @DisplayName("Tapping Arch of Orazca adds one colorless mana")
    void tapsForColorlessMana() {
        harness.addToBattlefield(player1, new ArchOfOrazca());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot draw without the city's blessing")
    void cannotDrawWithoutBlessing() {
        harness.addToBattlefield(player1, new ArchOfOrazca());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("city's blessing");
    }

    @Test
    @DisplayName("With the city's blessing, Arch of Orazca draws a card")
    void drawsCardWithBlessing() {
        gd.playersWithCityBlessing.add(player1.getId());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new ArchOfOrazca());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ascend does not grant the blessing with only nine permanents")
    void ninePermanentsDoNotGrantBlessing() {
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.enterBattlefieldAndReturn(player1, new ArchOfOrazca());

        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
    }

    @Test
    @DisplayName("An existing Arch grants the blessing when another permanent enters as the tenth")
    void laterPermanentGrantsBlessing() {
        harness.addToBattlefield(player1, new ArchOfOrazca());
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Forest());
        }

        harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThat(gd.playersWithCityBlessing).contains(player1.getId()).doesNotContain(player2.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The blessing remains after falling below ten permanents")
    void blessingPersistsBelowTenPermanents() {
        harness.addToBattlefield(player1, new ArchOfOrazca());
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.enterBattlefieldAndReturn(player1, new Forest());
        gd.playerBattlefields.get(player1.getId()).removeLast();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
    }

    @Test
    @DisplayName("The draw ability cannot be activated with only four mana")
    void drawRequiresFiveMana() {
        gd.playersWithCityBlessing.add(player1.getId());
        harness.addToBattlefield(player1, new ArchOfOrazca());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Gaining control of the tenth permanent grants the blessing without an entry event")
    void gainingControlOfTenthPermanentGrantsBlessing() {
        harness.addToBattlefield(player1, new ArchOfOrazca());
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        var bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EntrancingMelody()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 2, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playersWithCityBlessing).contains(player1.getId()).doesNotContain(player2.getId());
    }
}
