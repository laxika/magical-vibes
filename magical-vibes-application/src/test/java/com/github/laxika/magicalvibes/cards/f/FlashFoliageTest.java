package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
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

@CardUsed({FlashFoliage.class, MistralCharger.class})
class FlashFoliageTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Saproling blocking the targeted creature and draws a card")
    void createsBlockingSaprolingAndDrawsCard() {
        Permanent attacker = addCreatureReady(player1, new MistralCharger());
        addCreatureReady(player2, new MistralCharger());
        declareAttackers(List.of(0));
        harness.setLibrary(player2, List.of(new MistralCharger()));

        castFlashFoliage(attacker);

        Permanent token = findPermanents(player2, "Saproling").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.isBlocking()).isTrue();
        assertThat(token.getBlockingTargetIds()).containsExactly(attacker.getId());
        assertThat(gqs.isBlockedByAnyCreature(gd, attacker)).isTrue();
        harness.assertInHand(player2, "Mistral Charger");
    }

    @Test
    @DisplayName("Cannot be cast before blockers are declared")
    void cannotCastBeforeBlockersAreDeclared() {
        Permanent attacker = addCreatureReady(player1, new MistralCharger());
        declareAttackers(List.of(0));
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        giveSpell();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot target a creature attacking another player")
    void cannotTargetCreatureAttackingAnotherPlayer() {
        Permanent attacker = addCreatureReady(player1, new MistralCharger());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        giveSpell();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature attacking you");
    }

    private void castFlashFoliage(Permanent target) {
        giveSpell();
        harness.castAndResolveInstant(player2, 0, target.getId());
    }

    @Test
    void saprolingTradesWithFlyingAttackerAndPreventsPlayerDamage() {
        Permanent attacker = prepareUnblockedAttack();

        harness.castAndResolveInstant(player2, 0, attacker.getId());
        harness.resolveCombatDamage();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Mistral Charger");
        assertThat(findPermanents(player2, "Saproling")).isEmpty();
        harness.assertInHand(player2, "Mistral Charger");
    }

    @Test
    void illegalTargetOnResolutionPreventsBothTokenAndDraw() {
        Permanent attacker = prepareUnblockedAttack();
        harness.castInstant(player2, 0, attacker.getId());
        attacker.setAttacking(false);

        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Saproling")).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Flash Foliage");
    }

    @Test
    void canCreateBlockerAfterCombatDamageWithoutUndoingPlayerDamage() {
        Permanent attacker = prepareUnblockedAttack();
        harness.resolveCombatDamage();
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();

        castFlashFoliage(attacker);

        Permanent token = findPermanents(player2, "Saproling").getFirst();
        assertThat(token.isBlocking()).isTrue();
        assertThat(token.getBlockingTargetIds()).containsExactly(attacker.getId());
        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player1, "Mistral Charger");
        harness.assertInHand(player2, "Mistral Charger");
    }

    @Test
    void canAddSaprolingToAlreadyBlockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new MistralCharger());
        Permanent blocker = addCreatureReady(player2, new MistralCharger());
        giveSpell();
        harness.setLibrary(player2, List.of(new MistralCharger()));
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.castAndResolveInstant(player2, 0, attacker.getId());

        Permanent token = findPermanents(player2, "Saproling").getFirst();
        assertThat(token.getBlockingTargetIds()).containsExactly(attacker.getId());
        assertThat(blocker.getBlockingTargetIds()).containsExactly(attacker.getId());
        harness.assertInHand(player2, "Mistral Charger");
    }

    private Permanent prepareUnblockedAttack() {
        Permanent attacker = addCreatureReady(player1, new MistralCharger());
        giveSpell();
        harness.setLibrary(player2, List.of(new MistralCharger()));
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        return attacker;
    }

    private void giveSpell() {
        harness.setHand(player2, List.of(new FlashFoliage()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
    }
}
