package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({PestControl.class, Forest.class, GrizzlyBears.class, LlanowarElves.class, Memnite.class})
class PestControlTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all nonland permanents with mana value 1 or less")
    void destroysMatchingPermanentsAcrossBothBattlefields() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addToBattlefield(player2, new Memnite());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new PestControl()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Memnite");
        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Memnite");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cycling discards Pest Control and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new PestControl()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Pest Control");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
