package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BarkKnuckleBoxer.class, Shock.class})
class BarkKnuckleBoxerTest extends BaseCardTest {

    @Test
    @DisplayName("Gains indestructible when its controller expends four")
    void gainsIndestructibleWhenControllerExpendsFour() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BarkKnuckleBoxer(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        Permanent boxer = findPermanent(player1, "Bark-Knuckle Boxer");
        assertThat(gqs.hasKeyword(gd, boxer, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Does not gain indestructible before its controller expends four")
    void doesNotGainIndestructibleBelowExpendThreshold() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BarkKnuckleBoxer(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, player2.getId());

        Permanent boxer = findPermanent(player1, "Bark-Knuckle Boxer");
        assertThat(gqs.hasKeyword(gd, boxer, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Indestructible wears off at end of turn")
    void indestructibleWearsOffAtEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BarkKnuckleBoxer(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        Permanent boxer = findPermanent(player1, "Bark-Knuckle Boxer");
        assertThat(gqs.hasKeyword(gd, boxer, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, boxer, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Spending the fourth mana on a second Boxer protects only the Boxer already in play")
    void onlyBoxerAlreadyOnBattlefieldTriggers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BarkKnuckleBoxer(), new BarkKnuckleBoxer()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent first = findPermanent(player1, "Bark-Knuckle Boxer");
        harness.castCreature(player1, 0);

        assertThat(gqs.hasKeyword(gd, first, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, first, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.passBothPriorities();

        Permanent second = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(first.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, second, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("An opponent expending four does not protect your Boxer")
    void opponentsManaDoesNotTrigger() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent boxer = harness.addToBattlefieldAndReturn(player1, new BarkKnuckleBoxer());
        harness.setHand(player2, List.of(new BarkKnuckleBoxer(), new BarkKnuckleBoxer()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, boxer, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Expend protection resolves before the spell that spends the fourth mana")
    void survivesLethalSpellThatCrossesThreshold() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BarkKnuckleBoxer(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent boxer = findPermanent(player1, "Bark-Knuckle Boxer");
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, boxer.getId());

        assertThat(gqs.hasKeyword(gd, boxer, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, boxer, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bark-Knuckle Boxer");
        harness.assertNotInGraveyard(player1, "Bark-Knuckle Boxer");
    }

    @Test
    @DisplayName("Its controller can expend four during an opponent's turn")
    void triggersDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent boxer = harness.addToBattlefieldAndReturn(player1, new BarkKnuckleBoxer());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 4);

        for (int i = 0; i < 3; i++) {
            harness.castAndResolveInstant(player1, 0, player2.getId());
        }
        assertThat(gqs.hasKeyword(gd, boxer, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gqs.hasKeyword(gd, boxer, Keyword.INDESTRUCTIBLE)).isTrue();
    }
}
