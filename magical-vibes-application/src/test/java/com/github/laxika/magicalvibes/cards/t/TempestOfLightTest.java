package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.l.LoxodonWarhammer;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        TempestOfLight.class,
        RuleOfLaw.class,
        AngelicChorus.class,
        GrizzlyBears.class,
        HolyStrength.class,
        LoxodonWarhammer.class,
        Plains.class
})
class TempestOfLightTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new TempestOfLight(), "{2}{W}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard()).isInstanceOf(TempestOfLight.class);
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Destroys a single enchantment")
    void destroysSingleEnchantment() {
        harness.addToBattlefield(player1, new RuleOfLaw());
        harness.castFromHand(player1, new TempestOfLight(), "{2}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Rule of Law");
        harness.assertInGraveyard(player1, "Rule of Law");
    }

    @Test
    @DisplayName("Destroys enchantments controlled by both players")
    void destroysEnchantmentsFromBothPlayers() {
        harness.addToBattlefield(player1, new RuleOfLaw());
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.castFromHand(player1, new TempestOfLight(), "{2}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Rule of Law");
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player1, "Rule of Law");
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Destroys auras attached to creatures")
    void destroysAurasAttachedToCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);

        HolyStrength aura = new HolyStrength();
        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, aura);
        auraPerm.setAttachedTo(bears.getId());

        harness.castFromHand(player1, new TempestOfLight(), "{2}{W}");
        harness.passBothPriorities();

        // Aura is destroyed
        harness.assertNotOnBattlefield(player1, "Holy Strength");
        harness.assertInGraveyard(player1, "Holy Strength");
        // Creature survives
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not destroy creatures")
    void doesNotDestroyCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new TempestOfLight(), "{2}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not destroy artifacts or lands")
    void doesNotDestroyArtifactsOrLands() {
        harness.addToBattlefield(player1, new LoxodonWarhammer());
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player1, new RuleOfLaw());

        harness.castFromHand(player1, new TempestOfLight(), "{2}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Loxodon Warhammer");
        harness.assertOnBattlefield(player2, "Plains");
        harness.assertNotOnBattlefield(player1, "Rule of Law");
    }

    @Test
    @DisplayName("Does nothing when no enchantments on battlefield")
    void doesNothingWhenNoEnchantments() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.castFromHand(player1, new TempestOfLight(), "{2}{W}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Tempest of Light goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.castFromHand(player1, new TempestOfLight(), "{2}{W}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Tempest of Light");
    }

    @Test
    @DisplayName("Resolving logs destroyed enchantments")
    void resolvingLogsDestroyedEnchantments() {
        harness.addToBattlefield(player1, new RuleOfLaw());
        harness.castFromHand(player1, new TempestOfLight(), "{2}{W}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("Rule of Law") && log.contains("destroyed"));
    }
}

