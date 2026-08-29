package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RedSunsTwilightTest extends BaseCardTest {

    @Test
    @DisplayName("At X=5, destroys targeted artifacts and creates hasty token copies")
    void destroysArtifactsAndCreatesTokenCopiesAtFive() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, artifact("First Artifact"));
        Permanent second = harness.addToBattlefieldAndReturn(player2, artifact("Second Artifact"));

        castAndResolve(5, List.of(first.getId(), second.getId()));

        harness.assertInGraveyard(player2, "First Artifact");
        harness.assertInGraveyard(player2, "Second Artifact");
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2).allSatisfy(token -> {
            assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(token.getCard().getKeywords()).contains(Keyword.HASTE);
            assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                    .contains(new DelayedPermanentAction(token.getId(), DelayedPermanentActionKind.EXILE_TOKEN_AT_END_STEP));
        });
    }

    @Test
    @DisplayName("Below X=5, destroys artifacts without creating token copies")
    void doesNotCreateCopiesBelowFive() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, artifact("Artifact"));

        castAndResolve(4, List.of(artifact.getId()));

        harness.assertInGraveyard(player2, "Artifact");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Rejects a non-artifact target")
    void rejectsNonArtifactTarget() {
        Card creature = new Card();
        creature.setName("Creature");
        creature.setType(CardType.CREATURE);
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, creature);

        harness.setHand(player1, List.of(new RedSunsTwilight()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, permanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castAndResolve(int xValue, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new RedSunsTwilight()));
        harness.addMana(player1, ManaColor.RED, xValue + 2);
        harness.castSorcery(player1, 0, xValue, targetIds);
        harness.passBothPriorities();
    }

    private static Card artifact(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setManaCost("{2}");
        return card;
    }
}
