package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AncientKavu;
import com.github.laxika.magicalvibes.cards.t.Tranquility;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Undermine.class, AncientKavu.class, Tranquility.class, UrzasRage.class})
class UndermineTest extends BaseCardTest {

    @Test
    void countersTargetSpellAndItsControllerLosesThreeLife() {
        AncientKavu kavu = new AncientKavu();
        Undermine undermine = new Undermine();
        harness.setHand(player1, List.of(kavu));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player1, 20);

        harness.setHand(player2, List.of(undermine));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, kavu.getId());

        harness.assertInGraveyard(player1, "Ancient Kavu");
        harness.assertNotOnBattlefield(player1, "Ancient Kavu");
        harness.assertInGraveyard(player2, "Undermine");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    void doesNotLoseLifeWhenTargetSpellIsNoLongerOnTheStack() {
        AncientKavu kavu = new AncientKavu();
        Undermine undermine = new Undermine();
        harness.setHand(player1, List.of(kavu));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player1, 20);

        harness.setHand(player2, List.of(undermine));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, kavu.getId());

        gd.stack.removeIf(entry -> entry.getCard().getId().equals(kavu.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Undermine");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    void countersNonCreatureSpell() {
        Tranquility tranquility = new Tranquility();
        Undermine undermine = new Undermine();
        harness.setHand(player1, List.of(tranquility));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.setLife(player1, 20);

        harness.setHand(player2, List.of(undermine));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, tranquility.getId());

        harness.assertInGraveyard(player1, "Tranquility");
        harness.assertInGraveyard(player2, "Undermine");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    void countersSpellBeforeItsControllerLosesLife() {
        AncientKavu kavu = new AncientKavu();
        harness.setHand(player1, List.of(kavu));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setHand(player2, List.of(new Undermine()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, kavu.getId());

        harness.assertInGraveyard(player1, "Ancient Kavu");
        harness.assertLife(player1, 17);
        List<String> resolutionEvents = gd.gameLog.stream()
                .map(GameLogEntry::plainText)
                .filter(log -> log.equals("Ancient Kavu is countered.")
                        || log.contains("loses 3 life (Undermine)"))
                .toList();
        assertThat(resolutionEvents).hasSize(2);
        assertThat(resolutionEvents.get(0)).isEqualTo("Ancient Kavu is countered.");
        assertThat(resolutionEvents.get(1)).contains("loses 3 life (Undermine)");
    }

    @Test
    void uncounterableSpellStillCausesItsControllerToLoseLife() {
        UrzasRage rage = new UrzasRage();
        harness.setHand(player1, List.of(rage));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player2, List.of(new Undermine()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, rage.getId());

        assertThat(gd.stack).hasSize(1);
        harness.assertNotInGraveyard(player1, "Urza's Rage");
        harness.assertInGraveyard(player2, "Undermine");
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Urza's Rage");
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canCounterOwnSpellAndLoseLife() {
        AncientKavu kavu = new AncientKavu();
        harness.setHand(player1, List.of(kavu, new Undermine()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player1, 0, kavu.getId());

        harness.assertInGraveyard(player1, "Ancient Kavu");
        harness.assertInGraveyard(player1, "Undermine");
        harness.assertNotOnBattlefield(player1, "Ancient Kavu");
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }
}
