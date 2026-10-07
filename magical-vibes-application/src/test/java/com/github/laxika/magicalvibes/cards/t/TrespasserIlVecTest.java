package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrespasserIlVec.class, AshcoatBear.class, Island.class})
class TrespasserIlVecTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Trespasser il-Vec requires discarding a card")
    void activationRequiresDiscardingACard() {
        addCreatureReady(player1, new TrespasserIlVec());
        harness.setHand(player1, List.of(new AshcoatBear()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
    }

    @Test
    @DisplayName("Discarding a card gives Trespasser il-Vec shadow until end of turn")
    void resolvingAbilityGrantsShadow() {
        Permanent trespasser = addCreatureReady(player1, new TrespasserIlVec());
        harness.setHand(player1, List.of(new Island()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, trespasser, Keyword.SHADOW)).isTrue();
        harness.assertInGraveyard(player1, "Island");
    }

    @Test
    @DisplayName("Shadow wears off at end of turn")
    void shadowWearsOffAtEndOfTurn() {
        Permanent trespasser = addCreatureReady(player1, new TrespasserIlVec());
        harness.setHand(player1, List.of(new AshcoatBear()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.hasKeyword(gd, trespasser, Keyword.SHADOW)).isFalse();
    }

    @Test
    @DisplayName("The ability can be activated more than once in the same turn")
    void canActivateMoreThanOnceInSameTurn() {
        Permanent trespasser = addCreatureReady(player1, new TrespasserIlVec());
        harness.setHand(player1, List.of(new AshcoatBear(), new AshcoatBear()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gqs.hasKeyword(gd, trespasser, Keyword.SHADOW)).isTrue();
    }

    @Test
    @DisplayName("Only Trespasser il-Vec gains the granted shadow")
    void onlySourceGainsShadow() {
        Permanent trespasser = addCreatureReady(player1, new TrespasserIlVec());
        Permanent otherCreature = addCreatureReady(player1, new AshcoatBear());
        harness.setHand(player1, List.of(new AshcoatBear()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, trespasser, Keyword.SHADOW)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.SHADOW)).isFalse();
    }

    @Test
    @DisplayName("A creature without shadow cannot block Trespasser il-Vec after it gains shadow")
    void creatureWithoutShadowCannotBlockAfterShadowIsGranted() {
        addCreatureReady(player1, new TrespasserIlVec());
        addCreatureReady(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new AshcoatBear()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shadow");
    }

    @Test
    @DisplayName("The ability cannot be activated without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addCreatureReady(player1, new TrespasserIlVec());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard a card");
    }

    @Test
    @DisplayName("Discard is paid before the ability resolves")
    void discardIsPaidBeforeResolution() {
        Permanent trespasser = addCreatureReady(player1, new TrespasserIlVec());
        harness.setHand(player1, List.of(new Island()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Island");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gqs.hasKeyword(gd, trespasser, Keyword.SHADOW)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, trespasser, Keyword.SHADOW)).isTrue();
    }

    @Test
    @DisplayName("A tapped creature with summoning sickness can activate the ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent trespasser = addCreatureReady(player1, new TrespasserIlVec());
        trespasser.setSummoningSick(true);
        trespasser.tap();
        harness.setHand(player1, List.of(new AshcoatBear()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, trespasser, Keyword.SHADOW)).isTrue();
        harness.assertInGraveyard(player1, "Ashcoat Bear");
    }

    @Test
    @DisplayName("Trespasser with shadow cannot block a creature without shadow")
    void cannotBlockCreatureWithoutShadow() {
        addCreatureReady(player1, new TrespasserIlVec());
        addCreatureReady(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new Island()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shadow");
    }

    @Test
    @DisplayName("Trespasser with shadow can block another creature with shadow")
    void canBlockCreatureWithShadow() {
        addCreatureReady(player1, new TrespasserIlVec());
        addCreatureReady(player2, new TrespasserIlVec());
        harness.setHand(player1, List.of(new Island()));
        harness.setHand(player2, List.of(new Island()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.activateAbility(player2, 0, null, null);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

}
