package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.SoulsFire;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CharnelhoardWurm.class, CylianSunsinger.class, SoulsFire.class})
class CharnelhoardWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Dealing combat damage lets the controller return a chosen graveyard card to hand")
    void dealingDamageReturnsChosenCardToHand() {
        Card target = new CylianSunsinger();
        harness.setGraveyard(player1, List.of(target));
        harness.setLife(player2, 20);
        attackWithWurmDealingDamage();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gd.playerHands.get(player1.getId())).contains(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Declining the return at resolution leaves the targeted card in the graveyard")
    void decliningLeavesCardInGraveyard() {
        Card target = new CylianSunsinger();
        harness.setGraveyard(player1, List.of(target));
        harness.setLife(player2, 20);
        attackWithWurmDealingDamage();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target);
    }

    @Test
    @DisplayName("With an empty graveyard the trigger prompts nothing and damage is still dealt")
    void emptyGraveyardDealsDamageWithoutPrompt() {
        harness.setGraveyard(player1, List.of());
        harness.setLife(player2, 20);
        attackWithWurmDealingDamage();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("A legal graveyard target is mandatory even when the controller intends to decline")
    void targetChoiceRequiresOneCardFromOwnGraveyard() {
        Card own = new CylianSunsinger();
        Card opposing = new CylianSunsinger();
        harness.setGraveyard(player1, List.of(own));
        harness.setGraveyard(player2, List.of(opposing));

        attackWithWurmDealingDamage();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(own.getId());
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("A targeted card that leaves the graveyard is not returned or replaced by another card")
    void targetLeavingGraveyardMakesTriggerDoNothing() {
        Card target = new CylianSunsinger();
        Card other = new CharnelhoardWurm();
        harness.setGraveyard(player1, List.of(target, other));
        attackWithWurmDealingDamage();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));

        harness.setGraveyard(player1, List.of(other));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(target, other);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
    }

    @Test
    @DisplayName("Trample damage to an opponent triggers one return after damaging a blocker")
    void trampleDamageTriggersReturn() {
        Card target = new CylianSunsinger();
        harness.setGraveyard(player1, List.of(target));
        addCreatureReady(player1, new CharnelhoardWurm());
        Permanent blocker = addCreatureReady(player2, new CylianSunsinger());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 2, player2.getId(), 4));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(target);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Noncombat damage to an opponent targets and may return a noncreature card")
    void noncombatDamageCanReturnNoncreatureCard() {
        Card target = new SoulsFire();
        harness.setGraveyard(player1, List.of(target));
        dealWurmPowerDamageTo(player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Damage to the Wurm's own controller does not trigger the return ability")
    void damageToControllerDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new CylianSunsinger()));
        dealWurmPowerDamageTo(player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void attackWithWurmDealingDamage() {
        Permanent wurm = addCreatureReady(player1, new CharnelhoardWurm());
        wurm.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }

    private void dealWurmPowerDamageTo(UUID playerId) {
        Permanent wurm = addCreatureReady(player1, new CharnelhoardWurm());
        harness.setHand(player1, List.of(new SoulsFire()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, List.of(wurm.getId(), playerId));
        harness.passBothPriorities();
    }
}
