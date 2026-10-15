package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CandlegroveWitch;
import com.github.laxika.magicalvibes.cards.d.DawnhartMentor;
import com.github.laxika.magicalvibes.cards.s.SilverBolt;
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

@CardUsed({MoonveilRegent.class, Terminate.class, GrizzlyBears.class, SavannahLions.class,
        CandlegroveWitch.class, DawnhartMentor.class, SilverBolt.class})
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

    @Test
    void drawsForAColoredSpellWithAnEmptyHand() {
        harness.addToBattlefield(player1, new MoonveilRegent());
        Card draw = new SilverBolt();
        harness.setLibrary(player1, List.of(draw));

        harness.castFromHand(player1, new CandlegroveWitch(), "{1}{W}");
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void discardsTheWholeHandButDrawsNothingForAColorlessSpell() {
        harness.addToBattlefield(player1, new MoonveilRegent());
        Card spell = new SilverBolt();
        Card firstDiscard = new CandlegroveWitch();
        Card secondDiscard = new DawnhartMentor();
        Card undrawn = new MoonveilRegent();
        harness.setHand(player1, List.of(spell, firstDiscard, secondDiscard));
        harness.setLibrary(player1, List.of(undrawn));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstDiscard, secondDiscard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawn);
    }

    @Test
    void opposingSpellDoesNotTriggerDiscardAndDraw() {
        harness.addToBattlefield(player1, new MoonveilRegent());
        Card kept = new DawnhartMentor();
        Card undrawn = new SilverBolt();
        harness.setHand(player1, List.of(kept));
        harness.setLibrary(player1, List.of(undrawn));

        harness.castFromHand(player2, new CandlegroveWitch(), "{1}{W}");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawn);
    }

    @Test
    void deathTriggerDoesNotCountTheDeadRegentOrOpposingPermanents() {
        Permanent regent = harness.addToBattlefieldAndReturn(player1, new MoonveilRegent());
        harness.addToBattlefield(player1, new SilverBolt());
        harness.addToBattlefield(player2, new CandlegroveWitch());
        harness.setLife(player2, 20);
        TestCards.mutableCard(regent).setToughness(0);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(regent.getCard());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void deathTriggerCountsColorsAtResolutionAndCanDamageACreature() {
        Permanent regent = harness.addToBattlefieldAndReturn(player1, new MoonveilRegent());
        harness.addToBattlefield(player1, new DawnhartMentor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CandlegroveWitch());
        TestCards.mutableCard(regent).setToughness(0);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.addToBattlefield(player1, new CandlegroveWitch());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }
}
