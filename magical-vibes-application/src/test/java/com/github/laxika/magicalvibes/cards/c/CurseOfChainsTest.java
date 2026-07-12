package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CurseOfChainsTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Resolving Curse of Chains attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new CurseOfChains()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Curse of Chains")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    // ===== Upkeep trigger taps the enchanted creature =====

    @Test
    @DisplayName("Taps the enchanted creature during upkeep")
    void tapsEnchantedCreatureDuringUpkeep() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());
        Permanent auraPerm = new Permanent(new CurseOfChains());
        auraPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(bearsPerm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Taps the enchanted creature during each player's upkeep")
    void tapsDuringEachUpkeep() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());
        Permanent auraPerm = new Permanent(new CurseOfChains());
        auraPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        // Opponent's upkeep also triggers it
        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        assertThat(bearsPerm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Taps an opponent's enchanted creature during upkeep")
    void tapsOpponentEnchantedCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent auraPerm = new Permanent(new CurseOfChains());
        auraPerm.setAttachedTo(opponentCreature.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(opponentCreature.isTapped()).isTrue();
    }
}
