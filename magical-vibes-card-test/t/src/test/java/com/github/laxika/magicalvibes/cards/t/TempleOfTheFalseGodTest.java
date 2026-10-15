package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.Omenspeaker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TempleOfTheFalseGod.class, Forest.class, Omenspeaker.class})
class TempleOfTheFalseGodTest extends BaseCardTest {

    @Test
    @DisplayName("Temple of the False God adds two colorless mana with five lands")
    void addsTwoColorlessManaWithFiveLands() {
        harness.addToBattlefield(player1, new TempleOfTheFalseGod());
        addForests(4);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Temple of the False God cannot activate with fewer than five lands")
    void cannotActivateWithFewerThanFiveLands() {
        harness.addToBattlefield(player1, new TempleOfTheFalseGod());
        addForests(3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Temple of the False God does not count nonlands toward five lands")
    void doesNotCountNonlandsTowardFiveLands() {
        harness.addToBattlefield(player1, new TempleOfTheFalseGod());
        addForests(3);
        harness.addToBattlefield(player1, new Omenspeaker());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Opponents' lands do not satisfy Temple's activation restriction")
    void doesNotCountOpponentsLands() {
        var temple = harness.addToBattlefieldAndReturn(player1, new TempleOfTheFalseGod());
        addForests(3);
        harness.addToBattlefield(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(temple.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapped lands still count toward Temple's activation restriction")
    void countsTappedLands() {
        var temple = harness.addToBattlefieldAndReturn(player1, new TempleOfTheFalseGod());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefieldAndReturn(player1, new Forest()).tap();
        }

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(temple.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Temple cannot activate again while tapped")
    void cannotActivateAgainWhileTapped() {
        harness.addToBattlefield(player1, new TempleOfTheFalseGod());
        addForests(4);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Temple adds only two mana even with more than five lands")
    void addsTwoManaWithMoreThanFiveLands() {
        harness.addToBattlefield(player1, new TempleOfTheFalseGod());
        addForests(5);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    private void addForests(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
    }
}
