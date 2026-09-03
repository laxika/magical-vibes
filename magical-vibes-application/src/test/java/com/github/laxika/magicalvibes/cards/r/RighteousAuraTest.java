package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.Archangel;
import com.github.laxika.magicalvibes.cards.f.Fireblast;
import com.github.laxika.magicalvibes.cards.k.KingCheetah;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RighteousAura.class, KingCheetah.class, Archangel.class, Fireblast.class})
class RighteousAuraTest extends BaseCardTest {

    @Test
    @DisplayName("Activation pays 2 life and prompts for a source choice")
    void activationPaysLifeAndPromptsForSource() {
        harness.setLife(player1, 20);
        addReadyAura(player1);
        addReadyKingCheetah(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Prevents the next damage from the chosen source")
    void preventsDamageFromChosenSource() {
        harness.setLife(player1, 20);
        addReadyAura(player1);
        Permanent kingCheetah = addReadyKingCheetah(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, kingCheetah.getId());

        kingCheetah.setAttacking(true);
        resolveCombat(player2);

        // 2 life paid on activation; combat damage prevented → still 18
        harness.assertLife(player1, 18);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("A different source still deals damage; the shield is untouched")
    void differentSourceStillDealsDamage() {
        harness.setLife(player1, 20);
        addReadyAura(player1);
        Permanent chosen = addReadyKingCheetah(player2);
        Permanent other = addReadyKingCheetah(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        other.setAttacking(true);
        resolveCombat(player2);

        // 2 life paid + 3 combat damage
        harness.assertLife(player1, 15);
        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.sourceId().equals(chosen.getId()));
    }

    @Test
    @DisplayName("Damage from the chosen source to a creature you control is not prevented")
    void chosenSourceDamageToControlledCreatureIsNotPrevented() {
        harness.setLife(player1, 20);
        addReadyAura(player1);
        Permanent blocker = addCreatureReady(player1, new Archangel());
        Permanent kingCheetah = addReadyKingCheetah(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, kingCheetah.getId());

        kingCheetah.setAttacking(true);
        prepareDeclareBlockers(player2);
        int blockerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(kingCheetah);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(blocker.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.sourceId().equals(kingCheetah.getId()));
    }

    @Test
    @DisplayName("A spell on the stack is a legal source choice")
    void spellOnStackIsLegalSourceChoice() {
        harness.setLife(player1, 20);
        addReadyAura(player1);
        Fireblast fireblast = new Fireblast();
        harness.setHand(player2, List.of(fireblast));
        harness.addMana(player2, ManaColor.RED, 6);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(fireblast.getId());
        harness.handlePermanentChosen(player1, fireblast.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        addReadyAura(player1);
        Permanent kingCheetah = addReadyKingCheetah(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, kingCheetah.getId());

        assertThat(gd.playerSourceNextDamageShields).isNotEmpty();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    private Permanent addReadyAura(Player player) {
        return addCreatureReady(player, new RighteousAura());
    }

    private Permanent addReadyKingCheetah(Player player) {
        return addCreatureReady(player, new KingCheetah());
    }
}
