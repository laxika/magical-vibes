package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.ReboundAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PowerPack.class, Divination.class, GrizzlyBears.class})
class PowerPackTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage exiles a random instant or sorcery for the next upkeep")
    void combatDamageQueuesNextUpkeepFreeCast() {
        Divination divination = new Divination();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears, divination));

        Permanent powerPack = addAttackingPowerPack();
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bears);
        assertThat(gd.findExiledCard(divination.getId())).isNotNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);
        assertThat(powerPack.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The next upkeep offers the exiled spell and exiles it after resolution")
    void castsExiledSpellAtNextUpkeep() {
        Divination divination = new Divination();
        harness.setGraveyard(player1, List.of(divination));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        addAttackingPowerPack();
        resolveCombat();
        resolveAllTriggers();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        assertThat(gd.findExiledCard(divination.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(divination);
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    @DisplayName("Combat damage does nothing without an instant or sorcery in the graveyard")
    void combatDamageWithNoMatchingCardDoesNothing() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        addAttackingPowerPack();
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bears);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    @DisplayName("A vanished random target is not replaced by a newly available graveyard card")
    void doesNotChooseAnotherCardWhenOriginalTargetLeavesGraveyard() {
        Divination original = new Divination();
        Divination replacement = new Divination();
        harness.setGraveyard(player1, List.of(original));
        addAttackingPowerPack();
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, this::resolveCombat);
        assertThat(gd.stack).isNotEmpty();

        harness.setHand(player1, List.of(original));
        harness.setGraveyard(player1, List.of(replacement));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(replacement);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    @DisplayName("The exile replacement survives cleanup until the controller's next upkeep")
    void nextUpkeepCastIsExiledAfterActualTurnTransitions() {
        Divination divination = new Divination();
        harness.setGraveyard(player1, List.of(divination));
        addAttackingPowerPack();
        resolveCombat();
        resolveAllTriggers();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);
        resolveAllTriggers();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        assertThat(gd.findExiledCard(divination.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(divination);
    }

    @Test
    @DisplayName("Declining the next-upkeep cast leaves the card exiled with no further offer")
    void declinedCastDoesNotRepeatAtFollowingUpkeep() {
        Divination divination = new Divination();
        harness.setGraveyard(player1, List.of(divination));
        addAttackingPowerPack();
        resolveCombat();
        resolveAllTriggers();
        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.findExiledCard(divination.getId())).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    private Permanent addAttackingPowerPack() {
        Permanent powerPack = addCreatureReady(player1, new PowerPack());
        powerPack.setAttacking(true);
        return powerPack;
    }
}
