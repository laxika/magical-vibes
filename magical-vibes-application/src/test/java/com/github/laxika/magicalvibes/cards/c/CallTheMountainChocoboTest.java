package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CallTheMountainChocobo.class, Forest.class, Mountain.class})
class CallTheMountainChocoboTest extends BaseCardTest {

    @Test
    @DisplayName("The spell searches for a Mountain and creates a Bird token")
    void searchesForMountainAndCreatesBird() {
        Mountain mountain = castAndChooseMountain();

        assertThat(gd.playerHands.get(player1.getId())).contains(mountain);
        Permanent bird = birdToken();
        assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bird)).isEqualTo(2);
    }

    @Test
    @DisplayName("The Bird token gets +1/+0 when a land you control enters")
    void birdGetsLandfallBoostUntilCleanup() {
        castAndChooseMountain();
        Permanent bird = birdToken();

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bird)).isEqualTo(2);

        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(2);
    }

    @Test
    @DisplayName("Flashback resolves the spell and exiles it")
    void flashbackResolvesAndExiles() {
        CallTheMountainChocobo spell = new CallTheMountainChocobo();
        harness.setGraveyard(player1, List.of(spell));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(birdToken()).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(spell);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("Failing to find an available Mountain still creates the Bird")
    void failingToFindStillCreatesBird() {
        Mountain mountain = new Mountain();
        harness.setHand(player1, List.of(new CallTheMountainChocobo()));
        harness.setLibrary(player1, List.of(mountain));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(mountain);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gqs.getEffectivePower(gd, birdToken())).isEqualTo(2);
    }

    @Test
    @DisplayName("An empty library does not prevent creating the Bird")
    void emptyLibraryStillCreatesBird() {
        harness.setHand(player1, List.of(new CallTheMountainChocobo()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gqs.getEffectivePower(gd, birdToken())).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof CallTheMountainChocobo);
    }

    @Test
    @DisplayName("An opponent's land does not boost the Bird")
    void opponentsLandDoesNotBoostBird() {
        castAndChooseMountain();
        Permanent bird = birdToken();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Forest()));

        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bird)).isEqualTo(2);
    }

    private Mountain castAndChooseMountain() {
        Mountain mountain = new Mountain();
        harness.setHand(player1, List.of(new CallTheMountainChocobo()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLibrary(player1, List.of(mountain, new Forest()));
        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(mountain);
        harness.handleCardChosen(player1, 0);
        return mountain;
    }

    private Permanent birdToken() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
    }
}
