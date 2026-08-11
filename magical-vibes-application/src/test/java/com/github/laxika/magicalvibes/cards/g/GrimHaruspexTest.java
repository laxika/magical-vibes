package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GrimHaruspexTest extends BaseCardTest {

    @Test
    void drawsCardWhenAnotherNontokenCreatureYouControlDies() {
        Card drawnCard = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addToBattlefield(player1, new GrimHaruspex());
        harness.addToBattlefield(player1, new GrizzlyBears());

        killWithShock(player2, player1, "Grizzly Bears");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void doesNotDrawWhenTokenCreatureYouControlDies() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new GrimHaruspex());
        harness.addToBattlefield(player1, tokenCreature());

        killWithShock(player2, player1, "Soldier Token");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotDrawWhenOpponentCreatureDies() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new GrimHaruspex());
        harness.addToBattlefield(player2, new GrizzlyBears());

        killWithShock(player1, player2, "Grizzly Bears");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void canBeCastFaceDownAndTurnedFaceUpForMorphCost() {
        harness.setHand(player1, List.of(new GrimHaruspex()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent grimHaruspex = findPermanent(player1, "Grim Haruspex");
        assertThat(grimHaruspex.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.BLACK, 1);
        int permanentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(grimHaruspex);
        harness.turnFaceUp(player1, permanentIndex);
        harness.passBothPriorities();

        assertThat(grimHaruspex.isFaceDown()).isFalse();
    }

    private void killWithShock(Player caster, Player targetController, String targetName) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(targetController, targetName);
        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
    }

    private Card tokenCreature() {
        Card token = new Card();
        token.setName("Soldier Token");
        token.setType(CardType.CREATURE);
        token.setManaCost("");
        token.setColor(CardColor.WHITE);
        token.setPower(1);
        token.setToughness(1);
        token.setToken(true);
        return token;
    }
}
