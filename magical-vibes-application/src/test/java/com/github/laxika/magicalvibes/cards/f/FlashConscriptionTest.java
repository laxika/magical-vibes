package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.b.BorosSignet;
import com.github.laxika.magicalvibes.cards.v.ViashinoFangtail;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({FlashConscription.class, BorosRecruit.class, BorosSignet.class, ViashinoFangtail.class})
class FlashConscriptionTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps, steals, and grants haste to the target creature")
    void untapsStealsAndGrantsHaste() {
        Permanent target = addCreatureReady(player2, new BorosRecruit());
        target.tap();

        castFlashConscription(target, false);

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("White mana grants life equal to the stolen creature's combat damage")
    void whiteManaGrantsCombatDamageLifeGain() {
        Permanent target = addCreatureReady(player2, new BorosRecruit());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castFlashConscription(target, true);
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(target)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Without white mana, the stolen creature does not grant life from combat damage")
    void noWhiteManaMeansNoCombatDamageLifeGain() {
        Permanent target = addCreatureReady(player2, new BorosRecruit());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castFlashConscription(target, false);
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(target)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Combat damage to a creature grants life even when the stolen creature dies")
    void combatDamageToCreatureGrantsLifeAfterSourceDies() {
        Permanent target = addCreatureReady(player2, new ViashinoFangtail());
        Permanent blocker = addCreatureReady(player2, new ViashinoFangtail());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castFlashConscription(target, true);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(target);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        declareAttackersAndPrepareBlockers(List.of(attackerIndex));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The granted life ability expires before the creature attacks on the next turn")
    void lifeGainAbilityExpiresAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new BorosRecruit());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castFlashConscription(target, true);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        advanceToUpkeep(player2);
        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(target)));
        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The granted life ability does not trigger for noncombat damage")
    void nonCombatDamageDoesNotGrantLife() {
        Permanent target = addCreatureReady(player2, new ViashinoFangtail());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castFlashConscription(target, true);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(target), null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Each white-mana cast grants an independent combat-damage life ability")
    void repeatedWhiteManaCastsGrantIndependentAbilities() {
        Permanent target = addCreatureReady(player2, new BorosRecruit());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        FlashConscription first = new FlashConscription();
        FlashConscription second = new FlashConscription();
        harness.setHand(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 10);

        harness.castInstant(player1, 0, target.getId());
        assertThat(gd.getSpellCastManaSpentByColor(first.getId(), ManaColor.WHITE)).isPositive();
        harness.passBothPriorities();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(5);
        harness.castInstant(player1, 0, target.getId());
        assertThat(gd.getSpellCastManaSpentByColor(second.getId(), ManaColor.WHITE)).isPositive();
        harness.passBothPriorities();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(target)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Control and haste expire at end of turn")
    void temporaryEffectsExpireAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new BorosRecruit());

        castFlashConscription(target, true);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BorosSignet());
        harness.setHand(player1, List.of(new FlashConscription()));
        addMana(false);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castFlashConscription(Permanent target, boolean whiteSpent) {
        harness.setHand(player1, List.of(new FlashConscription()));
        addMana(whiteSpent);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void addMana(boolean whiteSpent) {
        harness.addMana(player1, ManaColor.RED, 1);
        if (whiteSpent) {
            harness.addMana(player1, ManaColor.WHITE, 5);
        } else {
            harness.addMana(player1, ManaColor.COLORLESS, 5);
        }
    }
}
