package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
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

@CardUsed({ManaLeak.class, Shock.class, YouthfulKnight.class})
class ManaLeakTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack targeting a spell")
    void castingPutsOnStackTargetingSpell() {
        YouthfulKnight knight = new YouthfulKnight();
        harness.castFromHand(player1, knight, "{1}{W}");

        ManaLeak leak = new ManaLeak();
        harness.setHand(player2, List.of(leak));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, knight.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        var leakEntry = gd.stack.getLast();
        assertThat(leakEntry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(leakEntry.getCard()).isSameAs(leak);
        assertThat(leakEntry.getTargetId()).isEqualTo(knight.getId());
    }

    // ===== Counter-unless-pays: opponent cannot pay =====

    @Test
    @DisplayName("Counters spell when opponent has no mana to pay")
    void countersWhenOpponentCannotPay() {
        YouthfulKnight knight = new YouthfulKnight();
        harness.castFromHand(player1, knight, "{1}{W}");

        harness.setHand(player2, List.of(new ManaLeak()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, knight.getId());

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Youthful Knight");
        harness.assertNotOnBattlefield(player1, "Youthful Knight");
        assertThat(gd.stack).isEmpty();
    }

    // ===== Counter-unless-pays: opponent pays =====

    @Test
    @DisplayName("Spell is not countered when opponent pays {3}")
    void spellNotCounteredWhenOpponentPays() {
        YouthfulKnight knight = new YouthfulKnight();
        harness.castFromHand(player1, knight, "{1}{W}");
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.setHand(player2, List.of(new ManaLeak()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, knight.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        // Player1 pays {3}
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotInGraveyard(player1, "Youthful Knight");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Youthful Knight");
    }

    // ===== Counter-unless-pays: opponent declines to pay =====

    @Test
    @DisplayName("Spell is countered when opponent declines to pay")
    void spellCounteredWhenOpponentDeclines() {
        YouthfulKnight knight = new YouthfulKnight();
        harness.castFromHand(player1, knight, "{1}{W}");
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.setHand(player2, List.of(new ManaLeak()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, knight.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        // Player1 declines to pay
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Youthful Knight");
        harness.assertNotOnBattlefield(player1, "Youthful Knight");
    }

    // ===== Mana payment confirmation =====

    @Test
    @DisplayName("Opponent's mana pool is reduced after paying {3}")
    void manaPoolReducedAfterPaying() {
        YouthfulKnight knight = new YouthfulKnight();
        harness.castFromHand(player1, knight, "{1}{W}");
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.setHand(player2, List.of(new ManaLeak()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, knight.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int manaBefore = gd.playerManaPools.get(player1.getId()).getTotal();
        assertThat(manaBefore).isEqualTo(3);

        harness.handleMayAbilityChosen(player1, true);

        int manaAfter = gd.playerManaPools.get(player1.getId()).getTotal();
        assertThat(manaAfter).isEqualTo(0);
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles if target spell is no longer on the stack")
    void fizzlesIfTargetSpellRemoved() {
        YouthfulKnight knight = new YouthfulKnight();
        harness.castFromHand(player1, knight, "{1}{W}");

        harness.setHand(player2, List.of(new ManaLeak()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, knight.getId());

        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getId().equals(knight.getId()));

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player2, "Mana Leak");
    }

    // ===== Mana Leak goes to graveyard =====

    @Test
    @DisplayName("Mana Leak goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        YouthfulKnight knight = new YouthfulKnight();
        harness.castFromHand(player1, knight, "{1}{W}");

        harness.setHand(player2, List.of(new ManaLeak()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, knight.getId());

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
        var knight = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());
        ManaLeak leak = new ManaLeak();
        harness.setHand(player2, List.of(leak));
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, knight.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spell on the stack");

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(leak);
        assertThat(gd.stack).isEmpty();
    }
}
