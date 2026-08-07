package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardToHandReturn;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LiesaForgottenArchangelTest extends BaseCardTest {

    private Permanent addTokenCreature(UUID ownerId) {
        Card tokenCard = new Card();
        tokenCard.setName("Bear Token");
        tokenCard.setType(CardType.CREATURE);
        tokenCard.setManaCost("");
        tokenCard.setToken(true);
        tokenCard.setColor(CardColor.GREEN);
        tokenCard.setPower(2);
        tokenCard.setToughness(2);
        tokenCard.setSubtypes(List.of(CardSubtype.BEAR));
        Permanent tokenPerm = new Permanent(tokenCard);
        gd.playerBattlefields.get(ownerId).add(tokenPerm);
        return tokenPerm;
    }

    private boolean isExiled(String cardName) {
        return gd.exiledCards.stream().anyMatch(e -> e.card().getName().equals(cardName));
    }

    @Test
    @DisplayName("Own nontoken creature dies — returned to hand at the beginning of the next end step")
    void ownNontokenCreatureReturnsAtEndStep() {
        harness.addToBattlefield(player1, new LiesaForgottenArchangel());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsPermId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, bearsPermId);
        harness.passBothPriorities(); // Shock resolves, bears die

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Liesa, Forgotten Archangel"));

        harness.passBothPriorities(); // Resolve Liesa's trigger — registers the delayed return
        assertThat(gd.getDelayedActions(DelayedGraveyardToHandReturn.class)).hasSize(1);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gs.advanceStep(gd);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Own token creature dying does not trigger the delayed return")
    void ownTokenCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new LiesaForgottenArchangel());
        Permanent token = addTokenCreature(player1.getId());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, token.getId());
        harness.passBothPriorities(); // Shock resolves, token dies

        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.getDelayedActions(DelayedGraveyardToHandReturn.class)).isEmpty();
    }

    @Test
    @DisplayName("Liesa itself dying does not trigger its own return")
    void liesaItselfDoesNotTrigger() {
        harness.addToBattlefield(player1, new LiesaForgottenArchangel());
        UUID liesaPermId = harness.getPermanentId(player1, "Liesa, Forgotten Archangel");

        // 4/5 — five points of damage across three Shocks (the third only needs one).
        harness.setHand(player2, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castInstant(player2, 0, liesaPermId);
        harness.passBothPriorities();
        harness.castInstant(player2, 0, liesaPermId);
        harness.passBothPriorities();
        harness.castInstant(player2, 0, liesaPermId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Liesa, Forgotten Archangel");
        assertThat(gd.getDelayedActions(DelayedGraveyardToHandReturn.class)).isEmpty();
    }

    @Test
    @DisplayName("Opponent's dying creature is exiled instead of going to its graveyard")
    void opponentCreatureIsExiledInsteadOfDying() {
        harness.addToBattlefield(player1, new LiesaForgottenArchangel());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsPermId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bearsPermId);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(isExiled("Grizzly Bears")).isTrue();
    }

    @Test
    @DisplayName("Without Liesa, an opponent's dying creature goes to its graveyard normally")
    void withoutLiesaOpponentCreatureGoesToGraveyard() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsPermId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bearsPermId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(isExiled("Grizzly Bears")).isFalse();
    }
}
