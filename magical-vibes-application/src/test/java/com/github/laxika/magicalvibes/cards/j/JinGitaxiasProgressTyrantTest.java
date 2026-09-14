package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JinGitaxiasProgressTyrant.class, LightningBolt.class, MindStone.class})
class JinGitaxiasProgressTyrantTest extends BaseCardTest {

    @Test
    @DisplayName("Copies the first artifact, instant, or sorcery spell you cast each turn")
    void copiesOnlyOncePerTurn() {
        harness.addToBattlefield(player1, new JinGitaxiasProgressTyrant());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(11);
    }

    @Test
    @DisplayName("Copies a permanent spell as a token")
    void copiesPermanentSpellAsToken() {
        harness.addToBattlefield(player1, new JinGitaxiasProgressTyrant());
        harness.setHand(player1, List.of(new MindStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        List<Permanent> mindStones = findPermanents(player1, "Mind Stone");
        assertThat(mindStones).hasSize(2);
        assertThat(mindStones).filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    @DisplayName("Counters only the first opposing artifact, instant, or sorcery spell each turn")
    void countersOnlyOncePerTurn() {
        harness.addToBattlefield(player1, new JinGitaxiasProgressTyrant());
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, player1.getId());
        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Its two once-per-turn abilities track independently")
    void abilitiesTrackIndependently() {
        harness.addToBattlefield(player1, new JinGitaxiasProgressTyrant());
        harness.setHand(player1, List.of(new MindStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}
