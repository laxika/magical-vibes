package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WormHarvestTest extends BaseCardTest {

    private List<Permanent> wormTokens(UUID playerId) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Worm"))
                .toList();
    }

    @Test
    @DisplayName("Creates a 1/1 Worm token for each land card in your graveyard")
    void createsOneWormPerLandInGraveyard() {
        harness.setHand(player1, List.of(new WormHarvest()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> worms = wormTokens(player1.getId());
        assertThat(worms).hasSize(3);
        for (Permanent worm : worms) {
            assertThat(worm.getCard().getPower()).isEqualTo(1);
            assertThat(worm.getCard().getToughness()).isEqualTo(1);
            assertThat(worm.getCard().getSubtypes()).contains(CardSubtype.WORM);
        }
    }

    @Test
    @DisplayName("No tokens created when graveyard has no land cards")
    void noTokensWithNoLands() {
        harness.setHand(player1, List.of(new WormHarvest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(wormTokens(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only land cards in the graveyard are counted")
    void nonLandCardsNotCounted() {
        harness.setHand(player1, List.of(new WormHarvest()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new GrizzlyBears()));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(wormTokens(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Retrace recasts from the graveyard by discarding a land, which then also counts")
    void retraceFromGraveyard() {
        harness.setGraveyard(player1, new ArrayList<>(List.of(new WormHarvest(), new Forest(), new Forest())));
        harness.setHand(player1, new ArrayList<>(List.of(new Forest())));
        harness.addMana(player1, ManaColor.BLACK, 5);

        // Recast Worm Harvest (graveyard index 0), discarding the land in hand (index 0).
        harness.castRetrace(player1, 0, 0);
        harness.passBothPriorities();

        // Two lands already in the graveyard plus the discarded land = three Worm tokens.
        assertThat(wormTokens(player1.getId())).hasSize(3);
        // Worm Harvest returns to the graveyard, so it can be retraced again.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Worm Harvest"));
    }
}
