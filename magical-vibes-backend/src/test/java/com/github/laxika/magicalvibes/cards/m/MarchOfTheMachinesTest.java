package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AnimateNoncreatureArtifactsEffect;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MarchOfTheMachinesTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameService gs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("March of the Machines has correct card properties")
    void hasCorrectProperties() {
        MarchOfTheMachines card = new MarchOfTheMachines();

        assertThat(card.getName()).isEqualTo("March of the Machines");
        assertThat(card.getType()).isEqualTo(CardType.ENCHANTMENT);
        assertThat(card.getManaCost()).isEqualTo("{3}{U}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLUE);
        assertThat(card.isNeedsTarget()).isFalse();
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(AnimateNoncreatureArtifactsEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new MarchOfTheMachines()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("March of the Machines");
    }

    @Test
    @DisplayName("Resolving puts March of the Machines onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new MarchOfTheMachines()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("March of the Machines"));
    }

    // ===== Animating noncreature artifacts =====

    @Test
    @DisplayName("Noncreature artifact becomes a creature with P/T equal to mana value")
    void animatesNoncreatureArtifact() {
        // Angel's Feather costs {2}, so mana value = 2
        harness.addToBattlefield(player1, new AngelsFeather());
        harness.addToBattlefield(player1, new MarchOfTheMachines());

        Permanent feather = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Angel's Feather"))
                .findFirst().orElseThrow();

        assertThat(gs.isCreature(gd, feather)).isTrue();
        assertThat(gs.getEffectivePower(gd, feather)).isEqualTo(2);
        assertThat(gs.getEffectiveToughness(gd, feather)).isEqualTo(2);
    }

    @Test
    @DisplayName("Animated artifact does not gain any creature subtypes")
    void animatedArtifactDoesNotGainSubtypes() {
        harness.addToBattlefield(player1, new AngelsFeather());
        harness.addToBattlefield(player1, new MarchOfTheMachines());

        Permanent feather = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Angel's Feather"))
                .findFirst().orElseThrow();

        // March of the Machines makes artifacts into creatures but does NOT grant creature subtypes
        assertThat(gs.isCreature(gd, feather)).isTrue();
        assertThat(feather.getGrantedSubtypes()).isEmpty();
        assertThat(feather.getCard().getSubtypes()).isEmpty();
    }

    @Test
    @DisplayName("Icy Manipulator (cost {4}) becomes a 4/4 creature")
    void icyManipulatorBecomes4x4() {
        harness.addToBattlefield(player1, new IcyManipulator());
        harness.addToBattlefield(player1, new MarchOfTheMachines());

        Permanent icy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Icy Manipulator"))
                .findFirst().orElseThrow();

        assertThat(gs.isCreature(gd, icy)).isTrue();
        assertThat(gs.getEffectivePower(gd, icy)).isEqualTo(4);
        assertThat(gs.getEffectiveToughness(gd, icy)).isEqualTo(4);
    }

    // ===== Does not affect creatures =====

    @Test
    @DisplayName("Does not change existing creature's P/T")
    void doesNotAffectCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new MarchOfTheMachines());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    // ===== Animated artifacts benefit from creature buffs =====

    @Test
    @DisplayName("Animated artifacts benefit from Glorious Anthem")
    void animatedArtifactsBenefitFromAnthem() {
        harness.addToBattlefield(player1, new AngelsFeather());
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        harness.addToBattlefield(player1, new GloriousAnthem());

        Permanent feather = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Angel's Feather"))
                .findFirst().orElseThrow();

        // Angel's Feather: mana value 2 + Glorious Anthem +1/+1 = 3/3
        assertThat(gs.getEffectivePower(gd, feather)).isEqualTo(3);
        assertThat(gs.getEffectiveToughness(gd, feather)).isEqualTo(3);
    }

    // ===== Effect removed when March leaves =====

    @Test
    @DisplayName("Artifacts revert to non-creatures when March of the Machines leaves")
    void artifactsRevertWhenMarchLeaves() {
        harness.addToBattlefield(player1, new AngelsFeather());
        harness.addToBattlefield(player1, new MarchOfTheMachines());

        Permanent feather = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Angel's Feather"))
                .findFirst().orElseThrow();

        assertThat(gs.isCreature(gd, feather)).isTrue();
        assertThat(gs.getEffectivePower(gd, feather)).isEqualTo(2);

        // Remove March of the Machines
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("March of the Machines"));

        assertThat(gs.isCreature(gd, feather)).isFalse();
        assertThat(gs.getEffectivePower(gd, feather)).isEqualTo(0);
        assertThat(gs.getEffectiveToughness(gd, feather)).isEqualTo(0);
    }

    // ===== Affects both players' artifacts =====

    @Test
    @DisplayName("Affects artifacts on both sides of the battlefield")
    void affectsBothPlayersArtifacts() {
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        harness.addToBattlefield(player2, new IcyManipulator());

        Permanent opponentIcy = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Icy Manipulator"))
                .findFirst().orElseThrow();

        assertThat(gs.isCreature(gd, opponentIcy)).isTrue();
        assertThat(gs.getEffectivePower(gd, opponentIcy)).isEqualTo(4);
        assertThat(gs.getEffectiveToughness(gd, opponentIcy)).isEqualTo(4);
    }

    // ===== Enchantments are not affected =====

    @Test
    @DisplayName("March of the Machines does not animate enchantments")
    void doesNotAnimateEnchantments() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player1, new MarchOfTheMachines());

        Permanent anthem = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Glorious Anthem"))
                .findFirst().orElseThrow();

        assertThat(gs.isCreature(gd, anthem)).isFalse();
    }
}
