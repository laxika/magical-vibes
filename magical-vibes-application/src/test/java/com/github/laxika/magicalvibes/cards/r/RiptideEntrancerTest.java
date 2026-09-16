package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MistformWall;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiptideEntrancer.class, MistformWall.class, Island.class})
class RiptideEntrancerTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the may sacrifices Riptide Entrancer and permanently gains control of the damaged player's creature")
    void acceptSacrificeAndGainControl() {
        Permanent attacker = addAttacker();
        Permanent target = addCreatureReady(player2, new MistformWall());

        resolveCombatUnblocked();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Riptide Entrancer");
        harness.assertInGraveyard(player1, "Riptide Entrancer");
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.newestControlEffectFor(target.getId()).duration())
                .isEqualTo(com.github.laxika.magicalvibes.model.effect.EffectDuration.PERMANENT);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(attacker.getId()));
    }

    @Test
    @DisplayName("The target choice offers only creatures controlled by the damaged player")
    void targetChoiceIsRestrictedToDamagedPlayerCreatures() {
        addAttacker();
        Permanent target = addCreatureReady(player2, new MistformWall());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent ownCreature = addCreatureReady(player1, new MistformWall());

        resolveCombatUnblocked();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(target.getId())
                .doesNotContain(land.getId(), ownCreature.getId());
    }

    @Test
    @DisplayName("Declining the may keeps both permanents under their original control")
    void declineKeepsBothPermanents() {
        addAttacker();
        Permanent target = addCreatureReady(player2, new MistformWall());

        resolveCombatUnblocked();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Riptide Entrancer");
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.controlEffectsFor(target.getId())).isEmpty();
    }

    @Test
    @DisplayName("Blocked combat damage does not trigger Riptide Entrancer's ability")
    void blockedCombatDamageDoesNotTrigger() {
        addAttacker();
        Permanent blocker = addCreatureReady(player2, new MistformWall());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.assertNotOnBattlefield(player1, "Riptide Entrancer");
        harness.assertInGraveyard(player1, "Riptide Entrancer");
    }

    @Test
    @DisplayName("No creature controlled by the damaged player means the ability has no legal target")
    void noCreatureMeansNoTriggeredAbility() {
        Permanent attacker = addAttacker();
        harness.addToBattlefieldAndReturn(player2, new Island());

        resolveCombatUnblocked();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(attacker.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Riptide Entrancer"));
    }

    @Test
    @DisplayName("Riptide Entrancer can be morphed face down and turned face up for two blue mana")
    void morphsFaceDownAndCanBeTurnedFaceUpForBlue() {
        harness.setHand(player1, List.of(new RiptideEntrancer()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent entrancer = findPermanent(player1, "Riptide Entrancer");
        assertThat(entrancer.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(entrancer));
        harness.passBothPriorities();

        assertThat(entrancer.isFaceDown()).isFalse();
    }

    private Permanent addAttacker() {
        Permanent attacker = addCreatureReady(player1, new RiptideEntrancer());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        return attacker;
    }

    private void resolveCombatUnblocked() {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }
}
