package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.SpellColorTargetFilter;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlashfreezeTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Flashfreeze has correct card properties")
    void hasCorrectProperties() {
        Flashfreeze card = new Flashfreeze();

        assertThat(card.getName()).isEqualTo("Flashfreeze");
        assertThat(card.getType()).isEqualTo(CardType.INSTANT);
        assertThat(card.getManaCost()).isEqualTo("{1}{U}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLUE);
        assertThat(card.isNeedsSpellTarget()).isTrue();
        assertThat(card.getTargetFilter()).isInstanceOf(SpellColorTargetFilter.class);
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(CounterSpellEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack targeting a green spell")
    void castingPutsOnStackTargetingGreenSpell() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, "G", 1);

        harness.setHand(player2, List.of(new Flashfreeze()));
        harness.addMana(player2, "U", 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        StackEntry flashfreezeEntry = gd.stack.getLast();
        assertThat(flashfreezeEntry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(flashfreezeEntry.getCard().getName()).isEqualTo("Flashfreeze");
        assertThat(flashfreezeEntry.getTargetPermanentId()).isEqualTo(elves.getId());
    }

    @Test
    @DisplayName("Cannot target a blue spell")
    void cannotTargetBlueSpell() {
        AirElemental airElemental = new AirElemental();
        harness.setHand(player1, List.of(airElemental));
        harness.addMana(player1, "U", 5);

        harness.setHand(player2, List.of(new Flashfreeze()));
        harness.addMana(player2, "U", 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, airElemental.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving counters a green creature spell")
    void countersGreenCreatureSpell() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, "G", 1);

        harness.setHand(player2, List.of(new Flashfreeze()));
        harness.addMana(player2, "U", 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Countered spell goes to owner's graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));
        // Does not enter the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
    }

    @Test
    @DisplayName("Resolving counters a green instant spell")
    void countersGreenInstantSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, "G", 4);

        harness.setHand(player2, List.of(new Flashfreeze()));
        harness.addMana(player2, "U", 2);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, might.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Might of Oaks countered and in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Might of Oaks"));
    }

    @Test
    @DisplayName("Flashfreeze goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, "G", 1);

        harness.setHand(player2, List.of(new Flashfreeze()));
        harness.addMana(player2, "U", 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Flashfreeze"));
        assertThat(gd.stack).isEmpty();
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles if target spell is no longer on the stack")
    void fizzlesIfTargetSpellRemoved() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, "G", 1);

        harness.setHand(player2, List.of(new Flashfreeze()));
        harness.addMana(player2, "U", 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());

        // Remove target from stack before Flashfreeze resolves
        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getName().equals("Llanowar Elves"));

        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        // Flashfreeze still goes to graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Flashfreeze"));
    }
}
