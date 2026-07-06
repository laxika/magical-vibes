package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AncestralAngerTest extends BaseCardTest {

    

    @Test
    @DisplayName("With no Ancestral Anger in graveyard, grants +1/+0 and trample")
    void grantsBaseBoostWithEmptyGraveyard() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AncestralAnger()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castSorcery(player1, 0, bearId);
        harness.passBothPriorities();

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
        harness.castSorcery(player1, 0, bearId);
        harness.passBothPriorities();

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
        harness.castSorcery(player1, 0, bearId);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Boost and trample wear off at cleanup step")
    void boostWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AncestralAnger()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castSorcery(player1, 0, bearId);
        harness.passBothPriorities();

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
}
