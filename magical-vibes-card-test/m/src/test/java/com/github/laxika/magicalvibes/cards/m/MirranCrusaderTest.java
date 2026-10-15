package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.b.Bandage;
import com.github.laxika.magicalvibes.cards.b.BlackSunsZenith;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GoForTheThroat;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.l.LeoninSkyhunter;
import com.github.laxika.magicalvibes.cards.o.OgreResister;
import com.github.laxika.magicalvibes.cards.p.PhyrexianRager;
import com.github.laxika.magicalvibes.cards.u.UnholyStrength;
import com.github.laxika.magicalvibes.cards.v.VictorysHerald;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MirranCrusader.class, Bandage.class, GrizzlyBears.class, HolyStrength.class,
        LeoninSkyhunter.class, BlackSunsZenith.class, GiantSpider.class, GoForTheThroat.class,
        GoblinPiker.class, MirranMettle.class, MassOfGhouls.class, OgreResister.class, PhyrexianRager.class,
        UnholyStrength.class, VictorysHerald.class})
class MirranCrusaderTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Mirran Crusader puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new MirranCrusader()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Mirran Crusader");
    }

    @Test
    @DisplayName("Cannot cast Mirran Crusader without enough mana")
    void cannotCastWithoutMana() {
        harness.setHand(player1, List.of(new MirranCrusader()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Resolving puts Mirran Crusader on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new MirranCrusader()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Mirran Crusader");
    }

    @Test
    @DisplayName("Mirran Crusader enters battlefield with summoning sickness")
    void entersBattlefieldWithSummoningSickness() {
        harness.setHand(player1, List.of(new MirranCrusader()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Mirran Crusader");
        assertThat(perm.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Double strike kills 2/2 blocker in first strike phase before it deals damage")
    void doubleStrikeKillsBlockerBeforeRegularDamage() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new MirranCrusader());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new LeoninSkyhunter());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passUntil(TurnStep.END_OF_COMBAT);

        // First strike deals 2 damage killing the 2/2 blocker; Crusader survives
        harness.assertOnBattlefield(player1, "Mirran Crusader");
        harness.assertNotOnBattlefield(player2, "Leonin Skyhunter");
        harness.assertInGraveyard(player2, "Leonin Skyhunter");
    }

    @Test
    @DisplayName("Double strike deals 4 total damage killing a 4/4 blocker")
    void doubleStrikeKillsLargerBlocker() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new MirranCrusader());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new VictorysHerald());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passUntil(TurnStep.END_OF_COMBAT);

        // First strike: 2 damage to 4/4 (survives)
        // Regular damage: Crusader deals 2 more (total 4, kills 4/4), Victory's Herald deals 4 (kills Crusader)
        // Both die
        harness.assertNotOnBattlefield(player1, "Mirran Crusader");
        harness.assertInGraveyard(player1, "Mirran Crusader");
        harness.assertNotOnBattlefield(player2, "Victory's Herald");
        harness.assertInGraveyard(player2, "Victory's Herald");
    }

    @Test
    @DisplayName("Black creature cannot block Mirran Crusader")
    void blackCreatureCannotBlock() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new MirranCrusader());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new PhyrexianRager());
        blocker.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Green creature cannot block Mirran Crusader")
    void greenCreatureCannotBlock() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new MirranCrusader());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Red creature can block Mirran Crusader")
    void redCreatureCanBlock() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new MirranCrusader());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GoblinPiker());
        blocker.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Mirran Crusader takes no combat damage from black creature")
    void takesNoDamageFromBlack() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new MassOfGhouls());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new MirranCrusader());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passUntil(TurnStep.END_OF_COMBAT);

        // Crusader deals 2 first strike + 2 regular = 4 total (kills 5/3)
        // Mass of Ghouls' 5 damage to Crusader is prevented (protection)
        harness.assertNotOnBattlefield(player1, "Mass of Ghouls");
        harness.assertInGraveyard(player1, "Mass of Ghouls");
        harness.assertOnBattlefield(player2, "Mirran Crusader");
    }

    @Test
    @DisplayName("Mirran Crusader takes no combat damage from green creature")
    void takesNoDamageFromGreen() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new MirranCrusader());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passUntil(TurnStep.END_OF_COMBAT);

        // Crusader deals 2 first strike + 2 regular = 4 total (kills 2/4)
        // Giant Spider's 2 damage to Crusader is prevented (protection)
        harness.assertNotOnBattlefield(player1, "Giant Spider");
        harness.assertInGraveyard(player1, "Giant Spider");
        harness.assertOnBattlefield(player2, "Mirran Crusader");
    }

    @Test
    @DisplayName("Mirran Crusader takes normal combat damage from red creature")
    void takesNormalDamageFromRed() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new OgreResister());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new MirranCrusader());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passUntil(TurnStep.END_OF_COMBAT);

        // Crusader deals 2 first strike (4/3 survives)
        // Regular damage: Crusader deals 2 more (total 4, kills 4/3), Ogre Resister deals 4 (kills Crusader)
        // Both die
        harness.assertNotOnBattlefield(player1, "Ogre Resister");
        harness.assertNotOnBattlefield(player2, "Mirran Crusader");
        harness.assertInGraveyard(player2, "Mirran Crusader");
    }

    @Test
    @DisplayName("Cannot be targeted by black instant")
    void cannotBeTargetedByBlackInstant() {
        Permanent crusader = harness.addToBattlefieldAndReturn(player2, new MirranCrusader());
        crusader.setSummoningSick(false);

        // Add valid target so spell is playable
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setSummoningSick(false);

        harness.setHand(player1, List.of(new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, crusader.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Cannot be targeted by green instant")
    void cannotBeTargetedByGreenInstant() {
        Permanent crusader = harness.addToBattlefieldAndReturn(player2, new MirranCrusader());
        crusader.setSummoningSick(false);

        // Add valid target so spell is playable
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setSummoningSick(false);

        harness.setHand(player1, List.of(new MirranMettle()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, crusader.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from green");
    }

    @Test
    @DisplayName("Can be targeted by white instant")
    void canBeTargetedByWhiteInstant() {
        Permanent crusader = harness.addToBattlefieldAndReturn(player1, new MirranCrusader());
        crusader.setSummoningSick(false);

        harness.setHand(player1, List.of(new Bandage()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, crusader.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Bandage");
    }

    @Test
    @DisplayName("Cannot be enchanted by black aura")
    void cannotBeEnchantedByBlackAura() {
        Permanent crusader = harness.addToBattlefieldAndReturn(player2, new MirranCrusader());
        crusader.setSummoningSick(false);

        // Add valid target so aura is playable
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setSummoningSick(false);

        harness.setHand(player1, List.of(new UnholyStrength()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, crusader.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Can be enchanted by white aura (Holy Strength)")
    void canBeEnchantedByWhiteAura() {
        Permanent crusader = harness.addToBattlefieldAndReturn(player1, new MirranCrusader());
        crusader.setSummoningSick(false);

        harness.setHand(player1, List.of(new HolyStrength()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, crusader.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Holy Strength");
    }

    @Test
    @DisplayName("An unblocked Mirran Crusader deals damage in both combat damage steps")
    void unblockedDoubleStrikeDealsFourDamage() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new MirranCrusader());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.passUntil(TurnStep.END_OF_COMBAT);

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Killing a blocker with first strike does not let Mirran Crusader damage the player")
    void remainsBlockedAfterFirstStrikeKillsBlocker() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new MirranCrusader());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.addToBattlefield(player2, new LeoninSkyhunter());
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passUntil(TurnStep.END_OF_COMBAT);

        harness.assertInGraveyard(player2, "Leonin Skyhunter");
        harness.assertOnBattlefield(player1, "Mirran Crusader");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Protection does not stop nontargeted minus-one counters from a black spell")
    void blackSunsZenithCanKillCrusader() {
        harness.addToBattlefield(player2, new MirranCrusader());
        harness.setHand(player1, List.of(new BlackSunsZenith()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 2);

        harness.assertNotOnBattlefield(player2, "Mirran Crusader");
        harness.assertInGraveyard(player2, "Mirran Crusader");
    }
}
