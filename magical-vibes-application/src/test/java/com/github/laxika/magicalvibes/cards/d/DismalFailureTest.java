package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SpellbreakerBehemoth;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DismalFailure.class, AvatarOfMight.class, Forest.class, GrizzlyBears.class,
        SpellbreakerBehemoth.class})
class DismalFailureTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the target spell and its controller discards a card")
    void countersSpellAndMakesItsControllerDiscard() {
        GrizzlyBears bears = new GrizzlyBears();
        GrizzlyBears cardToDiscard = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(bears, cardToDiscard)));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new DismalFailure()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Still makes the controller discard when the target spell cannot be countered")
    void discardsFromUncounterableSpellController() {
        harness.addToBattlefield(player1, new SpellbreakerBehemoth());
        AvatarOfMight avatar = new AvatarOfMight();
        GrizzlyBears cardToDiscard = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(avatar, cardToDiscard)));
        harness.addMana(player1, ManaColor.GREEN, 8);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.setHand(player2, List.of(new DismalFailure()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, avatar.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        harness.assertNotInGraveyard(player1, "Avatar of Might");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Avatar of Might");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Fizzles without a discard if the target spell leaves the stack")
    void fizzlesIfTargetSpellLeavesStack() {
        GrizzlyBears bears = new GrizzlyBears();
        GrizzlyBears cardToKeep = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(bears, cardToKeep)));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new DismalFailure()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        gd.stack.removeIf(entry -> entry.getCard().getName().equals("Grizzly Bears"));

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cardToKeep);
    }
}
