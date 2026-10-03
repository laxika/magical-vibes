package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.t.TyphoidRats;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CurseOfDeathsHold.class, WalkingCorpse.class, TyphoidRats.class})
class CurseOfDeathsHoldTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new CurseOfDeathsHold()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castEnchantment(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Resolving puts curse onto the battlefield attached to target player")
    void resolvingAttachesToPlayer() {
        harness.setHand(player1, List.of(new CurseOfDeathsHold()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent curse = findPermanent(player1, "Curse of Death's Hold");
        assertThat(curse.getAttachedTo()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Enchanted player's creatures get -1/-1")
    void debuffsEnchantedPlayerCreatures() {
        CurseOfDeathsHold curse = new CurseOfDeathsHold();
        Permanent cursePerm = harness.addToBattlefieldAndReturn(player1, curse);
        cursePerm.setAttachedTo(player2.getId());

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());

        // 2/2 base - 1/1 from curse = 1/1
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Curse controller's own creatures are NOT affected")
    void doesNotDebuffControllerCreatures() {
        CurseOfDeathsHold curse = new CurseOfDeathsHold();
        Permanent cursePerm = harness.addToBattlefieldAndReturn(player1, curse);
        cursePerm.setAttachedTo(player2.getId());

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        // Controller's creature is unaffected: 2/2
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Debuff applies when curse resolves onto battlefield")
    void debuffAppliesOnResolve() {
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new CurseOfDeathsHold()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        Permanent bears = findPermanent(player2, "Walking Corpse");

        // Before casting, no debuff
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        // After resolving, creature debuffed
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Debuff is removed when curse leaves the battlefield")
    void debuffRemovedWhenCurseLeaves() {
        CurseOfDeathsHold curse = new CurseOfDeathsHold();
        Permanent cursePerm = harness.addToBattlefieldAndReturn(player1, curse);
        cursePerm.setAttachedTo(player2.getId());

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);

        // Remove curse
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() == curse);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two curses give -2/-2 to enchanted player's creatures")
    void twoCursesStack() {
        CurseOfDeathsHold curse1 = new CurseOfDeathsHold();
        CurseOfDeathsHold curse2 = new CurseOfDeathsHold();
        harness.addToBattlefieldAndReturn(player1, curse1).setAttachedTo(player2.getId());
        harness.addToBattlefieldAndReturn(player1, curse2).setAttachedTo(player2.getId());

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());

        // 2/2 base - 2/2 from two curses = 0/0
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(0);
    }

    @Test
    @DisplayName("A curse can enchant its controller and debuff that player's creatures")
    void canEnchantItsController() {
        Permanent corpse = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        Permanent opponentCorpse = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new CurseOfDeathsHold()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castEnchantment(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Curse of Death's Hold").getAttachedTo()).isEqualTo(player1.getId());
        assertThat(gqs.getEffectivePower(gd, corpse)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, corpse)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentCorpse)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCorpse)).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature with one toughness dies when the curse resolves")
    void oneToughnessCreatureDiesOnResolution() {
        harness.addToBattlefield(player2, new TyphoidRats());
        harness.setHand(player1, List.of(new CurseOfDeathsHold()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Typhoid Rats");
        harness.assertInGraveyard(player2, "Typhoid Rats");
        harness.assertOnBattlefield(player1, "Curse of Death's Hold");
    }

    @Test
    @DisplayName("Creatures entering later are also affected")
    void creatureEnteringLaterIsDebuffed() {
        harness.setHand(player1, List.of(new CurseOfDeathsHold()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        Permanent corpse = harness.enterBattlefieldAndReturn(player2, new WalkingCorpse());
        assertThat(gqs.getEffectivePower(gd, corpse)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, corpse)).isEqualTo(1);
        harness.enterBattlefieldAndReturn(player2, new TyphoidRats());
        harness.runStateBasedActions();
        harness.assertNotOnBattlefield(player2, "Typhoid Rats");
        harness.assertInGraveyard(player2, "Typhoid Rats");
    }
}
