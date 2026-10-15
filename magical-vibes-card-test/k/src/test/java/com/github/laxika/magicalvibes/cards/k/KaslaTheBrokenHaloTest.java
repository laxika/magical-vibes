package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.StokeTheFlames;
import com.github.laxika.magicalvibes.cards.w.WandOfTheWorldsoul;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KaslaTheBrokenHalo.class, GrizzlyBears.class, StokeTheFlames.class, Shock.class})
class KaslaTheBrokenHaloTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a convoke spell scries two, then draws a card")
    void convokeSpellScriesThenDraws() {
        harness.addToBattlefield(player1, new KaslaTheBrokenHalo());
        Permanent firstConvokeCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondConvokeCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, new Shock(), new Shock()));
        harness.setHand(player1, List.of(new StokeTheFlames()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstantWithConvoke(player1, 0, List.of(player2.getId()),
                List.of(firstConvokeCreature.getId(), secondConvokeCreature.getId()));
        resolveAllTriggers();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(2);
        assertThat(firstConvokeCreature.isTapped()).isTrue();
        assertThat(secondConvokeCreature.isTapped()).isTrue();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Casting a spell without convoke does not trigger Kasla")
    void nonConvokeSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new KaslaTheBrokenHalo());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A convoke spell paid entirely with mana still triggers and draws after scry")
    void fullManaPaymentStillTriggersAndDrawsAfterBottoming() {
        harness.addToBattlefield(player1, new KaslaTheBrokenHalo());
        Card first = new Shock();
        Card second = new Shock();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new StokeTheFlames()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(first, second);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player2, 20);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        resolveAllTriggers();
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("An opponent's convoke spell does not trigger Kasla")
    void opponentsConvokeSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new KaslaTheBrokenHalo());
        harness.setHand(player2, List.of(new StokeTheFlames()));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 16);
    }

    @Test
    @DisplayName("Casting Kasla does not trigger its own ability")
    void castingKaslaDoesNotTriggerItself() {
        harness.castFromHand(player1, new KaslaTheBrokenHalo(), "{3}{U}{R}{W}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Kasla, the Broken Halo");
    }

    @Test
    @CardUsed({KaslaTheBrokenHalo.class, WandOfTheWorldsoul.class, Shock.class, GrizzlyBears.class})
    @DisplayName("A spell granted convoke by Wand of the Worldsoul triggers Kasla")
    void grantedConvokeTriggersKasla() {
        harness.addToBattlefield(player1, new KaslaTheBrokenHalo());
        harness.addToBattlefield(player1, new WandOfTheWorldsoul());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Shock(), new Shock()));

        harness.activateAbility(player1, 1, 1, null, null);
        resolveAllTriggers();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(2);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
