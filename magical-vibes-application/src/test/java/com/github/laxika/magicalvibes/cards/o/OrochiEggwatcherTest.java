package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CultOfTheWaxingMoon;
import com.github.laxika.magicalvibes.cards.s.SakuraTribeElder;
import com.github.laxika.magicalvibes.cards.s.ShidakoBroodmistress;
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

@CardUsed({OrochiEggwatcher.class, ShidakoBroodmistress.class, SakuraTribeElder.class})
class OrochiEggwatcherTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 1/1 Snake token")
    void createsSnakeToken() {
        Permanent eggwatcher = addReadyEggwatcher(player1);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Snake");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(eggwatcher.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not flip while fewer than ten creatures are controlled")
    void staysUnflippedBelowTenCreatures() {
        Permanent eggwatcher = addReadyEggwatcher(player1);
        addSakuraTribeElders(player1, 5);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(eggwatcher.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Flips when the created token is the tenth creature")
    void flipsWhenTokenIsTheTenth() {
        Permanent eggwatcher = addReadyEggwatcher(player1);
        addSakuraTribeElders(player1, 8);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(eggwatcher.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Flips when already controlling more than ten creatures")
    void flipsWhenAlreadyAboveTenCreatures() {
        Permanent eggwatcher = addReadyEggwatcher(player1);
        addSakuraTribeElders(player1, 9);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(eggwatcher.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Only creatures the ability controller controls count toward ten")
    void opponentCreaturesDoNotCount() {
        Permanent eggwatcher = addReadyEggwatcher(player1);
        addSakuraTribeElders(player1, 4);
        addSakuraTribeElders(player2, 9);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(eggwatcher.isTransformed()).isFalse();
    }

    @Test
    @CardUsed({CultOfTheWaxingMoon.class})
    @DisplayName("Flipping does not trigger abilities that watch for transformation")
    void flippingDoesNotTriggerTransformationAbilities() {
        Permanent eggwatcher = addReadyEggwatcher(player1);
        addSakuraTribeElders(player1, 7);
        addCreatureReady(player1, new CultOfTheWaxingMoon());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(eggwatcher.isTransformed()).isTrue();
        assertThat(countPermanents(player1, "Snake")).isEqualTo(1);
        assertThat(countPermanents(player1, "Wolf")).isZero();
    }

    @Test
    @DisplayName("Checks the creature count at resolution after a creature is sacrificed in response")
    void creatureCountCanDropBeforeResolution() {
        Permanent eggwatcher = addReadyEggwatcher(player1);
        addSakuraTribeElders(player1, 8);
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(eggwatcher.isTransformed()).isFalse();
        assertThat(countPermanents(player1, "Snake")).isEqualTo(1);
        assertThat(countPermanents(player1, "Sakura-Tribe Elder")).isEqualTo(7);
    }

    @Test
    @DisplayName("Flipping preserves the tap cost and makes the new ability available immediately")
    void flippedAbilityCanBeUsedWhileTapped() {
        Permanent eggwatcher = addReadyEggwatcher(player1);
        addSakuraTribeElders(player1, 8);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(eggwatcher.isTransformed()).isTrue();
        assertThat(eggwatcher.isTapped()).isTrue();

        Permanent token = findPermanent(player1, "Snake");
        harness.activateAbility(player1, 0, null, eggwatcher.getId());
        harness.handlePermanentChosen(player1, token.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, eggwatcher)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, eggwatcher)).isEqualTo(6);
        assertThat(countPermanents(player1, "Snake")).isZero();
    }

    @Test
    @DisplayName("Cannot activate the token ability while summoning sick")
    void tokenAbilityRequiresReadySource() {
        harness.addToBattlefield(player1, new OrochiEggwatcher());
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Snake")).isZero();
    }

    @Test
    @DisplayName("Cannot activate the token ability while tapped")
    void tokenAbilityRequiresUntappedSource() {
        Permanent eggwatcher = addReadyEggwatcher(player1);
        eggwatcher.tap();
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Snake")).isZero();
    }

    @Test
    @DisplayName("The token ability requires three mana including green")
    void tokenAbilityRequiresFullManaCost() {
        Permanent eggwatcher = addReadyEggwatcher(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(eggwatcher.isTapped()).isFalse();
        assertThat(countPermanents(player1, "Snake")).isZero();
    }

    private Permanent addReadyEggwatcher(Player player) {
        return addCreatureReady(player, new OrochiEggwatcher());
    }

    private void addSakuraTribeElders(Player player, int count) {
        for (int i = 0; i < count; i++) {
            addCreatureReady(player, new SakuraTribeElder());
        }
    }
}
