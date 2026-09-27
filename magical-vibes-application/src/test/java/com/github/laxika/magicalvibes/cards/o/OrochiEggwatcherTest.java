package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.s.SakuraTribeElder;
import com.github.laxika.magicalvibes.cards.s.ShidakoBroodmistress;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
    @DisplayName("Only creatures the controller owns count toward ten")
    void opponentCreaturesDoNotCount() {
        Permanent eggwatcher = addReadyEggwatcher(player1);
        addSakuraTribeElders(player1, 4);
        addSakuraTribeElders(player2, 9);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(eggwatcher.isTransformed()).isFalse();
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
