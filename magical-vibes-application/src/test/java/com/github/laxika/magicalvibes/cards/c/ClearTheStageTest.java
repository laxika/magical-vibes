package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.r.RampagingRendhorn;
import com.github.laxika.magicalvibes.cards.s.SimicGuildgate;
import com.github.laxika.magicalvibes.cards.t.TerritorialBoar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ClearTheStage.class, RampagingRendhorn.class, TerritorialBoar.class, SimicGuildgate.class, ConsulateDreadnought.class})
class ClearTheStageTest extends BaseCardTest {

    @Test
    @DisplayName("gives a creature -3/-3 and returns a creature card when ferocious is met")
    void shrinksAndReturnsCreatureCard() {
        Permanent largeCreature = harness.addToBattlefieldAndReturn(player1, new RampagingRendhorn());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RampagingRendhorn());
        Card graveyardCreature = new TerritorialBoar();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        cast(target, largeCreature, graveyardCreature);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, true);

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
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RampagingRendhorn());
        Card graveyardCreature = new TerritorialBoar();
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
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RampagingRendhorn());
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
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new SimicGuildgate());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("cannot target a noncreature card in the graveyard")
    void graveyardTargetMustBeCreatureCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RampagingRendhorn());
        Card forest = new SimicGuildgate();
        harness.setGraveyard(player1, List.of(forest));
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(target.getId(), forest.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void checksPowerAfterShrinkingTheOnlyLargeCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RampagingRendhorn());
        Card graveyardCreature = new TerritorialBoar();
        harness.setGraveyard(player1, List.of(graveyardCreature));

        cast(target, target, graveyardCreature);

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCreature);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(graveyardCreature);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canDeclineReturningTheChosenGraveyardTarget() {
        Permanent largeCreature = harness.addToBattlefieldAndReturn(player1, new RampagingRendhorn());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RampagingRendhorn());
        Card graveyardCreature = new TerritorialBoar();
        harness.setGraveyard(player1, List.of(graveyardCreature));

        cast(target, largeCreature, graveyardCreature);

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCreature);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(graveyardCreature);
    }

    @Test
    @CardUsed({ConsulateDreadnought.class})
    void uncrewedVehicleDoesNotSatisfyTheCreatureCondition() {
        harness.addToBattlefield(player1, new ConsulateDreadnought());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RampagingRendhorn());
        Card graveyardCreature = new TerritorialBoar();
        harness.setGraveyard(player1, List.of(graveyardCreature));

        cast(target, null, graveyardCreature);

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCreature);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(graveyardCreature);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canOmitGraveyardTargetEvenWhenTheConditionIsMet() {
        Permanent largeCreature = harness.addToBattlefieldAndReturn(player1, new RampagingRendhorn());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RampagingRendhorn());
        Card graveyardCreature = new TerritorialBoar();
        harness.setGraveyard(player1, List.of(graveyardCreature));

        cast(target, largeCreature, null);

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCreature);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void cannotCastWithoutACreatureTarget() {
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetACreatureCardInAnOpponentsGraveyard() {
        harness.addToBattlefield(player1, new RampagingRendhorn());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RampagingRendhorn());
        Card graveyardCreature = new TerritorialBoar();
        harness.setGraveyard(player2, List.of(graveyardCreature));
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(target.getId(), graveyardCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void stillShrinksWhenTheGraveyardTargetBecomesIllegal() {
        harness.addToBattlefield(player1, new RampagingRendhorn());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RampagingRendhorn());
        Card graveyardCreature = new TerritorialBoar();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        prepareCast();
        harness.castInstant(player1, 0, List.of(target.getId(), graveyardCreature.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(graveyardCreature));

        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(graveyardCreature);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void stillReturnsWhenTheCreatureTargetBecomesIllegal() {
        harness.addToBattlefield(player1, new RampagingRendhorn());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RampagingRendhorn());
        Card graveyardCreature = new TerritorialBoar();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        prepareCast();
        harness.castInstant(player1, 0, List.of(target.getId(), graveyardCreature.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setHand(player2, List.of(target.getCard()));

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).contains(graveyardCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(graveyardCreature);
    }

    @Test
    void checksTheConditionAtResolutionRatherThanCasting() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RampagingRendhorn());
        Card graveyardCreature = new TerritorialBoar();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        prepareCast();
        harness.castInstant(player1, 0, List.of(target.getId(), graveyardCreature.getId()));
        harness.addToBattlefield(player1, new RampagingRendhorn());

        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).contains(graveyardCreature);
    }

    @Test
    void doesNotReturnWhenTheLargeCreatureLeavesBeforeResolution() {
        Permanent largeCreature = harness.addToBattlefieldAndReturn(player1, new RampagingRendhorn());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RampagingRendhorn());
        Card graveyardCreature = new TerritorialBoar();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        prepareCast();
        harness.castInstant(player1, 0, List.of(target.getId(), graveyardCreature.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(largeCreature);
        harness.setHand(player1, List.of(largeCreature.getCard()));

        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCreature);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(graveyardCreature);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void aCreatureWithZeroToughnessDiesAfterTheSpellResolves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TerritorialBoar());

        cast(target, null, null);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
    }

    private void cast(Permanent target, Permanent largeCreature, Card graveyardCard) {
        prepareCast();
        if (largeCreature != null) {
            assertThat(largeCreature.getEffectivePower()).isGreaterThanOrEqualTo(4);
        }
        if (graveyardCard == null) {
            harness.castAndResolveInstant(player1, 0, List.of(target.getId()));
        } else {
            harness.castInstant(player1, 0, List.of(target.getId(), graveyardCard.getId()));
            harness.passBothPriorities();
        }
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new ClearTheStage()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
