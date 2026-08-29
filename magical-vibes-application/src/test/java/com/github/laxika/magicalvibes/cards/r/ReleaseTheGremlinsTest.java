package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReleaseTheGremlinsTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys X artifacts and creates X Gremlins")
    void destroysArtifactsAndCreatesGremlins() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent ornithopter = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player1, List.of(new ReleaseTheGremlins()));
        harness.addMana(player1, ManaColor.RED, 5); // X=2: {2}{2}{R}

        harness.castSorcery(player1, 0, 2, List.of(fountain.getId(), ornithopter.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Ornithopter");
        List<Permanent> gremlins = findPermanents(player1, "Gremlin");
        assertThat(gremlins).hasSize(2);
        assertThat(gremlins).allSatisfy(gremlin -> {
            assertThat(gremlin.getCard().getPower()).isEqualTo(2);
            assertThat(gremlin.getCard().getToughness()).isEqualTo(2);
            assertThat(gremlin.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(gremlin.getCard().getSubtypes()).containsExactly(CardSubtype.GREMLIN);
        });
    }

    @Test
    @DisplayName("X=0 destroys no artifacts and creates no Gremlins")
    void xZeroDoesNothing() {
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.setHand(player1, List.of(new ReleaseTheGremlins()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Rod of Ruin");
        assertThat(findPermanents(player1, "Gremlin")).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a non-artifact permanent")
    void cannotTargetNonArtifact() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ReleaseTheGremlins()));
        harness.addMana(player1, ManaColor.RED, 3); // X=1: {1}{1}{R}

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }
}
