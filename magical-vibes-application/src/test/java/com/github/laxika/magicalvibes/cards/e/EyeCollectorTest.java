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
