package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AbyssalGorestalker.class, GrizzlyBears.class})
class AbyssalGorestalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Its enter-the-battlefield ability makes each player sacrifice up to two creatures")
    void eachPlayerSacrificesTwoCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castGorestalker();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Abyssal Gorestalker");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(creatureCount(player2)).isZero();
    }

    @Test
    @DisplayName("A player with more than two creatures chooses which creatures to sacrifice")
    void playerChoosesTwoCreatures() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new GrizzlyBears());
        }
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castGorestalker();

        GameData gameData = harness.getGameData();
        PendingInteraction.MultiPermanentChoice choice =
                gameData.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(((MultiPermanentChoiceContext.ForcedSacrifice) choice.context()).accumulatedSacrificeIds())
                .hasSize(2);

        List<UUID> chosenIds = creatureIds(player1).stream().limit(2).toList();
        harness.handleMultiplePermanentsChosen(player1, chosenIds);

        assertThat(creatureCount(player1)).isEqualTo(2);
        assertThat(creatureCount(player2)).isZero();
    }

    private void castGorestalker() {
        harness.setHand(player1, List.of(new AbyssalGorestalker()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private List<UUID> creatureIds(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.CREATURE))
                .map(Permanent::getId)
                .toList();
    }

    private long creatureCount(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.CREATURE))
                .count();
    }
}
