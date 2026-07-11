package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MakeshiftMannequinTest extends BaseCardTest {

    private void addCost() {
        harness.addMana(player1, ManaColor.BLACK, 4);
    }

    private Permanent reanimate(Card creature) {
        harness.setGraveyard(player1, new ArrayList<>(List.of(creature)));
        harness.setHand(player1, List.of(new MakeshiftMannequin()));
        addCost();

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(creature.getId()))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Returns target creature card from graveyard to battlefield with a mannequin counter")
    void reanimatesWithMannequinCounter() {
        Permanent bears = reanimate(new GrizzlyBears());

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(bears.getCounterCount(CounterType.MANNEQUIN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Reanimated creature is sacrificed when targeted by a spell")
    void sacrificedWhenTargetedBySpell() {
        Permanent bears = reanimate(new GrizzlyBears());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, bears.getId());

        // Shock + the mannequin sacrifice trigger (on top) are on the stack
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Reanimated creature is sacrificed when targeted by an activated ability")
    void sacrificedWhenTargetedByAbility() {
        Permanent bears = reanimate(new GrizzlyBears());

        harness.addToBattlefield(player2, new ProdigalPyromancer());
        Permanent pyro = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Prodigal Pyromancer"))
                .findFirst().orElseThrow();
        pyro.setSummoningSick(false);

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(pyro),
                null, bears.getId());

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target a noncreature card in the graveyard")
    void cannotTargetNoncreatureCard() {
        Card shock = new Shock();
        harness.setGraveyard(player1, new ArrayList<>(List.of(shock)));
        harness.setHand(player1, List.of(new MakeshiftMannequin()));
        addCost();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
