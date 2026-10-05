package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OwynShieldmaiden.class, YouthfulKnight.class, GrizzlyBears.class})
class OwynShieldmaidenTest extends BaseCardTest {

    @Test
    void createsTwoHastyTramplingHumanKnightsAfterAnotherHumanEnters() {
        harness.addToBattlefield(player1, new OwynShieldmaiden());
        harness.enterBattlefieldAndReturn(player1, new YouthfulKnight());

        advanceToCombat(player1);

        List<Permanent> tokens = humanKnightTokens();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(2);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
            assertThat(token.getCard().getSubtypes())
                    .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.KNIGHT);
            assertThat(gqs.hasKeyword(gd, token, Keyword.TRAMPLE)).isTrue();
            assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        });
    }

    @Test
    void drawsAfterCreatingTokensWhenTheyBringHumanCountToSix() {
        harness.addToBattlefield(player1, new OwynShieldmaiden());
        harness.enterBattlefieldAndReturn(player1, new YouthfulKnight());
        harness.addToBattlefield(player1, new YouthfulKnight());
        harness.addToBattlefield(player1, new YouthfulKnight());
        Card drawnCard = new YouthfulKnight();
        harness.setLibrary(player1, List.of(drawnCard));

        advanceToCombat(player1);

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    void doesNotTriggerWhenOnlyANonHumanEnters() {
        harness.addToBattlefield(player1, new OwynShieldmaiden());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);

        assertThat(humanKnightTokens()).isEmpty();
    }

    @Test
    void doesNotTriggerWhenOnlyOwynEnters() {
        harness.enterBattlefieldAndReturn(player1, new OwynShieldmaiden());

        advanceToCombat(player1);

        assertThat(humanKnightTokens()).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerForAHumanEnteringUnderOpponentsControl() {
        harness.addToBattlefield(player1, new OwynShieldmaiden());
        harness.enterBattlefieldAndReturn(player2, new YouthfulKnight());

        advanceToCombat(player1);

        assertThat(humanKnightTokens()).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerDuringOpponentsCombat() {
        harness.addToBattlefield(player1, new OwynShieldmaiden());
        harness.enterBattlefieldAndReturn(player1, new YouthfulKnight());

        advanceToCombat(player2);

        assertThat(humanKnightTokens()).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void createsTokensWithoutDrawingWhenHumanCountRemainsBelowSix() {
        harness.addToBattlefield(player1, new OwynShieldmaiden());
        harness.enterBattlefieldAndReturn(player1, new YouthfulKnight());
        harness.addToBattlefield(player1, new YouthfulKnight());
        Card drawnCard = new YouthfulKnight();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of());

        advanceToCombat(player1);

        assertThat(humanKnightTokens()).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void sixHumansAloneDoNotTriggerWithoutAnotherHumanEnteringThisTurn() {
        harness.addToBattlefield(player1, new OwynShieldmaiden());
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new YouthfulKnight());
        }
        Card drawnCard = new YouthfulKnight();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of());

        advanceToCombat(player1);

        assertThat(humanKnightTokens()).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void stillTriggersAfterTheHumanThatEnteredLeavesTheBattlefield() {
        harness.addToBattlefield(player1, new OwynShieldmaiden());
        Permanent human = harness.enterBattlefieldAndReturn(player1, new YouthfulKnight());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, human));

        advanceToCombat(player1);

        assertThat(humanKnightTokens()).hasSize(2);
    }

    @Test
    void triggersForAHumanThatEnteredBeforeOwyn() {
        harness.enterBattlefieldAndReturn(player1, new YouthfulKnight());
        harness.enterBattlefieldAndReturn(player1, new OwynShieldmaiden());

        advanceToCombat(player1);

        assertThat(humanKnightTokens()).hasSize(2);
    }

    @Test
    void resolvesAfterOwynLeavesTheBattlefield() {
        Permanent owyn = harness.addToBattlefieldAndReturn(player1, new OwynShieldmaiden());
        harness.enterBattlefieldAndReturn(player1, new YouthfulKnight());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.stack).hasSize(1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, owyn));
        harness.passBothPriorities();

        assertThat(humanKnightTokens()).hasSize(2);
    }

    private List<Permanent> humanKnightTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.HUMAN))
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.KNIGHT))
                .toList();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
    }
}
