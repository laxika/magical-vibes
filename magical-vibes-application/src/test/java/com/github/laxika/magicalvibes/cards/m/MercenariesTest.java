package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Mercenaries.class, BalduvianBears.class})
class MercenariesTest extends BaseCardTest {

    @Test
    @DisplayName("Activating records a one-shot shield for the activator against this creature")
    void activationRecordsShield() {
        Permanent mercs = addReadyMercenaries(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.playerId().equals(player1.getId()) && s.sourceId().equals(mercs.getId()));
    }

    @Test
    @DisplayName("Prevents the next combat damage from Mercenaries to the activator and consumes the shield")
    void preventsNextCombatDamageAndConsumesShield() {
        harness.setLife(player2, 20);
        Permanent mercs = addReadyMercenaries(player1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        // Opponent pays {3} to prevent the next damage Mercenaries would deal to them.
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        mercs.setAttacking(true);
        resolveCombat(player1);

        harness.assertLife(player2, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("A second hit from Mercenaries after the shield is consumed deals damage")
    void secondHitDealsDamage() {
        harness.setLife(player2, 20);
        Permanent mercs = addReadyMercenaries(player1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        mercs.setAttacking(true);
        resolveCombat(player1);
        harness.assertLife(player2, 20);

        // Advance past cleanup so combat flags reset, then swing again without a new shield.
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        mercs.setSummoningSick(false);
        mercs.setAttacking(true);
        resolveCombat(player1);

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Shield only protects the activator; another player still takes damage")
    void shieldOnlyProtectsActivator() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent mercs = addMercenariesWithDamageAbility(player1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 1, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.playerId().equals(player2.getId()) && s.sourceId().equals(mercs.getId()));
    }

    @Test
    @DisplayName("Damage from a different creature is not prevented by the Mercenaries shield")
    void otherCreatureStillDealsDamage() {
        harness.setLife(player2, 20);
        Permanent mercs = addReadyMercenaries(player1);
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        bears.setAttacking(true);
        resolveCombat(player1);

        harness.assertLife(player2, 18);
        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.sourceId().equals(mercs.getId()));
    }

    @Test
    @DisplayName("Prevents noncombat damage from Mercenaries to the protected player")
    void preventsNextNoncombatDamage() {
        harness.setLife(player2, 20);
        addMercenariesWithDamageAbility(player1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Damage from Mercenaries to a creature is not prevented")
    void damageToCreatureIsNotPrevented() {
        harness.setLife(player2, 20);
        Permanent mercs = addMercenariesWithDamageAbility(player1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        Permanent bears = addCreatureReady(player2, new BalduvianBears());
        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player2, 20);
        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.playerId().equals(player2.getId()) && s.sourceId().equals(mercs.getId()));
    }

    @Test
    @DisplayName("Opponent pays the mana cost from their own pool")
    void opponentPaysManaFromOwnPool() {
        addReadyMercenaries(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.playerSourceNextDamageShields).isNotEmpty();
    }

    @Test
    @DisplayName("Shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        addReadyMercenaries(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.playerSourceNextDamageShields).isNotEmpty();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    private Permanent addMercenariesWithDamageAbility(Player player) {
        Card card = new Mercenaries().createRuntimeCopy();
        card.addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new DealDamageToAnyTargetEffect(1)),
                "Mercenaries deals 1 damage to any target."
        ));
        return addCreatureReady(player, card);
    }

    private Permanent addReadyMercenaries(Player player) {
        return addCreatureReady(player, new Mercenaries());
    }
}
