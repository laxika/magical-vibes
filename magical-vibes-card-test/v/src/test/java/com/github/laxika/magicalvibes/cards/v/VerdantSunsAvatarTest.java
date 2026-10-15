package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CrashTheRamparts;
import com.github.laxika.magicalvibes.cards.j.JungleDelver;
import com.github.laxika.magicalvibes.cards.p.PerilousVoyage;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VerdantSunsAvatar.class, GiantSpider.class, GrizzlyBears.class,
        CrashTheRamparts.class, JungleDelver.class, PerilousVoyage.class})
class VerdantSunsAvatarTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Verdant Sun's Avatar puts it on the stack")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new VerdantSunsAvatar(), "{5}{G}{G}");

        GameData gd = harness.getGameData();

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Avatar entering triggers self life gain equal to its own toughness (5)")
    void selfEntryTriggersLifeGain() {
        harness.castFromHand(player1, new VerdantSunsAvatar(), "{5}{G}{G}");
        // Resolve creature spell → self ETB trigger on stack
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("Resolving self-ETB trigger gains 5 life")
    void selfEntryGainsFiveLife() {
        harness.castFromHand(player1, new VerdantSunsAvatar(), "{5}{G}{G}");
        // Resolve creature spell → trigger on stack
        harness.passBothPriorities();
        // Resolve trigger
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(25);
    }

    @Test
    @DisplayName("Another creature entering triggers Avatar's life gain equal to that creature's toughness")
    void anotherCreatureEnteringTriggersLifeGain() {
        harness.addToBattlefield(player1, new VerdantSunsAvatar());

        // Cast Giant Spider (2/4)
        harness.castFromHand(player1, new GiantSpider(), "{3}{G}");

        // Resolve creature spell → Avatar trigger on stack
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Another creature entering resolves and gains life equal to its toughness")
    void anotherCreatureGainsCorrectLife() {
        harness.addToBattlefield(player1, new VerdantSunsAvatar());

        // Cast Grizzly Bears (2/2)
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        // Resolve creature spell → trigger on stack
        harness.passBothPriorities();
        // Resolve trigger
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(gd.stack).isEmpty();
        // Started at 20, gained 2 life (Grizzly Bears toughness = 2)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Avatar does not trigger for opponent's creatures")
    void doesNotTriggerForOpponentCreatures() {
        // Avatar on player2's battlefield
        harness.addToBattlefield(player2, new VerdantSunsAvatar());

        // Player1 casts a creature — player2's Avatar should not trigger
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        // Resolve creature spell
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // No triggered ability on stack
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Two Verdant Sun's Avatars trigger separately for a creature entering")
    void twoAvatarsTriggerSeparately() {
        harness.addToBattlefield(player1, new VerdantSunsAvatar());
        harness.addToBattlefield(player1, new VerdantSunsAvatar());

        // Cast Grizzly Bears (2/2)
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        // Resolve creature spell → two triggers on stack
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);

        // Resolve first trigger
        harness.passBothPriorities();
        // Resolve second trigger
        harness.passBothPriorities();

        // Started at 20, gained 2 + 2 = 4 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Self-entry life gain uses toughness after a responding pump spell")
    void selfEntryUsesToughnessAtResolution() {
        harness.setHand(player1, List.of(new VerdantSunsAvatar(), new CrashTheRamparts()));
        harness.addMana(player1, ManaColor.GREEN, 10);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Verdant Sun's Avatar"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 28);
        harness.assertLife(player2, 20);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Self-entry uses last-known toughness when the pumped Avatar leaves")
    void selfEntryUsesLastKnownToughness() {
        harness.setHand(player1, List.of(new VerdantSunsAvatar(), new CrashTheRamparts()));
        harness.setHand(player2, List.of(new PerilousVoyage()));
        harness.addMana(player1, ManaColor.GREEN, 10);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        var avatarId = harness.getPermanentId(player1, "Verdant Sun's Avatar");
        harness.castInstant(player1, 0, avatarId);
        harness.passBothPriorities();
        harness.castInstant(player2, 0, avatarId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Verdant Sun's Avatar");
        harness.assertInHand(player1, "Verdant Sun's Avatar");
        harness.assertLife(player1, 28);
    }

    @Test
    @DisplayName("Ally-entry life gain uses counters added before the trigger resolves")
    void allyEntryUsesToughnessAtResolution() {
        harness.addToBattlefield(player1, new VerdantSunsAvatar());
        harness.setHand(player1, List.of(new JungleDelver()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
        assertThat(harness.getGameData().stack).isEmpty();
    }
}
