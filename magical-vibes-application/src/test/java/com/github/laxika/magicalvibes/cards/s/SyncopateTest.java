package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.w.WoodlandDruid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Syncopate.class, WoodlandDruid.class})
class SyncopateTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack targeting a spell")
    void castingPutsOnStackTargetingSpell() {
        WoodlandDruid druid = new WoodlandDruid();
        harness.setHand(player1, List.of(druid));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new Syncopate()));
        harness.addMana(player2, ManaColor.BLUE, 3); // 1U + X=2

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 2, druid.getId());

        assertThat(gd.stack).hasSize(2);
        var syncopateEntry = gd.stack.getLast();
        assertThat(syncopateEntry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(syncopateEntry.getTargetId()).isEqualTo(druid.getId());
    }

    @Test
    @DisplayName("Counters and exiles spell when opponent has no mana to pay X")
    void countersAndExilesWhenOpponentCannotPay() {
        WoodlandDruid druid = new WoodlandDruid();
        harness.setHand(player1, List.of(druid));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new Syncopate()));
        harness.addMana(player2, ManaColor.BLUE, 2); // 1U + X=1

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, druid.getId());

        // Resolve — player1 has 0 mana, spell is countered and exiled immediately
        harness.passBothPriorities();

        // Spell should be exiled, NOT in graveyard
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(druid.getId()));
        harness.assertNotInGraveyard(player1, "Woodland Druid");
        harness.assertNotOnBattlefield(player1, "Woodland Druid");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counters and exiles spell when opponent declines to pay")
    void countersAndExilesWhenOpponentDeclines() {
        WoodlandDruid druid = new WoodlandDruid();
        harness.setHand(player1, List.of(druid));
        harness.addMana(player1, ManaColor.GREEN, 2); // 1 to cast, 1 available

        harness.setHand(player2, List.of(new Syncopate()));
        harness.addMana(player2, ManaColor.BLUE, 2); // 1U + X=1

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, druid.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        // Player1 declines to pay
        harness.handleMayAbilityChosen(player1, false);

        // Spell should be exiled, NOT in graveyard
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(druid.getId()));
        harness.assertNotInGraveyard(player1, "Woodland Druid");
    }

    @Test
    @DisplayName("Spell is not countered when opponent pays X")
    void spellNotCounteredWhenOpponentPays() {
        WoodlandDruid druid = new WoodlandDruid();
        harness.setHand(player1, List.of(druid));
        harness.addMana(player1, ManaColor.GREEN, 3); // 1 to cast, 2 to pay X=2

        harness.setHand(player2, List.of(new Syncopate()));
        harness.addMana(player2, ManaColor.BLUE, 3); // 1U + X=2

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 2, druid.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        // Player1 pays {2}
        harness.handleMayAbilityChosen(player1, true);

        // Woodland Druid should not be countered
        harness.assertNotInGraveyard(player1, "Woodland Druid");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getId().equals(druid.getId()));

        // Resolve the Woodland Druid spell
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Woodland Druid");
    }

    @Test
    @DisplayName("Opponent's mana pool is reduced after paying X")
    void manaPoolReducedAfterPaying() {
        WoodlandDruid druid = new WoodlandDruid();
        harness.setHand(player1, List.of(druid));
        harness.addMana(player1, ManaColor.GREEN, 4); // 1 to cast, 3 to pay X=3

        harness.setHand(player2, List.of(new Syncopate()));
        harness.addMana(player2, ManaColor.BLUE, 4); // 1U + X=3

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 3, druid.getId());
        harness.passBothPriorities();

        int manaBefore = gd.playerManaPools.get(player1.getId()).getTotal();
        assertThat(manaBefore).isEqualTo(3); // 4 added - 1 to cast

        harness.handleMayAbilityChosen(player1, true);

        int manaAfter = gd.playerManaPools.get(player1.getId()).getTotal();
        assertThat(manaAfter).isEqualTo(0); // 3 - 3 paid
    }

    @Test
    @DisplayName("X=0 offers a zero-mana payment choice before countering and exiling")
    void xEqualsZeroOffersPaymentChoice() {
        WoodlandDruid druid = new WoodlandDruid();
        harness.setHand(player1, List.of(druid));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new Syncopate()));
        harness.addMana(player2, ManaColor.BLUE, 1); // U + X=0

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 0, druid.getId());

        // Resolve — X=0, opponent can always pay {0}, so may-ability is prompted
        harness.passBothPriorities();

        // The zero payment is always available, so the player is asked.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        // Player1 declines to pay {0} (silly but valid)
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(druid.getId()));
    }

    @Test
    @DisplayName("Fizzles if target spell is no longer on the stack")
    void fizzlesIfTargetSpellRemoved() {
        WoodlandDruid druid = new WoodlandDruid();
        harness.setHand(player1, List.of(druid));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new Syncopate()));
        harness.addMana(player2, ManaColor.BLUE, 3); // 1U + X=2

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 2, druid.getId());

        gd.stack.removeIf(se -> se.getCard().getId().equals(druid.getId()));

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player2, "Syncopate");
    }

    @Test
    @DisplayName("Syncopate goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        WoodlandDruid druid = new WoodlandDruid();
        harness.setHand(player1, List.of(druid));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new Syncopate()));
        harness.addMana(player2, ManaColor.BLUE, 2); // 1U + X=1

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, druid.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Syncopate");
    }
}
