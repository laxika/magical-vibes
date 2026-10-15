package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.Gravecrawler;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.GameData;
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

@CardUsed({RavenousRotbelly.class, Gravecrawler.class, Ornithopter.class})
class RavenousRotbellyTest extends BaseCardTest {

    @Test
    @DisplayName("It sacrifices up to three Zombies, then each opponent sacrifices that many creatures")
    void sacrificesChosenZombiesAndThatManyOpponentCreatures() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new Gravecrawler());
        }
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new Ornithopter());
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

        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice creatureChoice =
                gameData.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(creatureChoice).isNotNull();
        assertThat(creatureChoice.playerId()).isEqualTo(player2.getId());
        assertThat(creatureChoice.maxCount()).isEqualTo(3);
        assertThat(creatureChoice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        harness.handleMultiplePermanentsChosen(player2, creatureChoice.validIds().stream().limit(3).toList());

        assertThat(countNamedPermanents(player1, "Gravecrawler")).isEqualTo(1);
        assertThat(countNamedPermanents(player2, "Ornithopter")).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the optional sacrifice leaves both players' creatures alone")
    void mayDeclineSacrifice() {
        harness.addToBattlefield(player1, new Gravecrawler());
        harness.addToBattlefield(player2, new Ornithopter());

        castRotbelly();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countNamedPermanents(player1, "Gravecrawler")).isEqualTo(1);
        assertThat(countNamedPermanents(player2, "Ornithopter")).isEqualTo(1);
    }

    @Test
    @DisplayName("The controller may choose zero Zombies")
    void mayChooseZeroZombies() {
        harness.addToBattlefield(player1, new Gravecrawler());
        harness.addToBattlefield(player2, new Ornithopter());

        castRotbelly();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countNamedPermanents(player1, "Gravecrawler")).isEqualTo(1);
        assertThat(countNamedPermanents(player2, "Ornithopter")).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing Zombies creates a separate trigger before opponents choose creatures")
    void opponentsCanRespondToReflexiveTrigger() {
        harness.addToBattlefield(player1, new Gravecrawler());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());

        castRotbelly();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1,
                List.of(harness.getPermanentId(player1, "Gravecrawler")));

        harness.assertInGraveyard(player1, "Gravecrawler");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(countNamedPermanents(player2, "Ornithopter")).isEqualTo(2);

        harness.passBothPriorities();
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(choice.validIds().getFirst()));
        assertThat(countNamedPermanents(player2, "Ornithopter")).isEqualTo(1);
    }

    @Test
    @DisplayName("Rotbelly may sacrifice itself even when it is the only Zombie")
    void maySacrificeItself() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());

        castRotbelly();
        harness.handleMayAbilityChosen(player1, true);
        UUID rotbellyId = harness.getPermanentId(player1, "Ravenous Rotbelly");
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(rotbellyId);
        harness.handleMultiplePermanentsChosen(player1, List.of(rotbellyId));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ravenous Rotbelly");
        harness.assertNotOnBattlefield(player1, "Ravenous Rotbelly");
        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    @DisplayName("An opponent with fewer creatures sacrifices all they have")
    void opponentSacrificesAllWhenTheyHaveTooFewCreatures() {
        UUID first = harness.addToBattlefieldAndReturn(player1, new Gravecrawler()).getId();
        UUID second = harness.addToBattlefieldAndReturn(player1, new Gravecrawler()).getId();
        harness.addToBattlefield(player2, new Ornithopter());

        castRotbelly();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of(first, second));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Gravecrawler");
        harness.assertOnBattlefield(player1, "Ravenous Rotbelly");
        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Zombies may be sacrificed even when the opponent has no creatures")
    void maySacrificeWithNoOpponentCreatures() {
        harness.addToBattlefield(player1, new Gravecrawler());

        castRotbelly();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1,
                List.of(harness.getPermanentId(player1, "Gravecrawler")));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gravecrawler");
        harness.assertNotOnBattlefield(player1, "Gravecrawler");
        harness.assertOnBattlefield(player1, "Ravenous Rotbelly");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castRotbelly() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new RavenousRotbelly(), "{4}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private long countNamedPermanents(com.github.laxika.magicalvibes.model.Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> name.equals(permanent.getCard().getName()))
                .count();
    }
}
