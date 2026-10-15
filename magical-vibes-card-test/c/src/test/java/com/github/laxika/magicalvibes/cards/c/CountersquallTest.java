package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.Banefire;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.Guile;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Countersquall.class, GrizzlyBears.class, LlanowarElves.class, MightOfOaks.class})
class CountersquallTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new Countersquall()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, elves.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Counters a noncreature spell and its controller loses 2 life")
    void countersNoncreatureSpellAndControllerLosesLife() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setLife(player1, 20);

        harness.setHand(player2, List.of(new Countersquall()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, might.getId());

        // Countered spell goes to owner's graveyard
        harness.assertInGraveyard(player1, "Might of Oaks");
        // Its controller loses 2 life
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Caster of Countersquall does not lose life")
    void casterDoesNotLoseLife() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new Countersquall()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, might.getId());

        harness.assertLife(player2, 20);
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles if target spell is no longer on the stack — no life loss")
    void fizzlesIfTargetSpellRemoved() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setLife(player1, 20);

        harness.setHand(player2, List.of(new Countersquall()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, might.getId());

        // Remove target from stack before Countersquall resolves
        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getName().equals("Might of Oaks"));

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        // No life loss when spell fizzles
        harness.assertLife(player1, 20);
    }
    @Test
    @CardUsed(Banefire.class)
    @DisplayName("An uncounterable noncreature spell still causes its controller to lose life")
    void uncounterableSpellStillCausesLifeLoss() {
        Banefire banefire = new Banefire();
        harness.setHand(player1, List.of(banefire));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.setHand(player2, List.of(new Countersquall()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 5, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, banefire.getId());

        harness.assertLife(player1, 18);
        harness.assertNotInGraveyard(player1, "Banefire");
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player1, "Banefire");
    }

    @Test
    @CardUsed(Banefire.class)
    @DisplayName("Can counter its controller's own spell and that player loses life")
    void canCounterOwnSpell() {
        Banefire banefire = new Banefire();
        harness.setHand(player1, List.of(banefire, new Countersquall()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 1, player2.getId());
        harness.castAndResolveInstant(player1, 0, banefire.getId());

        harness.assertInGraveyard(player1, "Banefire");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @CardUsed(Banefire.class)
    @DisplayName("Counters the spell before applying life loss")
    void counterHappensBeforeLifeLoss() {
        Banefire banefire = new Banefire();
        harness.setHand(player1, List.of(banefire));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player2, List.of(new Countersquall()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 1, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, banefire.getId());

        List<String> resolutionEvents = gd.gameLog.stream()
                .map(GameLogEntry::plainText)
                .filter(log -> log.equals("Banefire is countered.")
                        || log.contains("loses 2 life (Countersquall)"))
                .toList();
        assertThat(resolutionEvents).hasSize(2);
        assertThat(resolutionEvents.getFirst()).isEqualTo("Banefire is countered.");
        harness.assertInGraveyard(player1, "Banefire");
        harness.assertLife(player1, 18);
    }

    @Test
    @CardUsed({Banefire.class, Guile.class})
    @DisplayName("Guile's replacement choice occurs before Countersquall's life loss")
    void counterReplacementChoicePrecedesLifeLoss() {
        harness.addToBattlefield(player2, new Guile());
        Banefire banefire = new Banefire();
        harness.setHand(player1, List.of(banefire));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player2, List.of(new Countersquall()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);

        harness.castSorcery(player1, 0, 1, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, banefire.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.assertNotInGraveyard(player1, "Banefire");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(banefire.getId()));
        harness.assertLife(player1, 20);

        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }
}
