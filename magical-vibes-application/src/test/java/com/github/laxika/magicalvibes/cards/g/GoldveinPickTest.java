package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoldveinPick.class, GrizzlyBears.class, SerraAngel.class})
class GoldveinPickTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving equip ability attaches Goldvein Pick to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent pick = addPickReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(pick.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature gets +1/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent pick = addPickReady(player1);
        pick.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Goldvein Pick does not affect an unequipped creature")
    void doesNotAffectUnequippedCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addPickReady(player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creates a Treasure token when equipped creature deals combat damage to a player")
    void createsTreasureTokenOnCombatDamage() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent pick = addPickReady(player1);
        pick.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(treasuresFor(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Does not create a Treasure token when equipped creature deals no combat damage to a player")
    void doesNotCreateTreasureWhenBlocked() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent pick = addPickReady(player1);
        pick.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new SerraAngel());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        assertThat(treasuresFor(player1)).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentCreature() {
        addPickReady(player1);
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An unequipped attacker does not trigger Goldvein Pick")
    void unequippedAttackerDoesNotCreateTreasure() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipped = addCreatureReady(player1, new GrizzlyBears());
        Permanent pick = addPickReady(player1);
        pick.setAttachedTo(equipped.getId());
        attacker.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(treasuresFor(player1)).isEmpty();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Treasure belongs to the Pick controller even when the opponent controls the equipped creature")
    void equipmentControllerCreatesTreasure() {
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        Permanent pick = addPickReady(player1);
        pick.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(treasuresFor(player1)).hasSize(1);
        assertThat(treasuresFor(player2)).isEmpty();
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Two Picks each trigger once rather than once per point of damage")
    void eachEquipmentCreatesOneTreasure() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addPickReady(player1).setAttachedTo(attacker.getId());
        addPickReady(player1).setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(treasuresFor(player1)).hasSize(2);
        harness.assertLife(player2, 16);
        assertThat(treasuresFor(player1)).allSatisfy(token -> assertThat(token.isTapped()).isFalse());
    }

    @Test
    @DisplayName("A combat damage trigger still resolves after the Pick leaves the battlefield")
    void triggerSurvivesEquipmentLeavingBattlefield() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent pick = addPickReady(player1);
        pick.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        resolveCombat();
        assertThat(treasuresFor(player1)).isEmpty();
        gd.playerBattlefields.get(player1.getId()).remove(pick);
        gd.playerGraveyards.get(player1.getId()).add(pick.getCard());
        resolveAllTriggers();

        assertThat(treasuresFor(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Equip cannot be activated during combat")
    void equipRequiresSorceryTiming() {
        addPickReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Reequipping moves the boost from the old creature to the new creature")
    void reequippingMovesBoost() {
        Permanent pick = addPickReady(player1);
        Permanent original = addCreatureReady(player1, new GrizzlyBears());
        Permanent replacement = addCreatureReady(player1, new GrizzlyBears());
        pick.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, replacement.getId());
        harness.passBothPriorities();

        assertThat(pick.getAttachedTo()).isEqualTo(replacement.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, replacement)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, replacement)).isEqualTo(3);
    }

    private Permanent addPickReady(Player player) {
        return addCreatureReady(player, new GoldveinPick());
    }

    private List<Permanent> treasuresFor(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.TREASURE))
                .toList();
    }
}
