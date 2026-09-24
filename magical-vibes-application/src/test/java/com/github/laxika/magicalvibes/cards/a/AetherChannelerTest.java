package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AetherChanneler.class, Forest.class, GrizzlyBears.class, Island.class})
class AetherChannelerTest extends BaseCardTest {

    @Test
    @DisplayName("The token mode creates a 1/1 white Bird with flying")
    void createsBirdToken() {
        cast(0);
        resolveCreatureAndEtb();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Bird"))
                .singleElement()
                .satisfies(bird -> {
                    assertThat(bird.getCard().getPower()).isEqualTo(1);
                    assertThat(bird.getCard().getToughness()).isEqualTo(1);
                    assertThat(gqs.hasKeyword(gd, bird, Keyword.FLYING)).isTrue();
                });
    }

    @Test
    @DisplayName("The draw mode draws a card")
    void drawsCard() {
        harness.setLibrary(player1, List.of(new Forest()));
        cast(2);
        resolveCreatureAndEtb();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof Forest);
    }

    @Test
    @DisplayName("The bounce mode returns another nonland permanent to its owner's hand")
    void returnsAnotherNonlandPermanent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(1, bears.getId());
        resolveCreatureAndEtb();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The bounce mode rejects a land target")
    void rejectsLandTarget() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> cast(1, island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int mode, UUID... targetIds) {
        harness.setHand(player1, List.of(new AetherChanneler()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0, mode,
                targetIds.length == 0 ? null : targetIds[0]);
    }

    private void resolveCreatureAndEtb() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
