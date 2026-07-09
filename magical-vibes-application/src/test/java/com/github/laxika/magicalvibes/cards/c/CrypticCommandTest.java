package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrypticCommandTest extends BaseCardTest {

    // Mode indices: 0 = counter, 1 = return permanent, 2 = tap opponents' creatures, 3 = draw.

    @Test
    @DisplayName("Counter + draw: counters target spell and draws a card")
    void counterAndDraw() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new CrypticCommand()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castModalInstantWithModes(player2, 0, 2, new int[]{0, 3}, bears.getId(), List.of());
        harness.passBothPriorities();

        // Spell was countered
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Controller drew the top card of their library
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Return + tap: bounces target permanent and taps opponents' other creatures")
    void returnAndTap() {
        Permanent toBounce = addCreatureReady(player1, new GrizzlyBears());
        Permanent toTap = addCreatureReady(player1, new HillGiant());

        harness.setHand(player2, List.of(new CrypticCommand()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castModalInstantWithModes(player2, 0, 2, new int[]{1, 2}, toBounce.getId(), List.of());
        harness.passBothPriorities();

        // Targeted permanent bounced
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Remaining opponent creature tapped
        assertThat(toTap.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Counter + return: counters a spell and bounces a permanent (both targets)")
    void counterAndReturn() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.addToBattlefield(player1, new Spellbook());
        UUID spellbookId = harness.getPermanentId(player1, "Spellbook");

        harness.setHand(player2, List.of(new CrypticCommand()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castModalInstantWithModes(player2, 0, 2, new int[]{0, 1}, bears.getId(), List.of(spellbookId));
        harness.passBothPriorities();

        // Spell countered
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Permanent bounced
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Spellbook"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Spellbook"));
    }

    @Test
    @DisplayName("Tap + draw: needs no target, taps opponents' creatures and draws")
    void tapAndDraw() {
        Permanent toTap = addCreatureReady(player1, new HillGiant());

        harness.setHand(player2, List.of(new CrypticCommand()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        harness.castModalInstantWithModes(player2, 0, 2, new int[]{2, 3}, null, List.of());
        harness.passBothPriorities();

        assertThat(toTap.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Choosing the counter mode with no spell to target is rejected")
    void counterModeRequiresSpellTarget() {
        harness.setHand(player2, List.of(new CrypticCommand()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        assertThatThrownBy(() ->
                harness.castModalInstantWithModes(player2, 0, 2, new int[]{0, 3}, null, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }
}
