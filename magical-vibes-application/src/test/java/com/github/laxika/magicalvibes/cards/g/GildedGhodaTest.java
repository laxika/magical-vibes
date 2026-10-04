package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GildedGhoda.class})
class GildedGhodaTest extends BaseCardTest {

    @Test
    @DisplayName("Saddle taps another creature and enables the Treasure attack trigger")
    void saddleEnablesTreasureAttackTrigger() {
        Permanent ghoda = addCreatureReady(player1, new GildedGhoda());
        Permanent helper = harness.addToBattlefieldAndReturn(player1, new GildedGhoda());
        helper.setSummoningSick(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(helper.isTapped()).isTrue();
        assertThat(ghoda.isTapped()).isFalse();
        assertThat(ghoda.isSaddled()).isTrue();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("The Treasure trigger resolves after the saddled attacker leaves the battlefield")
    void createsTreasureAfterSourceLeavesBattlefield() {
        Permanent ghoda = addCreatureReady(player1, new GildedGhoda());
        ghoda.setSaddled(true);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(ghoda);
        gd.playerGraveyards.get(player1.getId()).add(ghoda.getCard());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Attacking while saddled creates a Treasure token")
    void attacksWhileSaddledCreatesTreasure() {
        Permanent ghoda = addCreatureReady(player1, new GildedGhoda());
        ghoda.setSaddled(true);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Attacking while not saddled does not create a Treasure token")
    void doesNotCreateTreasureWhenNotSaddled() {
        addCreatureReady(player1, new GildedGhoda());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("The attack trigger checks saddled when attackers are declared")
    void checksSaddledAtDeclaration() {
        Permanent ghoda = addCreatureReady(player1, new GildedGhoda());

        declareAttackers(player1, List.of(0));
        ghoda.setSaddled(true);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }
}
