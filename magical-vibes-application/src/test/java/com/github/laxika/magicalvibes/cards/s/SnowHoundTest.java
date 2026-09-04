package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.k.KjeldoranWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SnowHound.class, BalduvianBears.class, SeaSpirit.class, KjeldoranWarrior.class})
class SnowHoundTest extends BaseCardTest {

    @Test
    @DisplayName("Ability returns Snow Hound and target green creature you control to hand")
    void returnsSelfAndGreenCreature() {
        Permanent hound = addCreatureReady(player1, new SnowHound());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Snow Hound");
        harness.assertNotOnBattlefield(player1, "Balduvian Bears");
        harness.assertInHand(player1, "Snow Hound");
        harness.assertInHand(player1, "Balduvian Bears");
        assertThat(hound.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability returns Snow Hound and target blue creature you control to hand")
    void returnsSelfAndBlueCreature() {
        addCreatureReady(player1, new SnowHound());
        Permanent seaSpirit = addCreatureReady(player1, new SeaSpirit());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, seaSpirit.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Snow Hound");
        harness.assertNotOnBattlefield(player1, "Sea Spirit");
        harness.assertInHand(player1, "Snow Hound");
        harness.assertInHand(player1, "Sea Spirit");
    }

    @Test
    @DisplayName("Cannot target a non-green non-blue creature")
    void cannotTargetWhiteCreature() {
        addCreatureReady(player1, new SnowHound());
        Permanent warrior = addCreatureReady(player1, new KjeldoranWarrior());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, warrior.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an opponent's green creature")
    void cannotTargetOpponentsCreature() {
        addCreatureReady(player1, new SnowHound());
        Permanent bears = addCreatureReady(player2, new BalduvianBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without paying the generic mana cost")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new SnowHound());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while Snow Hound is tapped")
    void cannotActivateWhileTapped() {
        Permanent hound = addCreatureReady(player1, new SnowHound());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        hound.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
