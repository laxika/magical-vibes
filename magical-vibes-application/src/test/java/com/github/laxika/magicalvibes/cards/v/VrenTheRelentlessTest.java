package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VrenTheRelentless.class, DoomBlade.class, GrizzlyBears.class,
        SwordsToPlowshares.class, WrathOfGod.class})
class VrenTheRelentlessTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles an opponent's creature instead of letting it die")
    void exilesOpponentCreatureInsteadOfDying() {
        harness.addToBattlefield(player1, new VrenTheRelentless());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Counts opponent creatures exiled this turn, including tokens, and makes other Rats larger")
    void createsRatTokensBasedOnCreaturesExiledThisTurn() {
        harness.addToBattlefield(player1, new VrenTheRelentless());
        Permanent opponentBearsOne = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opponentBearsTwo = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opponentToken = addTokenCreature(player2);
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(
                new SwordsToPlowshares(), new SwordsToPlowshares(),
                new SwordsToPlowshares(), new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        for (var target : List.of(opponentBearsOne, opponentBearsTwo, opponentToken, ownBears)) {
            harness.castInstant(player1, 0, target.getId());
            harness.passBothPriorities();
        }

        advanceToEndStep(player2);
        harness.passBothPriorities();

        List<Permanent> rats = findPermanents(player1, "Rat");
        assertThat(rats).hasSize(3);
        assertThat(rats).allSatisfy(rat -> {
            assertThat(gqs.getEffectivePower(gd, rat)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, rat)).isEqualTo(4);
        });
    }

    @Test
    @DisplayName("Still exiles an opponent's creature when Vren leaves in the same destruction event")
    void replacementAppliesWhenVrenLeavesAtTheSameTime() {
        harness.addToBattlefield(player1, new VrenTheRelentless());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Vren, the Relentless");
        harness.assertInGraveyard(player1, "Vren, the Relentless");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getName().equals("Grizzly Bears"));
    }

    private Permanent addTokenCreature(Player owner) {
        Card tokenCard = new Card();
        tokenCard.setName("Bear Token");
        tokenCard.setOwnerId(owner.getId());
        tokenCard.setType(CardType.CREATURE);
        tokenCard.setManaCost("");
        tokenCard.setToken(true);
        tokenCard.setColor(CardColor.GREEN);
        tokenCard.setPower(2);
        tokenCard.setToughness(2);
        tokenCard.setSubtypes(List.of(CardSubtype.BEAR));
        return harness.addToBattlefieldAndReturn(owner, tokenCard);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
