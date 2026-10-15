package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SapseepForest.class, SafeholdElite.class})
class SapseepForestTest extends BaseCardTest {

    @Test
    @DisplayName("Gain-life ability gains 1 life when controlling two or more green permanents")
    void gainLifeWithTwoGreenPermanents() {
        Permanent forest = addForest(player1);
        addGreenPermanents(player1, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, forestIndex(forest), 1, null, null);
        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Gain-life ability cannot be activated with fewer than two green permanents")
    void gainLifeRejectedWithTooFewGreenPermanents() {
        Permanent forest = addForest(player1);
        addGreenPermanents(player1, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, forestIndex(forest), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tap ability adds green mana")
    void manaAbilityAddsGreen() {
        Permanent forest = addForest(player1);

        harness.activateAbility(player1, forestIndex(forest), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void entersTappedWhenPlayed() {
        harness.setHand(player1, List.of(new SapseepForest()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Sapseep Forest").isTapped()).isTrue();
    }

    @Test
    void opponentsGreenPermanentsDoNotCount() {
        Permanent forest = addForest(player1);
        addGreenPermanents(player1, 1);
        addGreenPermanents(player2, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, forestIndex(forest), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void gainLifeRequiresGreenMana() {
        Permanent forest = addForest(player1);
        addGreenPermanents(player1, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, forestIndex(forest), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tappedForestCannotActivateGainLife() {
        Permanent forest = addForest(player1);
        forest.tap();
        addGreenPermanents(player1, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, forestIndex(forest), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void greenPermanentRestrictionIsNotRecheckedAtResolution() {
        Permanent forest = addForest(player1);
        addGreenPermanents(player1, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, forestIndex(forest), 1, null, null);
        List<Permanent> greenPermanents = findPermanents(player1, "Safehold Elite");
        gd.playerBattlefields.get(player1.getId()).removeAll(greenPermanents);
        gd.playerBattlefields.get(player2.getId()).addAll(greenPermanents);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    private Permanent addForest(Player player) {
        return harness.addToBattlefieldAndReturn(player, new SapseepForest());
    }

    private int forestIndex(Permanent forest) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(forest);
    }

    private void addGreenPermanents(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new SafeholdElite());
        }
    }
}
