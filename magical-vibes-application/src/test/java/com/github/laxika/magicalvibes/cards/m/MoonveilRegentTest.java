package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoonveilRegent.class, Terminate.class, GrizzlyBears.class, SavannahLions.class})
class MoonveilRegentTest extends BaseCardTest {

    @Test
    @DisplayName("May discard its controller's hand and draw for each color of the cast spell")
    void acceptsCastTriggerAndDrawsForEachSpellColor() {
        harness.addToBattlefield(player1, new MoonveilRegent());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card spell = new Terminate();
        Card discarded = new SavannahLions();
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new SavannahLions();
        harness.setHand(player1, List.of(spell, discarded));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(firstDraw, secondDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell, discarded);
    }

    @Test
    @DisplayName("Declining its cast trigger keeps the hand and library unchanged")
    void declinesCastTrigger() {
        harness.addToBattlefield(player1, new MoonveilRegent());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card spell = new Terminate();
        Card kept = new SavannahLions();
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new SavannahLions();
        harness.setHand(player1, List.of(spell, kept));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    @Test
    @DisplayName("When it dies, it deals damage equal to the distinct colors of controlled permanents")
    void dealsDamageForDistinctControlledPermanentColors() {
        Permanent dyingRegent = addCreatureReady(player1, new MoonveilRegent());
        harness.addToBattlefield(player1, new MoonveilRegent());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SavannahLions());
        harness.setLife(player2, 20);
        TestCards.mutableCard(dyingRegent).setToughness(0);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }
}
