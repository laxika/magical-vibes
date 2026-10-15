package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.j.JacesDefeat;
import com.github.laxika.magicalvibes.cards.m.Manalith;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConsignOblivion.class, Forest.class, GrizzlyBears.class, LightningBolt.class, Peek.class,
        JacesDefeat.class, Manalith.class})
class ConsignOblivionTest extends BaseCardTest {

    @Test
    @DisplayName("Consign returns target nonland permanent to hand and goes to the graveyard")
    void consignBouncesNonlandPermanent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ConsignOblivion()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Consign");
    }

    @Test
    @DisplayName("Consign cannot target a land")
    void consignCannotTargetLand() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // valid target so spell is playable
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new ConsignOblivion()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    @DisplayName("Oblivion cast from graveyard makes target opponent discard two, then exiles")
    void oblivionFlashbackDiscardsAndExiles() {
        harness.setGraveyard(player1, List.of(new ConsignOblivion()));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek(), new LightningBolt())));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveFlashback(player1, 0, player2.getId());

        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(2);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Consign") || c.getName().equals("Oblivion"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Consign"));
    }

    @Test
    @DisplayName("Oblivion cannot target yourself")
    void oblivionCannotTargetSelf() {
        harness.setGraveyard(player1, List.of(new ConsignOblivion()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Oblivion requires sorcery timing")
    void oblivionRequiresSorceryTiming() {
        harness.setGraveyard(player1, List.of(new ConsignOblivion()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");
    }

    @Test
    @DisplayName("Consign returns a noncreature artifact")
    void consignBouncesArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Manalith());
        harness.setHand(player1, List.of(new ConsignOblivion()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, artifact.getId());

        harness.assertNotOnBattlefield(player2, "Manalith");
        harness.assertInHand(player2, "Manalith");
        harness.assertInGraveyard(player1, "Consign");
    }

    @Test
    @DisplayName("Oblivion discards the only card in an opponent's hand")
    void oblivionDiscardsSingleCard() {
        harness.setGraveyard(player1, List.of(new ConsignOblivion()));
        harness.setHand(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveFlashback(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Consign"));
    }

    @Test
    @DisplayName("Oblivion resolves against an opponent with an empty hand")
    void oblivionResolvesWithEmptyHand() {
        harness.setGraveyard(player1, List.of(new ConsignOblivion()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveFlashback(player1, 0, player2.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertNotInGraveyard(player1, "Consign");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Consign"));
    }

    @Test
    @DisplayName("Oblivion is black on the stack and cannot be targeted by Jace's Defeat")
    void oblivionCannotBeTargetedByBlueSpellCounter() {
        ConsignOblivion card = new ConsignOblivion();
        harness.setGraveyard(player1, List.of(card));
        harness.setHand(player2, List.of(new JacesDefeat()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castFlashback(player1, 0, player2.getId());
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, card.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
