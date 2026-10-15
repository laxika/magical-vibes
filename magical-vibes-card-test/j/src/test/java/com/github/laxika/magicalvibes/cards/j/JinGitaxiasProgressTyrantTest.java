package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.i.IronApprentice;
import com.github.laxika.magicalvibes.cards.k.KaitosPursuit;
import com.github.laxika.magicalvibes.cards.k.KamiOfTerribleSecrets;
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

@CardUsed({JinGitaxiasProgressTyrant.class, LightningBolt.class, MindStone.class,
        IronApprentice.class, KamiOfTerribleSecrets.class, KaitosPursuit.class})
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

    @Test
    @DisplayName("A nonartifact creature does not consume the controller's copy trigger")
    void nonartifactCreatureDoesNotConsumeCopyTrigger() {
        harness.addToBattlefield(player1, new JinGitaxiasProgressTyrant());
        harness.setHand(player1, List.of(new KamiOfTerribleSecrets(), new IronApprentice()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Kami of Terrible Secrets")).hasSize(1);
        assertThat(findPermanents(player1, "Iron Apprentice")).hasSize(2);
        assertThat(findPermanents(player1, "Iron Apprentice"))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    @DisplayName("A nonartifact creature does not consume the opponent's counter trigger")
    void nonartifactCreatureDoesNotConsumeCounterTrigger() {
        harness.forceActivePlayer(player2);
        harness.addToBattlefield(player1, new JinGitaxiasProgressTyrant());
        harness.setHand(player2, List.of(new KamiOfTerribleSecrets(), new IronApprentice(), new IronApprentice()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 5);

        harness.castCreature(player2, 0);
        resolveAllTriggers();
        harness.castCreature(player2, 0);
        resolveAllTriggers();
        assertThat(findPermanents(player2, "Iron Apprentice")).isEmpty();
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Kami of Terrible Secrets")).hasSize(1);
        assertThat(findPermanents(player2, "Iron Apprentice")).hasSize(1);
    }

    @Test
    @DisplayName("Copies only the first artifact creature cast each turn")
    void copiesOnlyFirstArtifactCreature() {
        harness.addToBattlefield(player1, new JinGitaxiasProgressTyrant());
        harness.setHand(player1, List.of(new IronApprentice(), new IronApprentice()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Iron Apprentice")).hasSize(3);
        assertThat(findPermanents(player1, "Iron Apprentice"))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    @DisplayName("Copies a spell even after an opposing Jin-Gitaxias counters the original")
    void copiesCounteredOriginalAndCopyIsNotCast() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new JinGitaxiasProgressTyrant());
        harness.addToBattlefield(player2, new JinGitaxiasProgressTyrant());
        harness.setHand(player1, List.of(new IronApprentice()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Iron Apprentice")).hasSize(1);
        assertThat(findPermanents(player1, "Iron Apprentice").getFirst().getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Copies a sorcery and retains its original target when new targets are declined")
    void copiesSorcery() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new JinGitaxiasProgressTyrant());
        harness.setHand(player1, List.of(new KaitosPursuit()));
        harness.setHand(player2, List.of(new IronApprentice(), new IronApprentice(),
                new IronApprentice(), new IronApprentice()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Counters an opposing sorcery")
    void countersSorcery() {
        harness.forceActivePlayer(player2);
        harness.addToBattlefield(player1, new JinGitaxiasProgressTyrant());
        harness.setHand(player2, List.of(new KaitosPursuit()));
        harness.setHand(player1, List.of(new IronApprentice(), new IronApprentice()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castSorcery(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }
}
