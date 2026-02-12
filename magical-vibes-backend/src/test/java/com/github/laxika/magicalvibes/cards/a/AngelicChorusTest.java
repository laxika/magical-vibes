package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HuntedWumpus;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AngelicChorusTest {

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

    @Test
    @DisplayName("Angelic Chorus has correct card properties")
    void angelicChorusHasCorrectProperties() {
        AngelicChorus card = new AngelicChorus();

        assertThat(card.getName()).isEqualTo("Angelic Chorus");
        assertThat(card.getType()).isEqualTo(CardType.ENCHANTMENT);
        assertThat(card.getManaCost()).isEqualTo("{3}{W}{W}");
        assertThat(card.getColor()).isEqualTo(CardColor.WHITE);
        assertThat(card.getEffects(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD).getFirst())
                .isInstanceOf(GainLifeEqualToToughnessEffect.class);
    }

    @Test
    @DisplayName("Casting Angelic Chorus puts it on the stack as an enchantment spell")
    void castingAngelicChorusPutsItOnStack() {
        harness.setHand(player1, List.of(new AngelicChorus()));
        harness.addMana(player1, "W", 5);

        harness.castEnchantment(player1, 0);

        GameData gd = harness.getGameData();

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Angelic Chorus");
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get("W")).isEqualTo(0);
    }

    @Test
    @DisplayName("Angelic Chorus resolves onto the battlefield")
    void angelicChorusResolvesOntoBattlefield() {
        harness.setHand(player1, List.of(new AngelicChorus()));
        harness.addMana(player1, "W", 5);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Angelic Chorus"));
    }

    @Test
    @DisplayName("Creature entering triggers Angelic Chorus life gain ability on the stack")
    void creatureEnteringTriggersLifeGainAbility() {
        harness.addToBattlefield(player1, new AngelicChorus());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, "G", 2);
        harness.castCreature(player1, 0);

        // Resolve creature spell → Angelic Chorus trigger goes on stack
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Angelic Chorus");
        assertThat(trigger.getEffectsToResolve()).hasSize(1);
        assertThat(trigger.getEffectsToResolve().getFirst()).isInstanceOf(GainLifeEffect.class);
        assertThat(((GainLifeEffect) trigger.getEffectsToResolve().getFirst()).amount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Angelic Chorus resolves and increases life total by creature's toughness")
    void angelicChorusLifeGainResolvesCorrectly() {
        harness.addToBattlefield(player1, new AngelicChorus());

        // Cast Grizzly Bears (2/2)
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, "G", 2);
        harness.castCreature(player1, 0);

        // Resolve creature spell → triggered ability on stack
        harness.passBothPriorities();
        // Resolve triggered ability
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(gd.stack).isEmpty();
        // Started at 20, gained 2 life (Grizzly Bears toughness = 2)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Life gain equals the entering creature's toughness")
    void lifeGainEqualsCreatureToughness() {
        harness.addToBattlefield(player1, new AngelicChorus());

        // Cast Giant Spider (2/4)
        harness.setHand(player1, List.of(new GiantSpider()));
        harness.addMana(player1, "G", 4);
        harness.castCreature(player1, 0);

        // Resolve creature spell → triggered ability
        harness.passBothPriorities();
        // Resolve triggered ability
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Started at 20, gained 4 life (Giant Spider toughness = 4)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Angelic Chorus does not trigger for opponent's creatures")
    void doesNotTriggerForOpponentCreatures() {
        // Angelic Chorus on player2's battlefield (not player1's)
        harness.addToBattlefield(player2, new AngelicChorus());

        // Player1 casts a creature — player2's Angelic Chorus should not trigger
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, "G", 2);
        harness.castCreature(player1, 0);

        // Resolve creature spell
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // No triggered ability on stack
        assertThat(gd.stack).isEmpty();
        // Both players' life unchanged
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Two Angelic Choruses trigger separately for the same creature")
    void twoChorusesTriggerSeparately() {
        harness.addToBattlefield(player1, new AngelicChorus());
        harness.addToBattlefield(player1, new AngelicChorus());

        // Cast Grizzly Bears (2/2)
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, "G", 2);
        harness.castCreature(player1, 0);

        // Resolve creature spell → two triggered abilities on stack
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);

        // Resolve first triggered ability
        harness.passBothPriorities();
        // Resolve second triggered ability
        harness.passBothPriorities();

        // Started at 20, gained 2 + 2 = 4 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Angelic Chorus triggers alongside Hunted Wumpus ETB when Wumpus enters")
    void triggersAlongsideWumpusEtb() {
        harness.addToBattlefield(player1, new AngelicChorus());

        // Cast Hunted Wumpus (6/4)
        harness.setHand(player1, List.of(new HuntedWumpus()));
        harness.addMana(player1, "G", 4);
        harness.castCreature(player1, 0);

        // Resolve creature spell → Wumpus enters → both ETB and Angelic Chorus trigger
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Stack has both: Wumpus ETB ability and Angelic Chorus trigger
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(e ->
                e.getCard().getName().equals("Angelic Chorus"));
        assertThat(gd.stack).anyMatch(e ->
                e.getCard().getName().equals("Hunted Wumpus"));

        // Angelic Chorus trigger should grant 4 life (Wumpus toughness = 4)
        StackEntry chorusTrigger = gd.stack.stream()
                .filter(e -> e.getCard().getName().equals("Angelic Chorus"))
                .findFirst().orElseThrow();
        assertThat(chorusTrigger.getEffectsToResolve().getFirst())
                .isInstanceOf(GainLifeEffect.class);
        assertThat(((GainLifeEffect) chorusTrigger.getEffectsToResolve().getFirst()).amount())
                .isEqualTo(4);
    }
}
