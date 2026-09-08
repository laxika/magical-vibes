package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.c.CallousDeceiver;
import com.github.laxika.magicalvibes.cards.g.GildedLotus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.ReachThroughMists;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class YukiOnnaTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by destroying a target artifact")
    void entersByDestroyingTargetArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GildedLotus());
        harness.setHand(player1, List.of(new YukiOnna()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, 0, artifact.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Gilded Lotus");
        harness.assertOnBattlefield(player1, "Yuki-Onna");
    }

    @Test
    @DisplayName("Casting a Spirit spell may return Yuki-Onna to its owner's hand")
    void spiritSpellReturnsYukiOnna() {
        addYukiOnna();
        harness.setHand(player1, List.of(new CallousDeceiver()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Yuki-Onna");
    }

    @Test
    @DisplayName("Casting an Arcane spell may return Yuki-Onna to its owner's hand")
    void arcaneSpellReturnsYukiOnna() {
        addYukiOnna();
        harness.setHand(player1, List.of(new ReachThroughMists()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Yuki-Onna");
    }

    @Test
    @DisplayName("Declining the cast trigger leaves Yuki-Onna on the battlefield")
    void decliningCastTriggerLeavesYukiOnnaOnBattlefield() {
        addYukiOnna();
        harness.setHand(player1, List.of(new ReachThroughMists()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Yuki-Onna");
    }

    @Test
    @DisplayName("A non-Spirit non-Arcane spell does not trigger Yuki-Onna")
    void unrelatedSpellDoesNotTrigger() {
        addYukiOnna();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        harness.assertOnBattlefield(player1, "Yuki-Onna");
    }

    @Test
    @DisplayName("The ETB ability cannot target a nonartifact permanent")
    void cannotTargetNonartifact() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new YukiOnna()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addYukiOnna() {
        harness.addToBattlefield(player1, new YukiOnna());
    }
}
