package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EyeCollector.class, Forest.class})
class EyeCollectorTest extends BaseCardTest {

    @Test
    @DisplayName("Each player mills a card when Eye Collector deals combat damage to a player")
    void eachPlayerMillsOnCombatDamage() {
        addAttackingEyeCollector(player1);
        setLibrary(player1, 2);
        setLibrary(player2, 2);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Eye Collector does not trigger when it deals no combat damage to a player")
    void noTriggerWhenBlocked() {
        addAttackingEyeCollector(player1);
        Permanent blocker = addCreatureReady(player2, new EyeCollector());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        setLibrary(player1, 2);
        setLibrary(player2, 2);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Milling both players is one combat damage triggered ability")
    void bothPlayersMillFromOneTrigger() {
        addAttackingEyeCollector(player1);
        setLibrary(player1, 2);
        setLibrary(player2, 2);

        resolveCombat();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An empty controller library does not stop the opponent from milling")
    void emptyControllerLibraryDoesNotStopOpponentMill() {
        addAttackingEyeCollector(player1);
        setLibrary(player1, 0);
        setLibrary(player2, 2);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Both players mill their top card when the second player controls Eye Collector")
    void secondPlayerControllerMillsBothTopCards() {
        addAttackingEyeCollector(player2);
        Forest firstTop = new Forest();
        Forest secondTop = new Forest();
        Forest firstBottom = new Forest();
        Forest secondBottom = new Forest();
        harness.setLibrary(player1, List.of(firstTop, firstBottom));
        harness.setLibrary(player2, List.of(secondTop, secondBottom));

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstBottom);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(firstTop);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(secondBottom);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(secondTop);
    }

    private Permanent addAttackingEyeCollector(Player player) {
        Permanent eyeCollector = addCreatureReady(player, new EyeCollector());
        eyeCollector.setAttacking(true);
        return eyeCollector;
    }

    private void setLibrary(Player player, int count) {
        List<com.github.laxika.magicalvibes.model.Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Forest());
        }
        harness.setLibrary(player, cards);
    }
}
