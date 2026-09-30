package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MycelicBallad.class, GrizzlyBears.class})
class MycelicBalladTest extends BaseCardTest {

    @Test
    @DisplayName("Each player sacrifices two creatures and you gain two life")
    void sacrificesCreaturesAndGainsLife() {
        addCreatures(player1, 2);
        addCreatures(player2, 2);
        harness.setLife(player1, 10);
        MycelicBallad otherBallad = new MycelicBallad();
        harness.setLibrary(player1, List.of(otherBallad));

        castMycelicBallad();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 12);
        assertThat(gd.getCardIntensity(findBalladId())).isEqualTo(3);
        assertThat(gd.getCardIntensity(otherBallad.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Players choose which creatures to sacrifice")
    void playersChooseCreatures() {
        Permanent ownKeep = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownSacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent otherSacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addCreatures(player2, 3);

        castMycelicBallad();

        harness.handleMultiplePermanentsChosen(player1,
                List.of(ownSacrifice.getId(), otherSacrifice.getId()));
        chooseAllCreatures(player2);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .containsExactly(ownKeep.getId());
        harness.assertLife(player1, 22);
    }

    private void castMycelicBallad() {
        harness.setHand(player1, List.of(new MycelicBallad()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void chooseAllCreatures(com.github.laxika.magicalvibes.model.Player player) {
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player.getId());
        harness.handleMultiplePermanentsChosen(player,
                choice.validIds().stream().limit(choice.maxCount()).toList());
    }

    private void addCreatures(com.github.laxika.magicalvibes.model.Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new GrizzlyBears());
        }
    }

    private java.util.UUID findBalladId() {
        return gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card instanceof MycelicBallad)
                .findFirst()
                .orElseThrow()
                .getId();
    }
}
