package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChromeCompanion.class, GrizzlyBears.class, Shock.class})
class ChromeCompanionTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life when Chrome Companion becomes tapped")
    void gainsLifeWhenBecomesTapped() {
        harness.setLife(player1, 20);
        Permanent companion = harness.addToBattlefieldAndReturn(player1, new ChromeCompanion());

        tap(companion);

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Puts a card from a graveyard on the bottom of its owner's library")
    void putsGraveyardCardOnLibraryBottom() {
        Card target = new GrizzlyBears();
        Card existingTop = new Shock();
        Card existingBottom = new Shock();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player2, List.of(existingTop, existingBottom));
        addReadyCompanion();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(existingTop, existingBottom, target);
    }

    @Test
    @DisplayName("Rejects a target that is not a card in a graveyard")
    void rejectsNonGraveyardTarget() {
        Card target = new GrizzlyBears();
        addReadyCompanion();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void activationGainsLifeBeforeMovingOwnGraveyardCard() {
        Card target = new Shock();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of());
        harness.setLife(player1, 20);
        Permanent companion = addReadyCompanion();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);

        assertThat(companion.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target);
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(target);
    }

    @Test
    void lifeTriggerStillResolvesWhenGraveyardTargetDisappears() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player2, List.of());
        harness.setLife(player1, 20);
        addReadyCompanion();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.setGraveyard(player2, List.of());
        harness.setExile(player2, List.of(target));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.findExiledCard(target.getId())).isNotNull();
    }

    @Test
    void eachCompanionOnlyTriggersForItselfAndItsController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new ChromeCompanion());
        Permanent opponentCompanion = harness.addToBattlefieldAndReturn(player2, new ChromeCompanion());

        tap(opponentCompanion);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(21);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggersAgainAfterUntappingInTheSameTurn() {
        harness.setLife(player1, 20);
        Permanent companion = harness.addToBattlefieldAndReturn(player1, new ChromeCompanion());
        tap(companion);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        companion.untap();
        tap(companion);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    void cannotActivateTapAbilityWhileSummoningSick() {
        Card target = new Shock();
        harness.setGraveyard(player1, List.of(target));
        Permanent companion = harness.addToBattlefieldAndReturn(player1, new ChromeCompanion());
        companion.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(companion.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target);
    }

    private Permanent addReadyCompanion() {
        Permanent companion = harness.addToBattlefieldAndReturn(player1, new ChromeCompanion());
        companion.setSummoningSick(false);
        return companion;
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
