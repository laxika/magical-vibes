package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BogImp;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HulkingOgre;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.cards.r.RavenousRats;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TomeScour;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BogImp.class, Compost.class, CruelEdict.class, GrizzlyBears.class, HulkingOgre.class, Millstone.class, MindRot.class, RavenousRats.class, Shock.class, TomeScour.class})
class CompostTest extends BaseCardTest {

    // ===== Triggering =====

    @Test
    @DisplayName("Triggers when an opponent's black creature dies (from the battlefield)")
    void triggersWhenOpponentBlackCreatureDies() {
        harness.addToBattlefield(player1, new Compost());
        harness.addToBattlefield(player2, new BogImp());

        UUID bogImpId = harness.getPermanentId(player2, "Bog Imp");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bogImpId);
        harness.passBothPriorities(); // resolve Compost trigger → may prompt

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Bog Imp");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    // ===== Triggering =====

    @Test
    @DisplayName("Triggers when an opponent's black creature dies (from the battlefield)")
    void triggersWhenOpponentBlackCreatureDiesUpstreamReview() {
        harness.addToBattlefield(player1, new Compost());
        harness.addToBattlefield(player2, new RavenousRats());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Cruel Edict; the Compost trigger remains on the stack.
        harness.passBothPriorities(); // resolve Compost trigger → may prompt

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Ravenous Rats");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Triggers when a black card is milled into an opponent's graveyard")
    void triggersWhenOpponentBlackCardMilled() {
        harness.addToBattlefield(player1, new Millstone());
        harness.addToBattlefield(player1, new Compost());
        harness.setLibrary(player2, List.of(new BogImp(), new GrizzlyBears()));

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities(); // Resolve Millstone → mills 2, black card enters graveyard
        harness.passBothPriorities(); // resolve Compost trigger → may prompt

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Bog Imp");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Triggers when a black card is milled into an opponent's graveyard (from anywhere)")
    void triggersWhenOpponentBlackCardMilledUpstreamReview() {
        harness.addToBattlefield(player1, new Compost());
        // Only the top card is black — a single trigger expected.
        harness.setLibrary(player2, List.of(new RavenousRats(), new HulkingOgre(), new HulkingOgre(),
                new HulkingOgre(), new HulkingOgre()));

        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Tome Scour → mills 5, black card enters graveyard
        harness.passBothPriorities(); // resolve Compost trigger → may prompt

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Ravenous Rats");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Does NOT trigger for a non-black card put into an opponent's graveyard")
    void doesNotTriggerForNonBlackCard() {
        harness.addToBattlefield(player1, new Compost());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bearsId);

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does NOT trigger for a non-black card put into an opponent's graveyard")
    void doesNotTriggerForNonBlackCardUpstreamReview() {
        harness.addToBattlefield(player1, new Compost());
        harness.addToBattlefield(player2, new HulkingOgre());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Cruel Edict; no Compost trigger should be created.

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Hulking Ogre");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does NOT trigger when a black card goes into the controller's own graveyard")
    void doesNotTriggerForOwnBlackCard() {
        harness.addToBattlefield(player1, new Millstone());
        harness.addToBattlefield(player1, new Compost());
        // Player1 mills a black card into their OWN graveyard.
        harness.setLibrary(player1, List.of(new BogImp(), new GrizzlyBears()));

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities(); // Resolve Millstone → own black card into own graveyard

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Bog Imp");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does NOT trigger when a black card goes into the controller's own graveyard")
    void doesNotTriggerForOwnBlackCardUpstreamReview() {
        harness.addToBattlefield(player1, new Compost());
        // Player1 mills a black card into their OWN graveyard.
        harness.setLibrary(player1, List.of(new RavenousRats(), new HulkingOgre(), new HulkingOgre(),
                new HulkingOgre(), new HulkingOgre()));

        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities(); // Resolve Tome Scour → own black card into own graveyard

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Ravenous Rats");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Accepting the may ability draws a card")
    void acceptingMayAbilityDrawsCard() {
        harness.addToBattlefield(player1, new Compost());
        harness.addToBattlefield(player2, new BogImp());

        UUID bogImpId = harness.getPermanentId(player2, "Bog Imp");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bogImpId);

        int handSizeAfterCast = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.passBothPriorities(); // resolve Compost trigger → may prompt
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeAfterCast + 1);
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Accepting the may ability draws a card")
    void acceptingMayAbilityDrawsCardUpstreamReview() {
        harness.addToBattlefield(player1, new Compost());
        harness.addToBattlefield(player2, new RavenousRats());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Cruel Edict; the Compost trigger remains on the stack.

        int handSizeAfterCast = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.passBothPriorities(); // resolve Compost trigger → may prompt
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeAfterCast + 1);
    }

    @Test
    @DisplayName("Declining the may ability does not draw a card")
    void decliningMayAbilityDoesNotDrawCard() {
        harness.addToBattlefield(player1, new Compost());
        harness.addToBattlefield(player2, new BogImp());

        UUID bogImpId = harness.getPermanentId(player2, "Bog Imp");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bogImpId);

        int handSizeAfterCast = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.passBothPriorities(); // resolve Compost trigger → may prompt
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeAfterCast);
    }

    @Test
    @DisplayName("Declining the may ability does not draw a card")
    void decliningMayAbilityDoesNotDrawCardUpstreamReview() {
        harness.addToBattlefield(player1, new Compost());
        harness.addToBattlefield(player2, new RavenousRats());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Cruel Edict; the Compost trigger remains on the stack.

        int handSizeAfterCast = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.passBothPriorities(); // resolve Compost trigger → may prompt
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeAfterCast);
    }

    @Test
    @DisplayName("Triggers when an opponent discards a black card from their hand")
    void triggersWhenOpponentBlackCardIsDiscardedFromHand() {
        harness.addToBattlefield(player1, new Compost());
        harness.setHand(player2, List.of(new BogImp(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0); // Discard Bog Imp.
        harness.handleCardChosen(player2, 0); // Discard Grizzly Bears.
        harness.passBothPriorities(); // resolve Compost trigger → may prompt

        harness.assertInGraveyard(player2, "Bog Imp");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Triggers when an opponent discards a black card")
    void triggersWhenOpponentBlackCardDiscarded() {
        harness.addToBattlefield(player1, new Compost());
        harness.setHand(player2, List.of(new RavenousRats()));

        harness.setHand(player1, List.of(new RavenousRats()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Ravenous Rats; its discard trigger is on the stack.
        harness.passBothPriorities(); // Resolve the discard trigger.
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities(); // Resolve Compost trigger.

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Ravenous Rats");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Does NOT trigger when a black token is put into an opponent's graveyard")
    void doesNotTriggerForBlackToken() {
        harness.addToBattlefield(player1, new Compost());
        RavenousRats tokenCard = new RavenousRats();
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player2, tokenCard);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, token));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
