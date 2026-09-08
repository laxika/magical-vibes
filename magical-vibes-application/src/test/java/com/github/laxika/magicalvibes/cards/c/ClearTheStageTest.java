package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClearTheStageTest extends BaseCardTest {

    @Test
    @DisplayName("gives a creature -3/-3 and returns a creature card when ferocious is met")
    void shrinksAndReturnsCreatureCard() {
        Permanent largeCreature = harness.addToBattlefieldAndReturn(player1, new AlpineGrizzly());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Card graveyardCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        cast(target, largeCreature, graveyardCreature);

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        GameData gameData = harness.getGameData();
        assertThat(gameData.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(graveyardCreature.getId()));
        assertThat(gameData.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(graveyardCreature.getId()));
    }

    @Test
    @DisplayName("does not return a creature card without a creature with power 4 or greater")
    void doesNotReturnCreatureCardWithoutFerocious() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Card graveyardCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        cast(target, null, graveyardCreature);

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(graveyardCreature.getId()));
    }

    @Test
    @DisplayName("the -3/-3 effect wears off at end of turn")
    void shrinkWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        cast(target, null, null);
        assertThat(target.getEffectivePower()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("cannot target a noncreature permanent")
    void targetMustBeCreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("cannot target a noncreature card in the graveyard")
    void graveyardTargetMustBeCreatureCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId(), target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(Permanent target, Permanent largeCreature, Card graveyardCard) {
        prepareCast();
        if (largeCreature != null) {
            // Keep the ferocious creature separate from the creature receiving -3/-3.
            assertThat(largeCreature.getEffectivePower()).isGreaterThanOrEqualTo(4);
        }
        if (graveyardCard == null) {
            harness.castInstant(player1, 0, List.of(target.getId()));
        } else {
            harness.castInstant(player1, 0, graveyardCard.getId(), target.getId());
        }
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new ClearTheStage()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
