package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.SpikeshellHarrier;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RisenNecroregent.class, SpikeshellHarrier.class})
class RisenNecroregentTest extends BaseCardTest {

    @Test
    @DisplayName("At max speed, creates a 2/2 black Zombie token at your end step")
    void createsZombieTokenAtMaxSpeed() {
        harness.addToBattlefield(player1, new RisenNecroregent());
        gd.playerSpeeds.put(player1.getId(), 4);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        Permanent zombie = tokens.getFirst();
        assertThat(zombie.getCard().getPower()).isEqualTo(2);
        assertThat(zombie.getCard().getToughness()).isEqualTo(2);
        assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(zombie.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
    }

    @Test
    @DisplayName("Does not create a Zombie token below max speed")
    void doesNotCreateZombieTokenBelowMaxSpeed() {
        harness.addToBattlefield(player1, new RisenNecroregent());
        gd.playerSpeeds.put(player1.getId(), 3);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's end step")
    void doesNotTriggerDuringOpponentEndStep() {
        harness.addToBattlefield(player1, new RisenNecroregent());
        gd.playerSpeeds.put(player1.getId(), 4);

        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Entering with start your engines starts the controller's speed")
    void startsControllerSpeedWhenCast() {
        harness.castFromHand(player1, new RisenNecroregent(), "{4}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerSpeeds.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("A max-speed trigger still creates its token after the controller's speed decreases")
    void createsTokenIfSpeedDecreasesAfterTriggering() {
        Permanent necroregent = harness.addToBattlefieldAndReturn(player1, new RisenNecroregent());
        gd.playerSpeeds.put(player1.getId(), 4);

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.enterBattlefieldAndReturn(player2, new SpikeshellHarrier());
        harness.handlePermanentChosen(player2, necroregent.getId());
        harness.passBothPriorities();
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(3);
        harness.assertInHand(player1, "Risen Necroregent");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
