package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PhyrexianHulk;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RemoveSoul.class, GrizzlyBears.class, GiantGrowth.class, PhyrexianHulk.class,
        Counterspell.class, ProdigalSorcerer.class})
class RemoveSoulTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack targeting a creature spell")
    void castingPutsOnStackTargetingCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.castFromHand(player1, bears, "{1}{G}");

        RemoveSoul removeSoul = new RemoveSoul();
        harness.setHand(player2, List.of(removeSoul));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());

        assertThat(gd.stack).hasSize(2);
        StackEntry removeSoulEntry = gd.stack.getLast();
        assertThat(removeSoulEntry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(removeSoulEntry.getCard()).isSameAs(removeSoul);
        assertThat(removeSoulEntry.getTargetId()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Can target an artifact creature spell")
    void canTargetArtifactCreatureSpell() {
        PhyrexianHulk hulk = new PhyrexianHulk();
        harness.castFromHand(player1, hulk, "{6}");

        harness.setHand(player2, List.of(new RemoveSoul()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, hulk.getId());

        harness.assertInGraveyard(player1, "Phyrexian Hulk");
        harness.assertNotOnBattlefield(player1, "Phyrexian Hulk");
    }

    @Test
    @DisplayName("Cannot target a non-creature spell")
    void cannotTargetNonCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        GiantGrowth growth = new GiantGrowth();
        harness.setHand(player1, List.of(growth));
        harness.addMana(player1, ManaColor.GREEN, 1);

        RemoveSoul removeSoul = new RemoveSoul();
        harness.setHand(player2, List.of(removeSoul));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, growth.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature spell");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(removeSoul);
    }

    @Test
    @DisplayName("Resolving counters a creature spell")
    void countersCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.castFromHand(player1, bears, "{1}{G}");

        harness.setHand(player2, List.of(new RemoveSoul()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        // Countered spell goes to owner's graveyard
        harness.assertInGraveyard(player1, "Grizzly Bears");
        // Does not enter the battlefield
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Remove Soul goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.castFromHand(player1, bears, "{1}{G}");

        harness.setHand(player2, List.of(new RemoveSoul()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player2, "Remove Soul");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Fizzles if target spell is no longer on the stack")
    void fizzlesIfTargetSpellRemoved() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new RemoveSoul()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());

        Counterspell counterspell = new Counterspell();
        harness.setHand(player1, List.of(counterspell));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        // Remove Soul still goes to graveyard
        harness.assertInGraveyard(player2, "Remove Soul");
    }

    @Test
    @DisplayName("Cannot target an activated ability")
    void cannotTargetActivatedAbility() {
        ProdigalSorcerer sorcerer = new ProdigalSorcerer();
        addCreatureReady(player1, sorcerer);
        harness.activateAbility(player1, 0, null, player2.getId());

        RemoveSoul removeSoul = new RemoveSoul();
        harness.setHand(player2, List.of(removeSoul));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, sorcerer.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spell on the stack");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(removeSoul);
    }
}
