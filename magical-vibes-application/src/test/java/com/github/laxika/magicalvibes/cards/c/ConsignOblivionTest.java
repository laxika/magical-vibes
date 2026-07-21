package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConsignOblivionTest extends BaseCardTest {

    @Test
    @DisplayName("Consign returns target nonland permanent to hand and goes to the graveyard")
    void consignBouncesNonlandPermanent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ConsignOblivion()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Consign"));
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

        harness.castFlashback(player1, 0, player2.getId());
        harness.passBothPriorities();

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
}
