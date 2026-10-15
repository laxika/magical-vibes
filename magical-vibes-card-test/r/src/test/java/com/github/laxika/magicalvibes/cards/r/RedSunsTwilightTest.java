package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BlueSunsTwilight;
import com.github.laxika.magicalvibes.cards.m.MyrConvert;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RedSunsTwilight.class, MyrConvert.class, BlueSunsTwilight.class})
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

    @Test
    @DisplayName("X=0 permits no targets and leaves artifacts untouched")
    void zeroTargetsAtZero() {
        harness.addToBattlefield(player2, new MyrConvert());

        castAndResolve(0, List.of());

        harness.assertOnBattlefield(player2, "Myr Convert");
        harness.assertInGraveyard(player1, "Red Sun's Twilight");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Indestructible artifacts do not produce copies")
    void copiesOnlyArtifactsActuallyDestroyed() {
        Permanent protectedArtifact = harness.addToBattlefieldAndReturn(player2, new MyrConvert());
        protectedArtifact.getPersistentGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        Permanent ordinaryArtifact = harness.addToBattlefieldAndReturn(player2, new MyrConvert());

        castAndResolve(5, List.of(protectedArtifact.getId(), ordinaryArtifact.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(protectedArtifact);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Myr Convert");
    }

    @Test
    @DisplayName("Tokens remain until the next end-step exile trigger resolves")
    void exileUsesDelayedTrigger() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MyrConvert());
        castAndResolve(5, List.of(artifact.getId()));
        Permanent token = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
        assertThat(gd.stack).isNotEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
    }

    @Test
    @DisplayName("Copying a Twilight token does not copy its granted haste or delayed exile")
    void hasteIsNotCopiable() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MyrConvert());
        castAndResolve(5, List.of(artifact.getId()));
        Permanent originalToken = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(harness.getGameQueryService().hasKeyword(gd, originalToken, Keyword.HASTE)).isTrue();

        harness.setHand(player1, List.of(new BlueSunsTwilight()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castAndResolveSorcery(player1, 0, 5, originalToken.getId());

        Permanent copiedToken = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(originalToken.getId()))
                .findFirst().orElseThrow();
        assertThat(harness.getGameQueryService().hasKeyword(gd, copiedToken, Keyword.HASTE)).isFalse();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(copiedToken);
    }

    @Test
    @DisplayName("Artifacts that gain hexproof in response are not destroyed or copied")
    void skipsIllegalTargetWhileResolvingLegalTarget() {
        Permanent protectedArtifact = harness.addToBattlefieldAndReturn(player2, new MyrConvert());
        Permanent ordinaryArtifact = harness.addToBattlefieldAndReturn(player2, new MyrConvert());
        harness.setHand(player1, List.of(new RedSunsTwilight()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.castSorcery(player1, 0, 5, List.of(protectedArtifact.getId(), ordinaryArtifact.getId()));
        protectedArtifact.getPersistentGrantedKeywords().add(Keyword.HEXPROOF);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(protectedArtifact);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Myr Convert");
    }

    @Test
    @DisplayName("X limits the number of chosen artifacts")
    void rejectsMoreTargetsThanX() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new MyrConvert());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new MyrConvert());
        harness.setHand(player1, List.of(new RedSunsTwilight()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(first, second);
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
