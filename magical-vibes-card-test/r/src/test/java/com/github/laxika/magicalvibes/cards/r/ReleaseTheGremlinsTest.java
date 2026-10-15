package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AegisAutomaton;
import com.github.laxika.magicalvibes.cards.a.AlleyStrangler;
import com.github.laxika.magicalvibes.cards.h.HeroicIntervention;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReleaseTheGremlins.class, AegisAutomaton.class, AlleyStrangler.class,
        Ornithopter.class, HeroicIntervention.class})
class ReleaseTheGremlinsTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys X artifacts and creates X Gremlins")
    void destroysArtifactsAndCreatesGremlins() {
        Permanent automaton = harness.addToBattlefieldAndReturn(player2, new AegisAutomaton());
        Permanent ornithopter = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player1, List.of(new ReleaseTheGremlins()));
        harness.addMana(player1, ManaColor.RED, 5); // X=2: {2}{2}{R}

        harness.castSorcery(player1, 0, 2, List.of(automaton.getId(), ornithopter.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Aegis Automaton");
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
        harness.addToBattlefield(player2, new Ornithopter());
        harness.setHand(player1, List.of(new ReleaseTheGremlins()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player2, "Ornithopter");
        assertThat(findPermanents(player1, "Gremlin")).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a non-artifact permanent")
    void cannotTargetNonArtifact() {
        Permanent strangler = harness.addToBattlefieldAndReturn(player2, new AlleyStrangler());
        harness.setHand(player1, List.of(new ReleaseTheGremlins()));
        harness.addMana(player1, ManaColor.RED, 3); // X=1: {1}{1}{R}

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(strangler.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }

    @Test
    void cannotChoosePositiveXWithoutTargets() {
        harness.setHand(player1, List.of(new ReleaseTheGremlins()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotChooseFewerTargetsThanX() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player1, List.of(new ReleaseTheGremlins()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotChooseMoreTargetsThanX() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new AegisAutomaton());
        harness.setHand(player1, List.of(new ReleaseTheGremlins()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotChooseTheSameArtifactTwice() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player1, List.of(new ReleaseTheGremlins()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, List.of(artifact.getId(), artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mustPayForBothXSymbols() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new AegisAutomaton());
        harness.setHand(player1, List.of(new ReleaseTheGremlins()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void createsFullXTokensWhenOneTargetLeavesBeforeResolution() {
        Permanent automaton = harness.addToBattlefieldAndReturn(player2, new AegisAutomaton());
        Permanent ornithopter = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player1, List.of(new ReleaseTheGremlins()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player2, ManaColor.WHITE, 5);

        harness.castSorcery(player1, 0, 2, List.of(automaton.getId(), ornithopter.getId()));
        harness.activateAbility(player2, 0, null, ornithopter.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Aegis Automaton");
        assertThat(findPermanents(player1, "Gremlin")).hasSize(2);
    }

    @Test
    void createsNoTokensWhenEveryTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player2, new AegisAutomaton());
        Permanent ornithopter = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player1, List.of(new ReleaseTheGremlins()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player2, ManaColor.WHITE, 5);

        harness.castSorcery(player1, 0, 1, List.of(ornithopter.getId()));
        harness.activateAbility(player2, 0, null, ornithopter.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Ornithopter");
        harness.assertInGraveyard(player1, "Release the Gremlins");
        assertThat(findPermanents(player1, "Gremlin")).isEmpty();
    }

    @Test
    void createsTokensEvenWhenOwnIndestructibleTargetSurvives() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.setHand(player1, List.of(new HeroicIntervention(), new ReleaseTheGremlins()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 1, artifact.getId());

        harness.assertOnBattlefield(player1, "Ornithopter");
        assertThat(findPermanents(player1, "Gremlin")).hasSize(1);
    }

    @Test
    void skipsAnIllegalTargetWhileDestroyingAnotherControllersArtifact() {
        Permanent protectedArtifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new AegisAutomaton());
        harness.setHand(player1, List.of(new ReleaseTheGremlins()));
        harness.setHand(player2, List.of(new HeroicIntervention()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 2, List.of(protectedArtifact.getId(), ownArtifact.getId()));
        harness.castAndResolveInstant(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player1, "Aegis Automaton");
        assertThat(findPermanents(player1, "Gremlin")).hasSize(2);
    }
}
