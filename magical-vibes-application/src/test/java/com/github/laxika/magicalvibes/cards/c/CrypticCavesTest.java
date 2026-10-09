package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrypticCaves.class, Forest.class, Island.class, Mountain.class, Plains.class})
class CrypticCavesTest extends BaseCardTest {

    @Test
    @DisplayName("Cryptic Caves taps for colorless mana")
    void tapsForColorlessMana() {
        harness.addToBattlefield(player1, new CrypticCaves());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cryptic Caves draws a card and is sacrificed with five lands")
    void drawsCardWithFiveLands() {
        harness.addToBattlefield(player1, new CrypticCaves());
        addFourLands();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Cryptic Caves");
        harness.assertInGraveyard(player1, "Cryptic Caves");
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Cryptic Caves cannot use its draw ability with fewer than five lands")
    void drawAbilityRequiresFiveLands() {
        harness.addToBattlefield(player1, new CrypticCaves());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tapped lands count and colored mana pays the generic activation cost")
    void drawsWithTappedLandsAndColoredMana() {
        harness.addToBattlefield(player1, new CrypticCaves());
        addFourLands();
        for (int i = 1; i <= 4; i++) {
            gd.playerBattlefields.get(player1.getId()).get(i).tap();
        }
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Opponents' lands do not satisfy the activation restriction")
    void opponentsLandsDoNotCount() {
        harness.addToBattlefield(player1, new CrypticCaves());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Plains());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Cryptic Caves");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Cryptic Caves cannot activate its draw ability")
    void tappedCavesCannotDraw() {
        harness.addToBattlefield(player1, new CrypticCaves());
        addFourLands();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Cryptic Caves");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cryptic Caves cannot draw without paying mana")
    void cannotDrawWithoutMana() {
        harness.addToBattlefield(player1, new CrypticCaves());
        addFourLands();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Cryptic Caves");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void addFourLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Plains());
    }
}
