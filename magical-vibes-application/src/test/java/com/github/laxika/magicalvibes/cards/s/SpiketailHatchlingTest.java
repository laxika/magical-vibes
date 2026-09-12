package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Inflame;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpiketailHatchling.class, Inflame.class, SiltCrawler.class})
class SpiketailHatchlingTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Spiketail Hatchling puts it on the stack and resolves to battlefield")
    void castAndResolve() {
        SpiketailHatchling hatchling = new SpiketailHatchling();
        harness.castFromHand(player1, hatchling, "{1}{U}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(hatchling);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Spiketail Hatchling");
    }

    @Test
    @DisplayName("Activating ability sacrifices Spiketail Hatchling and puts ability on the stack")
    void activatingAbilitySacrificesAndPutsOnStack() {
        SpiketailHatchling hatchling = new SpiketailHatchling();
        harness.addToBattlefield(player2, hatchling);

        SpiketailHatchling targetSpell = new SpiketailHatchling();
        harness.castFromHand(player1, targetSpell, "{1}{U}");
        harness.passPriority(player1);

        harness.activateAbility(player2, 0, null, targetSpell.getId());

        harness.assertNotOnBattlefield(player2, "Spiketail Hatchling");
        harness.assertInGraveyard(player2, "Spiketail Hatchling");

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getLast().getCard()).isSameAs(hatchling);
    }

    @Test
    @DisplayName("Counters spell when opponent has no mana to pay")
    void countersWhenOpponentCannotPay() {
        SpiketailHatchling hatchling = new SpiketailHatchling();
        harness.addToBattlefield(player2, hatchling);

        SpiketailHatchling targetSpell = new SpiketailHatchling();
        harness.castFromHand(player1, targetSpell, "{1}{U}");
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, targetSpell.getId());

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spiketail Hatchling");
        harness.assertNotOnBattlefield(player1, "Spiketail Hatchling");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Spell is not countered when opponent pays {1}")
    void spellNotCounteredWhenOpponentPays() {
        SpiketailHatchling hatchling = new SpiketailHatchling();
        harness.addToBattlefield(player2, hatchling);

        SpiketailHatchling targetSpell = new SpiketailHatchling();
        harness.castFromHand(player1, targetSpell, "{1}{U}");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, targetSpell.getId());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotInGraveyard(player1, "Spiketail Hatchling");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spiketail Hatchling");
    }

    @Test
    @DisplayName("Spell is countered when opponent declines to pay")
    void spellCounteredWhenOpponentDeclines() {
        SpiketailHatchling hatchling = new SpiketailHatchling();
        harness.addToBattlefield(player2, hatchling);

        SpiketailHatchling targetSpell = new SpiketailHatchling();
        harness.castFromHand(player1, targetSpell, "{1}{U}");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, targetSpell.getId());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Spiketail Hatchling");
        harness.assertNotOnBattlefield(player1, "Spiketail Hatchling");
    }

    @Test
    @DisplayName("Ability fizzles if target spell is removed from the stack")
    void fizzlesIfTargetSpellRemoved() {
        SpiketailHatchling hatchling = new SpiketailHatchling();
        harness.addToBattlefield(player2, hatchling);

        Inflame targetSpell = new Inflame();
        harness.castFromHand(player1, targetSpell, "{R}");
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, targetSpell.getId());

        gd.stack.removeIf(se -> se.getCard().getId().equals(targetSpell.getId()));

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate ability without a spell on the stack")
    void cannotActivateWithoutSpellTarget() {
        SpiketailHatchling hatchling = new SpiketailHatchling();
        harness.addToBattlefield(player1, hatchling);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an activated ability on the stack")
    void cannotTargetActivatedAbility() {
        SpiketailHatchling hatchling1 = new SpiketailHatchling();
        harness.addToBattlefield(player2, hatchling1);

        SpiketailHatchling hatchling2 = new SpiketailHatchling();
        harness.addToBattlefield(player1, hatchling2);

        SpiketailHatchling targetSpell = new SpiketailHatchling();
        harness.castFromHand(player1, targetSpell, "{1}{U}");
        harness.passPriority(player1);

        harness.activateAbility(player2, 0, null, targetSpell.getId());

        assertThat(gd.stack).anyMatch(se -> se.getEntryType() == StackEntryType.ACTIVATED_ABILITY);

        var abilityEntry = gd.stack.stream()
                .filter(se -> se.getEntryType() == StackEntryType.ACTIVATED_ABILITY)
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, abilityEntry.getCard().getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Opponent's mana pool is reduced after paying {1}")
    void manaPoolReducedAfterPaying() {
        SpiketailHatchling hatchling = new SpiketailHatchling();
        harness.addToBattlefield(player2, hatchling);

        SpiketailHatchling targetSpell = new SpiketailHatchling();
        harness.castFromHand(player1, targetSpell, "{1}{U}");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, targetSpell.getId());

        harness.passBothPriorities();

        int manaBefore = gd.playerManaPools.get(player1.getId()).getTotal();
        assertThat(manaBefore).isEqualTo(1);

        harness.handleMayAbilityChosen(player1, true);

        int manaAfter = gd.playerManaPools.get(player1.getId()).getTotal();
        assertThat(manaAfter).isEqualTo(0);
    }

    @Test
    @DisplayName("Can counter own controller's spell on the stack")
    void canCounterOwnSpell() {
        SpiketailHatchling hatchling = new SpiketailHatchling();
        harness.addToBattlefield(player1, hatchling);

        SpiketailHatchling targetSpell = new SpiketailHatchling();
        harness.castFromHand(player1, targetSpell, "{1}{U}");

        harness.activateAbility(player1, 0, null, targetSpell.getId());

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spiketail Hatchling");
        harness.assertNotOnBattlefield(player1, "Spiketail Hatchling");
    }

    @Test
    @DisplayName("Can counter a noncreature spell")
    void countersNonCreatureSpell() {
        SpiketailHatchling hatchling = new SpiketailHatchling();
        harness.addToBattlefield(player2, hatchling);

        Inflame targetSpell = new Inflame();
        harness.castFromHand(player1, targetSpell, "{R}");
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, targetSpell.getId());

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Inflame");
        harness.assertNotOnBattlefield(player1, "Inflame");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flying prevents a creature without flying or reach from blocking")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new SpiketailHatchling());
        addCreatureReady(player2, new SiltCrawler());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }
}

