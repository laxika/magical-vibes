package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SultaiEmissaryTest extends BaseCardTest {

    @Test
    @DisplayName("When Sultai Emissary dies, its controller manifests the top card of their library")
    void diesManifestsTopCard() {
        Permanent emissary = addCreatureReady(player1, new SultaiEmissary());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, emissary.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested() && permanent.isFaceDown());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("When Sultai Emissary dies with an empty library, nothing is manifested")
    void diesWithEmptyLibraryDoesNothing() {
        Permanent emissary = addCreatureReady(player1, new SultaiEmissary());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, emissary.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(Permanent::isManifested);
    }
}
