package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrypticCommand.class, GrizzlyBears.class, HillGiant.class, Spellbook.class})
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
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");

        // Controller drew the top card of their library
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Counter + tap: counters target spell and taps opponents' creatures")
    void counterAndTap() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        Permanent toTap = addCreatureReady(player1, new HillGiant());

        harness.setHand(player2, List.of(new CrypticCommand()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castModalInstantWithModes(player2, 0, 2, new int[]{0, 2}, bears.getId(), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(toTap.isTapped()).isTrue();
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
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");

        // Remaining opponent creature tapped
        assertThat(toTap.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Return + draw: returns target permanent and draws a card")
    void returnAndDraw() {
        Permanent toBounce = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player2, List.of(new CrypticCommand()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        harness.castModalInstantWithModes(player2, 0, 2, new int[]{1, 3}, toBounce.getId(), List.of());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Counter + return: counters a spell and bounces a permanent (both targets)")
    void counterAndReturn() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Permanent spellbook = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        harness.setHand(player2, List.of(new CrypticCommand()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castModalInstantWithModes(player2, 0, 2, new int[]{0, 1}, bears.getId(), List.of(spellbook.getId()));
        harness.passBothPriorities();

        // Spell countered
        harness.assertInGraveyard(player1, "Grizzly Bears");

        // Permanent bounced
        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.assertInHand(player1, "Spellbook");
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
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Tap + draw: taps only creatures controlled by opponents")
    void tapAndDrawOnlyAffectsOpponentsCreatures() {
        Permanent opponentCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent ownCreature = addCreatureReady(player2, new HillGiant());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        harness.setHand(player2, List.of(new CrypticCommand()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        harness.castModalInstantWithModes(player2, 0, 2, new int[]{2, 3}, null, List.of());
        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(opponentArtifact.isTapped()).isFalse();
        harness.assertInHand(player2, "Grizzly Bears");
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

    @Test
    @DisplayName("Return + draw does not draw when its only target leaves the battlefield")
    void illegalReturnTargetPreventsDraw() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setHand(player2, List.of(new CrypticCommand()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        harness.castModalInstantWithModes(player2, 0, 2, new int[]{1, 3}, target.getId(), List.of());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.setGraveyard(player1, List.of(target.getCard()));
        harness.passBothPriorities();

        harness.assertNotInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Cryptic Command");
    }

    @Test
    @DisplayName("Counter + return still counters when the permanent target leaves")
    void countersWithIllegalPermanentTarget() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setHand(player2, List.of(new CrypticCommand()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castModalInstantWithModes(player2, 0, 2, new int[]{0, 1}, bears.getId(), List.of(target.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.setGraveyard(player1, List.of(target.getCard()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Spellbook");
    }

    @Test
    @DisplayName("Return + draw can return a permanent controlled by the caster")
    void returnsOwnPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        harness.setHand(player2, List.of(new CrypticCommand()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        harness.castModalInstantWithModes(player2, 0, 2, new int[]{1, 3}, target.getId(), List.of());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Spellbook");
        harness.assertNotOnBattlefield(player2, "Spellbook");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Tap + draw works with no creatures on the battlefield")
    void drawsWithNoCreaturesToTap() {
        harness.setHand(player2, List.of(new CrypticCommand()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        harness.castModalInstantWithModes(player2, 0, 2, new int[]{2, 3}, null, List.of());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
    }
}
