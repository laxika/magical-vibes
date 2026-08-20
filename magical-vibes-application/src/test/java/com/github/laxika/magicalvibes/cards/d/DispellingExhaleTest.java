package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class DispellingExhaleTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell when a Dragon was beheld from the battlefield and its controller cannot pay {4}")
    void beheldDragonFromBattlefieldUsesFourManaTax() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player2, new DragonWhelp());
        GrizzlyBears targetSpell = castTargetSpellWithMana(3);
        harness.setHand(player2, List.of(new DispellingExhale()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstantWithBehold(player2, 0, targetSpell.getId(), List.of(dragon.getId()), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Counters a spell when a Dragon was beheld from hand and its controller cannot pay {4}")
    void beheldDragonFromHandUsesFourManaTax() {
        GrizzlyBears targetSpell = castTargetSpellWithMana(3);
        DragonWhelp dragon = new DragonWhelp();
        harness.setHand(player2, List.of(new DispellingExhale(), dragon));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstantWithBehold(player2, 0, targetSpell.getId(), List.of(), List.of(1));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Without behold, the target spell survives when its controller pays {2}")
    void withoutBeholdUsesTwoManaTax() {
        GrizzlyBears targetSpell = castTargetSpellWithMana(2);
        harness.setHand(player2, List.of(new DispellingExhale()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstantWithBehold(player2, 0, targetSpell.getId(), List.of(), List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    private GrizzlyBears castTargetSpellWithMana(int extraMana) {
        GrizzlyBears targetSpell = new GrizzlyBears();
        harness.setHand(player1, List.of(targetSpell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, extraMana);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        return targetSpell;
    }
}
