package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AetherVial;
import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.o.Oxidize;
import com.github.laxika.magicalvibes.cards.v.Vex;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({LastWord.class, CrazedGoblin.class, Vex.class, Oxidize.class, AetherVial.class})
class LastWordTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a target spell")
    void countersTargetSpell() {
        CrazedGoblin goblin = new CrazedGoblin();
        harness.setHand(player1, List.of(goblin));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.setHand(player2, List.of(new LastWord()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, goblin.getId());

        harness.assertInGraveyard(player1, "Crazed Goblin");
        harness.assertInGraveyard(player2, "Last Word");
    }

    @Test
    @DisplayName("Cannot be countered")
    void cannotBeCountered() {
        CrazedGoblin goblin = new CrazedGoblin();
        LastWord lastWord = new LastWord();
        harness.setHand(player1, List.of(goblin, new Vex()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(lastWord));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, goblin.getId());
        harness.castInstant(player1, 0, lastWord.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Crazed Goblin");
        harness.assertInGraveyard(player1, "Vex");
        harness.assertInGraveyard(player2, "Last Word");
    }

    @Test
    @DisplayName("Counters a target noncreature spell")
    void countersTargetNoncreatureSpell() {
        AetherVial vial = new AetherVial();
        Permanent vialPermanent = harness.addToBattlefieldAndReturn(player1, vial);

        Oxidize oxidize = new Oxidize();
        harness.setHand(player1, List.of(oxidize));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new LastWord()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, vialPermanent.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, oxidize.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Oxidize");
        harness.assertOnBattlefield(player1, "Aether Vial");
        harness.assertInGraveyard(player2, "Last Word");
    }
}
