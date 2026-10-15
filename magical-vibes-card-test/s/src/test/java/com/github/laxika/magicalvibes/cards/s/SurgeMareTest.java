package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.o.OnakkeOgre;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.cards.h.HeavyArbalest;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SurgeMare.class, GreenwoodSentinel.class, OnakkeOgre.class, HeavyArbalest.class})
class SurgeMareTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability gives +2/-2 until end of turn")
    void abilityBoosts() {
        Permanent mare = addMareReady();
        addBlueMana(1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(mare.getEffectivePower()).isEqualTo(2);
        assertThat(mare.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent mare = addMareReady();
        addBlueMana(1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mare.getEffectivePower()).isEqualTo(0);
        assertThat(mare.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Repeated activations stack and can kill Surge Mare via state-based actions")
    void repeatedActivationsKillIt() {
        addMareReady();
        addBlueMana(3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        harness.assertNotOnBattlefield(player1, "Surge Mare");
        harness.assertInGraveyard(player1, "Surge Mare");
    }

    @Test
    @DisplayName("Surge Mare can't be blocked by a green creature")
    void cannotBeBlockedByGreenCreature() {
        Permanent blocker = addCreatureReady(player2, new GreenwoodSentinel());
        Permanent mare = addMareReady();
        mare.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(mare);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Surge Mare can be blocked by a non-green creature")
    void canBeBlockedByNonGreenCreature() {
        Permanent blocker = addCreatureReady(player2, new OnakkeOgre());
        Permanent mare = addMareReady();
        mare.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(mare);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Accepting the damage trigger draws then discards a card")
    void damageTriggerLoots() {
        Permanent mare = addMareReady();
        mare.setPowerModifier(2);
        mare.setToughnessModifier(-2);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Declining the damage trigger neither draws nor discards")
    void damageTriggerDeclined() {
        Permanent mare = addMareReady();
        mare.setPowerModifier(2);
        mare.setToughnessModifier(-2);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("No trigger when Surge Mare deals no damage to a player")
    void noTriggerWithoutDamage() {
        addMareReady();

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Noncombat damage to its controller does not trigger looting")
    void damageToControllerDoesNotTrigger() {
        Permanent mare = addMareReady();
        Permanent arbalest = addCreatureReady(player1, new HeavyArbalest());
        arbalest.setAttachedTo(mare.getId());

        harness.activateAbility(player1, 0, 1, null, player1.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Noncombat damage to an opponent triggers looting")
    void noncombatDamageToOpponentTriggers() {
        Permanent mare = addMareReady();
        Permanent arbalest = addCreatureReady(player1, new HeavyArbalest());
        arbalest.setAttachedTo(mare.getId());

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("Looting with an empty hand discards the newly drawn card")
    void emptyHandDiscardsDrawnCard() {
        harness.setHand(player1, List.of());
        SurgeMare drawnCard = new SurgeMare();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(drawnCard);
        Permanent mare = addMareReady();
        mare.setPowerModifier(2);
        mare.setToughnessModifier(-2);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.DiscardChoice) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void addBlueMana(int count) {
        harness.addMana(player1, ManaColor.BLUE, count);
        harness.addMana(player1, ManaColor.COLORLESS, count);
    }

    private Permanent addMareReady() {
        return addCreatureReady(player1, new SurgeMare());
    }
}
