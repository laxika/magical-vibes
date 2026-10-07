package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.b.BumpInTheNight;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiderSpawning.class, WalkingCorpse.class, BumpInTheNight.class})
class SpiderSpawningTest extends BaseCardTest {

    @Test
    @DisplayName("Only the caster's graveyard contributes to the number of Spiders")
    void opponentGraveyardDoesNotContribute() {
        harness.setGraveyard(player1, List.of(new WalkingCorpse()));
        harness.setGraveyard(player2, List.of(new WalkingCorpse(), new WalkingCorpse()));
        harness.setHand(player1, List.of(new SpiderSpawning()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(countPermanents(player1, "Spider")).isEqualTo(1);
        assertThat(countPermanents(player2, "Spider")).isZero();
    }

    @Test
    @DisplayName("Counts creature cards when resolving rather than when cast")
    void countsGraveyardAtResolution() {
        harness.setGraveyard(player1, List.of(new WalkingCorpse()));
        harness.setHand(player1, List.of(new SpiderSpawning()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, 0);
        harness.setGraveyard(player1, List.of(new WalkingCorpse(), new WalkingCorpse()));
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Spider")).isEqualTo(2);
    }

    @Test
    @DisplayName("Creates no Spiders if the creature cards leave before resolution")
    void createsNoTokensAfterGraveyardIsEmptied() {
        harness.setGraveyard(player1, List.of(new WalkingCorpse()));
        harness.setHand(player1, List.of(new SpiderSpawning()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, 0);
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Spider")).isZero();
        harness.assertInGraveyard(player1, "Spider Spawning");
    }

    @Test
    @DisplayName("Creates one 1/2 Spider token with reach per creature card in graveyard")
    void createsSpiderTokensPerCreatureInGraveyard() {
        harness.setGraveyard(player1, List.of(new WalkingCorpse(), new WalkingCorpse(), new WalkingCorpse()));
        harness.setHand(player1, List.of(new SpiderSpawning()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        List<Permanent> spiders = findPermanents(player1, "Spider");

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

        harness.castAndResolveSorcery(player1, 0, 0);

        long spiderCount = countPermanents(player1, "Spider");

        assertThat(spiderCount).isEqualTo(0);
    }

    @Test
    @DisplayName("Non-creature cards in graveyard are not counted")
    void nonCreatureCardsNotCounted() {
        harness.setGraveyard(player1, List.of(new WalkingCorpse(), new BumpInTheNight()));
        harness.setHand(player1, List.of(new SpiderSpawning()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        long spiderCount = countPermanents(player1, "Spider");

        assertThat(spiderCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Flashback creates Spider tokens per creature card in graveyard")
    void flashbackCreatesSpiderTokens() {
        // Put 2 creature cards + Spider Spawning itself in graveyard
        harness.setGraveyard(player1, List.of(new SpiderSpawning(), new WalkingCorpse(), new WalkingCorpse()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castAndResolveFlashback(player1, 0, null);

        List<Permanent> spiders = findPermanents(player1, "Spider");

        // Only 2 tokens — Spider Spawning is a sorcery, not a creature card
        assertThat(spiders).hasSize(2);
    }

    @Test
    @DisplayName("Flashback exiles the card after resolving")
    void flashbackExilesAfterResolving() {
        harness.setGraveyard(player1, List.of(new SpiderSpawning()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castAndResolveFlashback(player1, 0, null);

        harness.assertNotInGraveyard(player1, "Spider Spawning");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Spider Spawning"));
    }

    @Test
    @DisplayName("Normal cast goes to graveyard after resolving")
    void normalCastGoesToGraveyard() {
        harness.setHand(player1, List.of(new SpiderSpawning()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Spider Spawning");
    }
}
