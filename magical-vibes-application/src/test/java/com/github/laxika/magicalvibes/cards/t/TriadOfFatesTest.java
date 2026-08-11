package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TriadOfFatesTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a fate counter on another target creature")
    void putsFateCounterOnAnotherCreature() {
        Permanent triad = addReadyTriad(player1);
        Permanent bears = addReadyBears(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(triad.getCounterCount(CounterType.FATE)).isZero();
        assertThat(bears.getCounterCount(CounterType.FATE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot put a fate counter on Triad of Fates itself")
    void cannotTargetItselfForFateCounter() {
        addReadyTriad(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null,
                harness.getPermanentId(player1, "Triad of Fates")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exiles a fate-countered creature and returns it without its counter")
    void flickersFateCounteredCreature() {
        addReadyTriad(player1);
        Permanent bears = addReadyBears(player1);
        bears.setCounterCount(CounterType.FATE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(bears.getId());
        assertThat(returned.getCounterCount(CounterType.FATE)).isZero();
    }

    @Test
    @DisplayName("Exiles a fate-countered creature and its controller draws two cards")
    void exilesFateCounteredCreatureAndItsControllerDraws() {
        addReadyTriad(player1);
        Permanent bears = addReadyBears(player2);
        bears.setCounterCount(CounterType.FATE, 1);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 2, null, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot target a creature without a fate counter for the white ability")
    void cannotFlickerCreatureWithoutFateCounter() {
        addReadyTriad(player1);
        Permanent bears = addReadyBears(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyTriad(Player player) {
        Permanent triad = new Permanent(new TriadOfFates());
        triad.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(triad);
        return triad;
    }

    private Permanent addReadyBears(Player player) {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(bears);
        return bears;
    }
}
