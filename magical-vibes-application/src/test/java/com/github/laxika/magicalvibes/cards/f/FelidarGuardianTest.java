package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.ImplementOfImprovement;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FelidarGuardian.class, ImplementOfImprovement.class, Ornithopter.class})
class FelidarGuardianTest extends BaseCardTest {

    private void castGuardian() {
        harness.castFromHand(player1, new FelidarGuardian(), "{3}{W}");
    }

    @Test
    @DisplayName("ETB may flicker another permanent you control")
    void flickersChosenPermanent() {
        harness.addToBattlefield(player1, new Ornithopter());
        UUID targetId = harness.getPermanentId(player1, "Ornithopter");
        castGuardian();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Ornithopter");
        assertThat(harness.getPermanentId(player1, "Ornithopter")).isNotEqualTo(targetId);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Ornithopter"));
    }

    @Test
    @DisplayName("Declining the ETB ability leaves the target unchanged")
    void decliningDoesNotFlicker() {
        harness.addToBattlefield(player1, new Ornithopter());
        UUID targetId = harness.getPermanentId(player1, "Ornithopter");
        castGuardian();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(harness.getPermanentId(player1, "Ornithopter")).isEqualTo(targetId);
    }

    @Test
    @DisplayName("The ETB ability is not put on the stack without another legal permanent")
    void noOtherPermanentMeansNoTrigger() {
        castGuardian();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Felidar Guardian");
    }

    @Test
    @DisplayName("An opponent's permanent is not a legal target")
    void cannotTargetOpponentsPermanent() {
        harness.addToBattlefield(player2, new Ornithopter());
        castGuardian();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Ornithopter");
        harness.assertOnBattlefield(player1, "Felidar Guardian");
    }

    @Test
    void flickersNoncreatureArtifactWithoutTriggeringItsGraveyardAbility() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ImplementOfImprovement());
        artifact.tap();
        castGuardian();
        int handSize = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Implement of Improvement"))
                .findFirst().orElseThrow();
        assertThat(returned.getId()).isNotEqualTo(artifact.getId());
        assertThat(returned.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    void stolenPermanentReturnsUnderItsOwnersControl() {
        Ornithopter card = new Ornithopter();
        card.setOwnerId(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player1, card);
        gd.stolenCreatures.put(target.getId(), player2.getId());
        castGuardian();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertOnBattlefield(player2, "Ornithopter");
        assertThat(harness.getPermanentId(player2, "Ornithopter")).isNotEqualTo(target.getId());
    }

    @Test
    void tokenCopyDoesNotReturnAfterBeingExiled() {
        Ornithopter token = new Ornithopter();
        token.setToken(true);
        Permanent target = harness.addToBattlefieldAndReturn(player1, token);
        castGuardian();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertOnBattlefield(player1, "Felidar Guardian");
    }

    @Test
    void targetLeavingBeforeResolutionIsNotReturned() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        castGuardian();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.getPermanentRemovalService().removePermanentToExile(gd, target);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Ornithopter");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target.getCard());
    }
}
