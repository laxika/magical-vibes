package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.m.Megrim;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LightwielderPaladin.class, DrudgeSkeletons.class, RuneclawBear.class, ShivanDragon.class,
        BlackKnight.class, Megrim.class, Unsummon.class})
class LightwielderPaladinTest extends BaseCardTest {

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setSummoningSick(false);
        return perm;
    }

    private void dealUnblockedDamage() {
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.BlockerDeclaration) {
            gs.declareBlockers(gd, player2, List.of());
        }
        resolveCombat();
    }

    

    @Test
    @DisplayName("Combat damage trigger presents may ability choice when defender has black permanent")
    void combatDamageTriggerPresentsMayChoice() {
        Permanent paladin = addReadyCreature(player1, new LightwielderPaladin());
        Permanent target = addReadyCreature(player2, new DrudgeSkeletons());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(paladin)));
        dealUnblockedDamage();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting may and choosing a black permanent exiles it")
    void exilesBlackPermanent() {
        Permanent paladin = addReadyCreature(player1, new LightwielderPaladin());
        Permanent blackKnight = addReadyCreature(player2, new DrudgeSkeletons());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(paladin)));
        dealUnblockedDamage();
        harness.handlePermanentChosen(player1, blackKnight.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Drudge Skeletons");
        assertThat(gd.getPlayerExiledCards(player2.getId())).anyMatch(c -> c.getName().equals("Drudge Skeletons"));
    }

    @Test
    @DisplayName("Accepting may and choosing a red permanent exiles it")
    void exilesRedPermanent() {
        Permanent paladin = addReadyCreature(player1, new LightwielderPaladin());
        Permanent dragon = addReadyCreature(player2, new ShivanDragon());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(paladin)));
        dealUnblockedDamage();
        harness.handlePermanentChosen(player1, dragon.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Shivan Dragon");
        assertThat(gd.getPlayerExiledCards(player2.getId())).anyMatch(c -> c.getName().equals("Shivan Dragon"));
    }

    @Test
    @DisplayName("Declining the may ability does nothing")
    void declineDoesNothing() {
        Permanent paladin = addReadyCreature(player1, new LightwielderPaladin());
        Permanent blackKnight = addReadyCreature(player2, new DrudgeSkeletons());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(paladin)));
        dealUnblockedDamage();
        harness.handlePermanentChosen(player1, blackKnight.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Drudge Skeletons");
        assertThat(gameLogContains("declines")).isTrue();
    }

    @Test
    @DisplayName("No trigger when defender has no black or red permanents")
    void noTriggerWhenNoValidTargets() {
        Permanent paladin = addReadyCreature(player1, new LightwielderPaladin());
        addReadyCreature(player2, new RuneclawBear()); // green, not a valid target

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(paladin)));
        dealUnblockedDamage();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("No trigger when defender has no permanents")
    void noTriggerWhenNoPermanents() {
        Permanent paladin = addReadyCreature(player1, new LightwielderPaladin());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(paladin)));
        dealUnblockedDamage();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Valid targets only include black or red permanents of the damaged player")
    void onlyBlackOrRedFromDamagedPlayer() {
        Permanent paladin = addReadyCreature(player1, new LightwielderPaladin());
        Permanent ownBlack = addReadyCreature(player1, new DrudgeSkeletons());  // own black permanent
        Permanent enemyGreen = addReadyCreature(player2, new RuneclawBear()); // enemy green
        Permanent enemyBlack = addReadyCreature(player2, new DrudgeSkeletons()); // enemy black

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(paladin)));
        dealUnblockedDamage();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(enemyBlack.getId())
                .doesNotContain(ownBlack.getId())
                .doesNotContain(enemyGreen.getId());
    }

    @Test
    @DisplayName("Paladin deals combat damage even if may is declined")
    void dealsCombatDamage() {
        harness.setLife(player2, 20);
        Permanent paladin = addReadyCreature(player1, new LightwielderPaladin());
        Permanent target = addReadyCreature(player2, new DrudgeSkeletons());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(paladin)));
        dealUnblockedDamage();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        // Lightwielder Paladin is 4/4, should deal 4 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Game advances after exile choice is made")
    void gameAdvancesAfterChoice() {
        Permanent paladin = addReadyCreature(player1, new LightwielderPaladin());
        Permanent blackKnight = addReadyCreature(player2, new DrudgeSkeletons());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(paladin)));
        dealUnblockedDamage();
        harness.handlePermanentChosen(player1, blackKnight.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    @Test
    @DisplayName("The trigger can exile a black noncreature permanent")
    void exilesBlackEnchantment() {
        Permanent paladin = addReadyCreature(player1, new LightwielderPaladin());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new Megrim());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(paladin)));
        dealUnblockedDamage();
        harness.handlePermanentChosen(player1, enchantment.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Megrim");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Megrim"));
    }

    @Test
    @DisplayName("Protection from white prevents a black permanent from being targeted")
    void cannotTargetBlackKnight() {
        Permanent paladin = addReadyCreature(player1, new LightwielderPaladin());
        Permanent protectedKnight = addReadyCreature(player2, new BlackKnight());
        Permanent target = addReadyCreature(player2, new DrudgeSkeletons());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(paladin)));
        dealUnblockedDamage();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(target.getId())
                .doesNotContain(protectedKnight.getId());
    }

    @Test
    @DisplayName("A target returned to hand in response is not replaced by another target")
    void targetLeavingBattlefieldStopsExile() {
        Permanent paladin = addReadyCreature(player1, new LightwielderPaladin());
        Permanent target = addReadyCreature(player2, new DrudgeSkeletons());
        addReadyCreature(player2, new ShivanDragon());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(paladin)));
        dealUnblockedDamage();
        harness.handlePermanentChosen(player1, target.getId());
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInHand(player2, "Drudge Skeletons");
        harness.assertOnBattlefield(player2, "Shivan Dragon");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The trigger still resolves after Paladin leaves the battlefield")
    void sourceLeavingBattlefieldDoesNotStopExile() {
        Permanent paladin = addReadyCreature(player1, new LightwielderPaladin());
        Permanent target = addReadyCreature(player2, new DrudgeSkeletons());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(paladin)));
        dealUnblockedDamage();
        harness.handlePermanentChosen(player1, target.getId());
        harness.castInstant(player1, 0, paladin.getId());
        harness.passBothPriorities();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Lightwielder Paladin");
        harness.assertNotOnBattlefield(player2, "Drudge Skeletons");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Drudge Skeletons"));
    }

    @Test
    @DisplayName("First strike kills a blocker without triggering the player damage ability")
    void blockedPaladinDoesNotTrigger() {
        Permanent paladin = addReadyCreature(player1, new LightwielderPaladin());
        addReadyCreature(player2, new RuneclawBear());
        addReadyCreature(player2, new DrudgeSkeletons());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(paladin)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player2, "Runeclaw Bear");
        harness.assertOnBattlefield(player1, "Lightwielder Paladin");
        harness.assertOnBattlefield(player2, "Drudge Skeletons");
        assertThat(paladin.getMarkedDamage()).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
