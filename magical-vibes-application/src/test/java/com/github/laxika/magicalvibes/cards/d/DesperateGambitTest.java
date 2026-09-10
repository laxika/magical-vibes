package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FireWhip;
import com.github.laxika.magicalvibes.cards.h.HeavyBallista;
import com.github.laxika.magicalvibes.cards.t.Thunderbolt;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DesperateGambit.class, DwarvenBerserker.class, FireWhip.class, HeavyBallista.class,
        Thunderbolt.class})
class DesperateGambitTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving prompts for a source choice among the permanents you control only")
    void resolvingPromptsForOwnSourceChoice() {
        castGambit(player1);
        Permanent own = addReadyBerserker(player1);
        addReadyBerserker(player2);

        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(own.getId());
    }

    @Test
    @DisplayName("A controlled damage spell on the stack is a legal source choice")
    void stackDamageSpellIsLegalSourceChoice() {
        Permanent ownPermanent = addReadyBerserker(player1);
        Thunderbolt thunderbolt = new Thunderbolt();
        harness.setHand(player1, List.of(thunderbolt, new DesperateGambit()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, 0, player2.getId());
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(ownPermanent.getId(), thunderbolt.getId());
    }

    @Test
    @DisplayName("Choosing a source flips a coin and records a doubling or prevention shield")
    void choosingSourceFlipsAndRecordsShield() {
        castGambit(player1);
        Permanent source = addReadyBerserker(player1);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        assertThat(gd.sourceNextDamageToAnyTargetShields).hasSize(1);
        var shield = gd.sourceNextDamageToAnyTargetShields.getFirst();
        assertThat(shield.sourceId()).isEqualTo(source.getId());

        List<String> logs = gd.gameLog.stream().map(GameLogEntry::plainText).toList();
        if (shield.damageMultiplier() == 2) {
            assertThat(logs).anyMatch(entry -> entry.contains("wins the coin flip for Desperate Gambit"));
        } else {
            assertThat(shield.damageMultiplier()).isZero();
            assertThat(logs).anyMatch(entry -> entry.contains("loses the coin flip for Desperate Gambit"));
        }
    }

    @Test
    @DisplayName("The chosen source's next noncombat damage is doubled on a win, prevented on a loss")
    void nextNoncombatDamageIsDoubledOrPrevented() {
        harness.setLife(player2, 20);
        castGambit(player1);
        Permanent source = addReadyWhippedBerserker(player1);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());
        int multiplier = gd.sourceNextDamageToAnyTargetShields.getFirst().damageMultiplier();

        harness.activateAbility(player1, indexOf(player1, source), null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20 - multiplier);
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("The shield only covers the next damage event; later damage is normal")
    void onlyTheNextDamageEventIsAffected() {
        harness.setLife(player2, 20);
        castGambit(player1);
        Permanent source = addReadyWhippedBerserker(player1);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());
        int multiplier = gd.sourceNextDamageToAnyTargetShields.getFirst().damageMultiplier();

        harness.activateAbility(player1, indexOf(player1, source), null, player2.getId());
        harness.passBothPriorities();
        source.untap();
        harness.activateAbility(player1, indexOf(player1, source), null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20 - multiplier - 1);
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("Damage from a source other than the chosen one is unaffected")
    void otherSourceUnaffected() {
        harness.setLife(player2, 20);
        castGambit(player1);
        Permanent decoy = addReadyBerserker(player1);
        Permanent source = addReadyWhippedBerserker(player1);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, decoy.getId());

        harness.activateAbility(player1, indexOf(player1, source), null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.sourceNextDamageToAnyTargetShields).hasSize(1);
    }

    @Test
    @DisplayName("Combat damage from the chosen attacker is doubled or prevented")
    void combatDamageIsDoubledOrPrevented() {
        harness.setLife(player2, 20);
        castGambit(player1);
        Permanent attacker = addReadyBallista(player1);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, attacker.getId());
        int multiplier = gd.sourceNextDamageToAnyTargetShields.getFirst().damageMultiplier();

        harness.forceActivePlayer(player1);
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertLife(player2, 20 - 2 * multiplier);
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("An unused shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        castGambit(player1);
        Permanent source = addReadyBerserker(player1);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());
        assertThat(gd.sourceNextDamageToAnyTargetShields).isNotEmpty();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("Controlling no permanents leaves no prompt and no shield")
    void noOwnPermanentsNoShield() {
        castGambit(player1);
        addReadyBerserker(player2);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("Answering the source choice resumes the parked resolution entry")
    void answeringSourceChoiceClearsParkedResolution() {
        castGambit(player1);
        Permanent source = addReadyBerserker(player1);

        harness.passBothPriorities();
        assertThat(gd.pendingEffectResolutionEntry).isNotNull();

        harness.handlePermanentChosen(player1, source.getId());

        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    private void castGambit(Player player) {
        harness.castFromHand(player, new DesperateGambit(), "{R}");
    }

    private Permanent addReadyBerserker(Player player) {
        return addCreatureReady(player, new DwarvenBerserker());
    }

    private Permanent addReadyWhippedBerserker(Player player) {
        Permanent creature = addReadyBerserker(player);
        Permanent aura = new Permanent(new FireWhip());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player.getId()).add(aura);
        return creature;
    }

    private Permanent addReadyBallista(Player player) {
        return addCreatureReady(player, new HeavyBallista());
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
