package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GladeWatcher;
import com.github.laxika.magicalvibes.cards.t.TwinBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilumgarSorcerer.class, GladeWatcher.class, TwinBolt.class})
class SilumgarSorcererTest extends BaseCardTest {

    @Test
    @DisplayName("Declining exploit leaves Silumgar Sorcerer on the battlefield")
    void decliningExploitDoesNothing() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new SilumgarSorcerer(), "{1}{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Silumgar Sorcerer");
    }

    @Test
    @DisplayName("Exploiting another creature counters a creature spell")
    void exploitCountersCreatureSpell() {
        harness.addToBattlefield(player2, new GladeWatcher());
        GladeWatcher target = new GladeWatcher();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new SilumgarSorcerer()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castFromHand(player1, target, "{1}{G}");
        harness.passPriority(player1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, harness.getPermanentId(player2, "Glade Watcher"));
        harness.handlePermanentChosen(player2, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Glade Watcher");
        harness.assertNotOnBattlefield(player1, "Glade Watcher");
        harness.assertInGraveyard(player2, "Glade Watcher");
        harness.assertOnBattlefield(player2, "Silumgar Sorcerer");
    }

    @Test
    @DisplayName("Exploit does not target a noncreature spell")
    void exploitDoesNotTargetNoncreatureSpell() {
        harness.addToBattlefield(player2, new GladeWatcher());
        harness.setHand(player1, List.of(new TwinBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new SilumgarSorcerer()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0, Map.of(player2.getId(), 2));
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, harness.getPermanentId(player2, "Glade Watcher"));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Twin Bolt");
        harness.assertInGraveyard(player2, "Glade Watcher");
        harness.assertOnBattlefield(player2, "Silumgar Sorcerer");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Silumgar Sorcerer can exploit itself to counter a creature spell")
    void selfExploitCountersCreatureSpell() {
        GladeWatcher target = new GladeWatcher();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new SilumgarSorcerer()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castFromHand(player1, target, "{1}{G}");
        harness.passPriority(player1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, harness.getPermanentId(player2, "Silumgar Sorcerer"));
        harness.handlePermanentChosen(player2, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Silumgar Sorcerer");
        harness.assertNotOnBattlefield(player2, "Silumgar Sorcerer");
        harness.assertInGraveyard(player1, "Glade Watcher");
        harness.assertNotOnBattlefield(player1, "Glade Watcher");
    }

    @Test
    @DisplayName("Removing the sorcerer before exploit resolves prevents the counter trigger")
    void removalBeforeExploitPreventsCounter() {
        harness.addToBattlefield(player2, new GladeWatcher());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new SilumgarSorcerer()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castFromHand(player1, new GladeWatcher(), "{1}{G}");
        harness.setHand(player1, List.of(new TwinBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.passPriority(player1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.castInstant(player1, 0,
                Map.of(harness.getPermanentId(player2, "Silumgar Sorcerer"), 2));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, harness.getPermanentId(player2, "Glade Watcher"));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Silumgar Sorcerer");
        harness.assertInGraveyard(player2, "Glade Watcher");
        harness.assertOnBattlefield(player1, "Glade Watcher");
        harness.assertNotInGraveyard(player1, "Glade Watcher");
    }

    @Test
    @DisplayName("Declining exploit lets the pending creature spell resolve")
    void decliningExploitDoesNotCounterCreatureSpell() {
        harness.addToBattlefield(player2, new GladeWatcher());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new SilumgarSorcerer()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castFromHand(player1, new GladeWatcher(), "{1}{G}");
        harness.passPriority(player1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Glade Watcher");
        harness.assertOnBattlefield(player2, "Glade Watcher");
        harness.assertOnBattlefield(player2, "Silumgar Sorcerer");
        harness.assertNotInGraveyard(player1, "Glade Watcher");
        harness.assertNotInGraveyard(player2, "Glade Watcher");
    }
}
