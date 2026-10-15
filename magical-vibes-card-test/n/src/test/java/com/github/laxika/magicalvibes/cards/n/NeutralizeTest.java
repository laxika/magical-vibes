package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OfOneMind;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({Neutralize.class, GrizzlyBears.class, OfOneMind.class})
class NeutralizeTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a target spell")
    void countersTargetSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Neutralize()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new Neutralize()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Neutralize");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Counters a noncreature spell without letting it draw cards")
    void countersNoncreatureSpell() {
        OfOneMind drawSpell = new OfOneMind();
        harness.setHand(player1, List.of(drawSpell));
        harness.setLibrary(player1, List.of(new Neutralize(), new Neutralize()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setHand(player2, List.of(new Neutralize()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, drawSpell.getId());

        harness.assertInGraveyard(player1, "Of One Mind");
        harness.assertNotInHand(player1, "Neutralize");
        harness.assertInGraveyard(player2, "Neutralize");
    }

    @Test
    @DisplayName("Cycling discards as a cost before the draw resolves")
    void cyclingDiscardsBeforeDrawing() {
        harness.setHand(player1, List.of(new Neutralize()));
        harness.setLibrary(player1, List.of(new OfOneMind()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Neutralize");
        harness.assertNotInHand(player1, "Neutralize");
        harness.assertNotInHand(player1, "Of One Mind");

        harness.passBothPriorities();

        harness.assertInHand(player1, "Of One Mind");
    }
}
