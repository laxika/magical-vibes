package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpiderSpawningTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one 1/2 Spider token with reach per creature card in graveyard")
    void createsSpiderTokensPerCreatureInGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new SpiderSpawning()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        List<Permanent> spiders = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Spider"))
                .toList();

        assertThat(spiders).hasSize(3);

        for (Permanent spider : spiders) {
            assertThat(spider.getCard().getPower()).isEqualTo(1);
            assertThat(spider.getCard().getToughness()).isEqualTo(2);
            assertThat(spider.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(spider.getCard().getSubtypes()).contains(CardSubtype.SPIDER);
            assertThat(spider.getCard().getKeywords()).contains(Keyword.REACH);
            assertThat(spider.isTapped()).isFalse();
        }
    }

    @Test
    @DisplayName("No tokens created when graveyard has no creature cards")
    void noTokensWithEmptyGraveyard() {
        harness.setHand(player1, List.of(new SpiderSpawning()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        long spiderCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Spider"))
                .count();

        assertThat(spiderCount).isEqualTo(0);
    }

    @Test
    @DisplayName("Non-creature cards in graveyard are not counted")
    void nonCreatureCardsNotCounted() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LightningBolt()));
        harness.setHand(player1, List.of(new SpiderSpawning()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        long spiderCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Spider"))
                .count();

        assertThat(spiderCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Flashback creates Spider tokens per creature card in graveyard")
    void flashbackCreatesSpiderTokens() {
        // Put 2 creature cards + Spider Spawning itself in graveyard
        harness.setGraveyard(player1, List.of(new SpiderSpawning(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        List<Permanent> spiders = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Spider"))
                .toList();

        // Only 2 tokens — Spider Spawning is a sorcery, not a creature card
        assertThat(spiders).hasSize(2);
    }

    @Test
    @DisplayName("Flashback exiles the card after resolving")
    void flashbackExilesAfterResolving() {
        harness.setGraveyard(player1, List.of(new SpiderSpawning()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Spider Spawning"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Spider Spawning"));
    }

    @Test
    @DisplayName("Normal cast goes to graveyard after resolving")
    void normalCastGoesToGraveyard() {
        harness.setHand(player1, List.of(new SpiderSpawning()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Spider Spawning"));
    }
}
