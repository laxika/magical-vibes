package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.c.Combust;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RocEgg;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ManaLeak.class, Shock.class, RuneclawBear.class, Combust.class, Island.class, RocEgg.class})
class ManaLeakTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack targeting a spell")
    void castingPutsOnStackTargetingSpell() {
        RuneclawBear bears = new RuneclawBear();
        harness.castFromHand(player1, bears, "{1}{G}");

        ManaLeak leak = new ManaLeak();
        harness.setHand(player2, List.of(leak));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        var leakEntry = gd.stack.getLast();
        assertThat(leakEntry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(leakEntry.getCard()).isSameAs(leak);
        assertThat(leakEntry.getTargetId()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Counters spell when opponent has no mana to pay")
    void countersWhenOpponentCannotPay() {
        RuneclawBear bears = new RuneclawBear();
        harness.castFromHand(player1, bears, "{1}{G}");

        harness.setHand(player2, List.of(new ManaLeak()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Runeclaw Bear");
        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Spell is not countered when opponent pays {3}")
    void spellNotCounteredWhenOpponentPays() {
        RuneclawBear bears = new RuneclawBear();
        harness.castFromHand(player1, bears, "{1}{G}");
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.setHand(player2, List.of(new ManaLeak()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        // Player1 pays {3}
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotInGraveyard(player1, "Runeclaw Bear");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Spell is countered when opponent declines to pay")
    void spellCounteredWhenOpponentDeclines() {
        RuneclawBear bears = new RuneclawBear();
        harness.castFromHand(player1, bears, "{1}{G}");
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.setHand(player2, List.of(new ManaLeak()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        // Player1 declines to pay
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Runeclaw Bear");
        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Opponent's mana pool is reduced after paying {3}")
    void manaPoolReducedAfterPaying() {
        RuneclawBear bears = new RuneclawBear();
        harness.castFromHand(player1, bears, "{1}{G}");
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.setHand(player2, List.of(new ManaLeak()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        GameData gd = harness.getGameData();
        int manaBefore = gd.playerManaPools.get(player1.getId()).getTotal();
        assertThat(manaBefore).isEqualTo(3);

        harness.handleMayAbilityChosen(player1, true);

        int manaAfter = gd.playerManaPools.get(player1.getId()).getTotal();
        assertThat(manaAfter).isEqualTo(0);
    }

    @Test
    @DisplayName("Fizzles if target spell is no longer on the stack")
    void fizzlesIfTargetSpellRemoved() {
        RuneclawBear bears = new RuneclawBear();
        harness.castFromHand(player1, bears, "{1}{G}");

        harness.setHand(player2, List.of(new ManaLeak()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());

        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getId().equals(bears.getId()));

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player2, "Mana Leak");
    }

    @Test
    @DisplayName("Mana Leak goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        RuneclawBear bears = new RuneclawBear();
        harness.castFromHand(player1, bears, "{1}{G}");

        harness.setHand(player2, List.of(new ManaLeak()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player2, "Mana Leak");
    }

    @Test
    @DisplayName("Counters a noncreature spell")
    void countersNonCreatureSpell() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        ManaLeak leak = new ManaLeak();
        harness.setHand(player2, List.of(leak));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, shock.getId());

        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player2, "Mana Leak");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        var bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        ManaLeak leak = new ManaLeak();
        harness.setHand(player2, List.of(leak));
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spell on the stack");

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(leak);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Controller can generate the payment mana during Mana Leak's resolution")
    void canGenerateManaDuringResolution() {
        RuneclawBear bears = new RuneclawBear();
        harness.castFromHand(player1, bears, "{1}{G}");
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new Island());
        }
        harness.setHand(player2, List.of(new ManaLeak()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        for (int i = 0; i < 3; i++) {
            gs.tapPermanent(gd, player1, i);
        }
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Runeclaw Bear");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Controller may pay even when the target spell cannot be countered")
    void mayPayForUncounterableSpell() {
        var egg = harness.addToBattlefieldAndReturn(player2, new RocEgg());
        Combust combust = new Combust();
        harness.setHand(player1, List.of(combust));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, egg.getId());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.setHand(player2, List.of(new ManaLeak()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, combust.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertNotInGraveyard(player1, "Combust");
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Roc Egg");
    }

    @Test
    @DisplayName("Mana Leak can target its caster's spell and accept mixed colors for the generic payment")
    void canTargetOwnSpellAndPayWithMixedMana() {
        RuneclawBear bears = new RuneclawBear();
        harness.castFromHand(player1, bears, "{1}{G}");
        harness.setHand(player1, List.of(new ManaLeak()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, bears.getId());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Runeclaw Bear");
        harness.assertInGraveyard(player1, "Mana Leak");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
