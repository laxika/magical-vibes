package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AncestralAnger.class, GrizzlyBears.class, FountainOfYouth.class})
class AncestralAngerTest extends BaseCardTest {

    @Test
    @DisplayName("With no Ancestral Anger in graveyard, grants +1/+0 and trample")
    void grantsBaseBoostWithEmptyGraveyard() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AncestralAnger()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, bearId);

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(bear.getGrantedKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Boost scales with Ancestral Anger cards in graveyard")
    void boostScalesWithNamedCardsInGraveyard() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AncestralAnger()));
        harness.setGraveyard(player1, List.of(new AncestralAnger(), new AncestralAnger()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, bearId);

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isEqualTo(3);
        assertThat(bear.getGrantedKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Resolving draws a card")
    void resolvingDrawsACard() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AncestralAnger()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, bearId);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Boost and trample wear off at cleanup step")
    void boostWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AncestralAnger()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, bearId);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new AncestralAnger()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Targeting an opponent's creature still counts only the caster's graveyard and draws for the caster")
    void opposingTargetUsesCastersGraveyardAndDrawsForCaster() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AncestralAnger()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new FountainOfYouth()));
        harness.setGraveyard(player1, List.of(new AncestralAnger(), new FountainOfYouth()));
        harness.setGraveyard(player2, List.of(new AncestralAnger(), new AncestralAnger()));
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent bear = findPermanent(player2, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, bear.getId());

        assertThat(bear.getPowerModifier()).isEqualTo(2);
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(bear.getGrantedKeywords()).contains(Keyword.TRAMPLE);
        harness.assertInHand(player1, "Fountain of Youth");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Graveyard count is determined at resolution and the boost stays fixed afterward")
    void graveyardCountIsDeterminedAtResolution() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AncestralAnger()));
        harness.addMana(player1, ManaColor.RED, 1);
        Permanent bear = findPermanent(player1, "Grizzly Bears");

        harness.castSorcery(player1, 0, bear.getId());
        harness.setGraveyard(player1, List.of(new AncestralAnger(), new AncestralAnger()));
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(3);
        harness.setGraveyard(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(5);
    }

    @Test
    @DisplayName("An illegal sole target prevents drawing a card")
    void removedTargetPreventsDraw() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AncestralAnger()));
        harness.setLibrary(player1, List.of(new FountainOfYouth()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.castSorcery(player1, 0, bearId);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Ancestral Anger");
        assertThat(gd.stack).isEmpty();
    }
}
