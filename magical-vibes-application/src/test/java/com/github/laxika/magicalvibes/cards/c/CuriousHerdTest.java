package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BeastWithin;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CuriousHerd.class, SolRing.class, BeastWithin.class})
class CuriousHerdTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one 3/3 Beast for each artifact the targeted opponent controls")
    void createsBeastsForTargetOpponentsArtifacts() {
        harness.addToBattlefield(player2, new SolRing());
        harness.addToBattlefield(player2, new SolRing());
        harness.setHand(player1, List.of(new CuriousHerd()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(findPermanents(player1, "Beast")).hasSize(2);
        assertThat(findPermanents(player1, "Beast"))
                .allSatisfy(beast -> {
                    assertThat(beast.getEffectivePower()).isEqualTo(3);
                    assertThat(beast.getEffectiveToughness()).isEqualTo(3);
                });
    }

    @Test
    @DisplayName("Can target only an opponent")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new CuriousHerd()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Creates no tokens when the opponent has no artifacts, even if the caster does")
    void doesNotCountControllersArtifacts() {
        harness.addToBattlefield(player1, new SolRing());
        harness.setHand(player1, List.of(new CuriousHerd()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(findPermanents(player1, "Beast")).isEmpty();
        assertThat(findPermanents(player2, "Beast")).isEmpty();
        harness.assertInGraveyard(player1, "Curious Herd");
    }

    @Test
    @DisplayName("Counts artifacts entering after casting and gives the tokens only to the caster")
    void countsArtifactsAtResolution() {
        harness.addToBattlefield(player1, new SolRing());
        harness.setHand(player1, List.of(new CuriousHerd()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.addToBattlefield(player2, new SolRing());
        harness.addToBattlefield(player2, new SolRing());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Beast")).hasSize(2);
        assertThat(findPermanents(player2, "Beast")).isEmpty();
    }

    @Test
    @DisplayName("Does not count an artifact destroyed in response or its nonartifact replacement token")
    void doesNotCountArtifactsRemovedBeforeResolution() {
        harness.addToBattlefield(player2, new SolRing());
        harness.setHand(player1, List.of(new CuriousHerd(), new BeastWithin()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Sol Ring"));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Beast")).isEmpty();
        assertThat(findPermanents(player2, "Beast")).hasSize(1);
        harness.assertInGraveyard(player2, "Sol Ring");
        harness.assertInGraveyard(player1, "Curious Herd");
    }
}
