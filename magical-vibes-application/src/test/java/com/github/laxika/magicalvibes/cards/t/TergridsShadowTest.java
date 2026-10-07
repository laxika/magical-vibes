package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FearlessPup;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TergridsShadow.class, FearlessPup.class, Mountain.class})
class TergridsShadowTest extends BaseCardTest {

    @Test
    void eachPlayerSacrificesTwoCreatures() {
        harness.addToBattlefield(player1, new FearlessPup());
        harness.addToBattlefield(player1, new FearlessPup());
        harness.addToBattlefield(player2, new FearlessPup());
        harness.addToBattlefield(player2, new FearlessPup());
        harness.addToBattlefield(player1, new Mountain());

        TergridsShadow shadow = new TergridsShadow();
        harness.setHand(player1, List.of(shadow));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castAndResolveInstant(player1, 0);

        assertThat(creatureCount(player1)).isZero();
        assertThat(creatureCount(player2)).isZero();
        harness.assertOnBattlefield(player1, "Mountain");
    }

    @Test
    void foretellsAndCastsOnALaterTurn() {
        harness.addToBattlefield(player1, new FearlessPup());
        harness.addToBattlefield(player2, new FearlessPup());
        TergridsShadow shadow = new TergridsShadow();
        harness.setHand(player1, List.of(shadow));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(shadow.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castFromExile(player1, shadow.getId());
        harness.passBothPriorities();

        assertThat(creatureCount(player1)).isZero();
        assertThat(creatureCount(player2)).isZero();
    }

    @Test
    void bothPlayersChooseTwoBeforeAnyCreaturesAreSacrificed() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new FearlessPup());
            harness.addToBattlefield(player2, new FearlessPup());
        }
        harness.setHand(player1, List.of(new TergridsShadow()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMultiplePermanentsChosen(player1, gd.playerBattlefields.get(player1.getId()).stream()
                .limit(2).map(Permanent::getId).toList());

        assertThat(creatureCount(player1)).isEqualTo(3);
        assertThat(creatureCount(player2)).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, gd.playerBattlefields.get(player2.getId()).stream()
                .limit(2).map(Permanent::getId).toList());

        assertThat(creatureCount(player1)).isEqualTo(1);
        assertThat(creatureCount(player2)).isEqualTo(1);
    }

    @Test
    void playerWithoutCreaturesDoesNotPreventOpponentsSacrifice() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new FearlessPup());
        harness.setHand(player1, List.of(new TergridsShadow()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castAndResolveInstant(player1, 0);

        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertInGraveyard(player2, "Fearless Pup");
        harness.assertInGraveyard(player1, "Tergrid's Shadow");
    }

    @Test
    void foretoldCardCannotBeCastOnTheSameTurn() {
        TergridsShadow shadow = new TergridsShadow();
        harness.setHand(player1, List.of(shadow));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.foretell(player1, 0);

        assertThatThrownBy(() -> harness.castFromExile(player1, shadow.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(shadow.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    private long creatureCount(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.CREATURE))
                .map(Permanent::getId)
                .count();
    }
}
