package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.Gravecrawler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RavenousRotbelly.class, Gravecrawler.class, GrizzlyBears.class})
class RavenousRotbellyTest extends BaseCardTest {

    @Test
    @DisplayName("It sacrifices up to three Zombies, then each opponent sacrifices that many creatures")
    void sacrificesChosenZombiesAndThatManyOpponentCreatures() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new Gravecrawler());
        }
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new GrizzlyBears());
        }

        castRotbelly();
        harness.handleMayAbilityChosen(player1, true);

        GameData gameData = harness.getGameData();
        PendingInteraction.MultiPermanentChoice zombieChoice =
                gameData.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(zombieChoice).isNotNull();
        assertThat(zombieChoice.maxCount()).isEqualTo(3);
        assertThat(zombieChoice.validIds()).hasSize(5);

        List<UUID> selectedZombies = gameData.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> "Gravecrawler".equals(permanent.getCard().getName()))
                .map(Permanent::getId)
                .limit(3)
                .toList();
        assertThat(selectedZombies).hasSize(3);
        harness.handleMultiplePermanentsChosen(player1, selectedZombies);

        PendingInteraction.MultiPermanentChoice creatureChoice =
                gameData.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(creatureChoice).isNotNull();
        assertThat(creatureChoice.playerId()).isEqualTo(player2.getId());
        assertThat(creatureChoice.maxCount()).isEqualTo(3);
        assertThat(creatureChoice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        harness.handleMultiplePermanentsChosen(player2, creatureChoice.validIds().stream().limit(3).toList());

        assertThat(countNamedPermanents(player1, "Gravecrawler")).isEqualTo(1);
        assertThat(countNamedPermanents(player2, "Grizzly Bears")).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the optional sacrifice leaves both players' creatures alone")
    void mayDeclineSacrifice() {
        harness.addToBattlefield(player1, new Gravecrawler());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castRotbelly();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countNamedPermanents(player1, "Gravecrawler")).isEqualTo(1);
        assertThat(countNamedPermanents(player2, "Grizzly Bears")).isEqualTo(1);
    }

    @Test
    @DisplayName("The controller may choose zero Zombies")
    void mayChooseZeroZombies() {
        harness.addToBattlefield(player1, new Gravecrawler());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castRotbelly();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countNamedPermanents(player1, "Gravecrawler")).isEqualTo(1);
        assertThat(countNamedPermanents(player2, "Grizzly Bears")).isEqualTo(1);
    }

    private void castRotbelly() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new RavenousRotbelly()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private long countNamedPermanents(com.github.laxika.magicalvibes.model.Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> name.equals(permanent.getCard().getName()))
                .count();
    }
}
