package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.CallousDeceiver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.ReachThroughMists;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KiriOnnaTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by returning a target creature to its owner's hand")
    void entersByReturningTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new KiriOnna()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Kiri-Onna");
    }

    @Test
    @DisplayName("Casting a Spirit spell may return Kiri-Onna to its owner's hand")
    void spiritSpellReturnsKiriOnna() {
        addKiriOnna();
        harness.setHand(player1, List.of(new CallousDeceiver()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Kiri-Onna");
    }

    @Test
    @DisplayName("Casting an Arcane spell may return Kiri-Onna to its owner's hand")
    void arcaneSpellReturnsKiriOnna() {
        addKiriOnna();
        harness.setHand(player1, List.of(new ReachThroughMists()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Kiri-Onna");
    }

    @Test
    @DisplayName("Declining the cast trigger leaves Kiri-Onna on the battlefield")
    void decliningCastTriggerLeavesKiriOnnaOnBattlefield() {
        addKiriOnna();
        harness.setHand(player1, List.of(new ReachThroughMists()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Kiri-Onna");
    }

    @Test
    @DisplayName("A non-Spirit non-Arcane spell does not trigger Kiri-Onna")
    void unrelatedSpellDoesNotTrigger() {
        Permanent kiriOnna = addKiriOnna();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kiriOnna);
    }

    @Test
    @DisplayName("The ETB ability cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2,
                new com.github.laxika.magicalvibes.cards.g.GildedLotus());
        harness.setHand(player1, List.of(new KiriOnna()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addKiriOnna() {
        return harness.addToBattlefieldAndReturn(player1, new KiriOnna());
    }
}
