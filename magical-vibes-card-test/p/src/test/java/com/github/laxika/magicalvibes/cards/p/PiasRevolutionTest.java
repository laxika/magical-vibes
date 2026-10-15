package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PiasRevolution.class, MindStone.class, Naturalize.class})
class PiasRevolutionTest extends BaseCardTest {

    @Test
    void opponentLetsNontokenArtifactReturnToHand() {
        harness.addToBattlefield(player1, new PiasRevolution());
        harness.addToBattlefield(player1, new MindStone());
        UUID mindStoneId = harness.getPermanentId(player1, "Mind Stone");

        destroyArtifact(mindStoneId);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInHand(player1, "Mind Stone");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void opponentAcceptsDamageAndArtifactStaysInGraveyard() {
        harness.addToBattlefield(player1, new PiasRevolution());
        harness.addToBattlefield(player1, new MindStone());
        UUID mindStoneId = harness.getPermanentId(player1, "Mind Stone");

        destroyArtifact(mindStoneId);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player1, "Mind Stone");
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Mind Stone"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void doesNotTriggerForArtifactToken() {
        harness.addToBattlefield(player1, new PiasRevolution());
        harness.addToBattlefield(player1, createArtifactToken());
        UUID tokenId = harness.getPermanentId(player1, "Servo");

        destroyArtifact(tokenId);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void doesNotTriggerForArtifactEnteringOpponentsGraveyard() {
        harness.addToBattlefield(player1, new PiasRevolution());
        harness.addToBattlefield(player2, new MindStone());

        destroyArtifact(harness.getPermanentId(player2, "Mind Stone"));

        harness.assertInGraveyard(player2, "Mind Stone");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void doesNotTriggerForNonartifactEnteringYourGraveyard() {
        harness.addToBattlefield(player1, new PiasRevolution());

        destroyArtifact(harness.getPermanentId(player1, "Pia's Revolution"));

        harness.assertInGraveyard(player1, "Pia's Revolution");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    void stillDealsDamageAfterSourceLeavesBattlefield() {
        harness.addToBattlefield(player1, new PiasRevolution());
        harness.addToBattlefield(player1, new MindStone());
        UUID revolutionId = harness.getPermanentId(player1, "Pia's Revolution");

        destroyArtifact(harness.getPermanentId(player1, "Mind Stone"));
        harness.handlePermanentChosen(player1, player2.getId());
        destroyArtifact(revolutionId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player1, "Pia's Revolution");
        harness.assertInGraveyard(player1, "Mind Stone");
        harness.assertNotInHand(player1, "Mind Stone");
        harness.assertLife(player2, 17);
    }

    private void destroyArtifact(UUID artifactId) {
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, artifactId);
    }

    private Card createArtifactToken() {
        Card token = new Card();
        token.setName("Servo");
        token.setType(CardType.ARTIFACT);
        token.setToken(true);
        return token;
    }
}
