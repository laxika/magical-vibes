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
        assertThat(gd.getCardIntensity(otherBallad.getId())).isEqualTo(3);
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

    @Test
    @DisplayName("Sacrifices and life gain wait until both players have chosen")
    void waitsForBothPlayersBeforeSacrificing() {
        addCreatures(player1, 3);
        addCreatures(player2, 3);

        castMycelicBallad();
        chooseAllCreatures(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(3);
        harness.assertLife(player1, 20);

        chooseAllCreatures(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Life gain does not depend on how many creatures can be sacrificed")
    void gainsFullLifeWithTooFewCreatures() {
        addCreatures(player2, 1);

        castMycelicBallad();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The spell gains life and intensifies even when neither player has creatures")
    void resolvesWithoutCreatures() {
        castMycelicBallad();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
        assertThat(gd.getCardIntensity(findBalladId())).isEqualTo(3);
    }

    @Test
    @DisplayName("An uncast Chorus uses starting intensity plus prior increases when cast")
    void previouslyIntensifiedBalladSacrificesThreeCreatures() {
        MycelicBallad nextBallad = new MycelicBallad();
        harness.setLibrary(player1, List.of(nextBallad));
        castMycelicBallad();

        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(nextBallad));
        addCreatures(player1, 3);
        addCreatures(player2, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
        if (gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class) != null) {
            chooseAllCreatures(player1);
            chooseAllCreatures(player2);
        }

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 25);
        assertThat(gd.getCardIntensity(nextBallad.getId())).isEqualTo(4);
    }

    private void castMycelicBallad() {
        harness.setHand(player1, List.of(new MycelicBallad()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
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
