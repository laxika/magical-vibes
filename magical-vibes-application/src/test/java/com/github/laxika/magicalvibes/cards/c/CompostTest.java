package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BogImp;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TomeScour;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CompostTest extends BaseCardTest {

    // ===== Triggering =====

    @Test
    @DisplayName("Triggers when an opponent's black creature dies (from the battlefield)")
    void triggersWhenOpponentBlackCreatureDies() {
        harness.addToBattlefield(player1, new Compost());
        harness.addToBattlefield(player2, new BogImp());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Cruel Edict → BogImp sacrificed
        harness.passBothPriorities(); // resolve Compost trigger → may prompt

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Bog Imp"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Triggers when a black card is milled into an opponent's graveyard (from anywhere)")
    void triggersWhenOpponentBlackCardMilled() {
        harness.addToBattlefield(player1, new Compost());
        // Only the top card is black — a single trigger expected.
        harness.setLibrary(player2, List.of(new BogImp(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));

        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Tome Scour → mills 5, black card enters graveyard
        harness.passBothPriorities(); // resolve Compost trigger → may prompt

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Bog Imp"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Does NOT trigger for a non-black card put into an opponent's graveyard")
    void doesNotTriggerForNonBlackCard() {
        harness.addToBattlefield(player1, new Compost());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Cruel Edict → Grizzly Bears sacrificed

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does NOT trigger when a black card goes into the controller's own graveyard")
    void doesNotTriggerForOwnBlackCard() {
        harness.addToBattlefield(player1, new Compost());
        // Player1 mills a black card into their OWN graveyard.
        harness.setLibrary(player1, List.of(new BogImp(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));

        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities(); // Resolve Tome Scour → own black card into own graveyard

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Bog Imp"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Accepting the may ability draws a card")
    void acceptingMayAbilityDrawsCard() {
        harness.addToBattlefield(player1, new Compost());
        harness.addToBattlefield(player2, new BogImp());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Cruel Edict

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

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Cruel Edict

        int handSizeAfterCast = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.passBothPriorities(); // resolve Compost trigger → may prompt
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeAfterCast);
    }
}
