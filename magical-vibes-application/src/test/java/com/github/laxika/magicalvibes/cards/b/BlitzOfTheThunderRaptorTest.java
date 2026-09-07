package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ChandraBoldPyromancer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MagmaJet;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlitzOfTheThunderRaptor.class, ChandraBoldPyromancer.class, GrizzlyBears.class,
        MagmaJet.class, Mountain.class, Shock.class})
class BlitzOfTheThunderRaptorTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the controller's instant and sorcery cards in their graveyard")
    void dealsDamageForInstantAndSorceryCardsInOwnGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraBoldPyromancer());
        target.setCounterCount(CounterType.LOYALTY, 5);
        harness.setGraveyard(player1, List.of(new MagmaJet(), new Shock(), new Mountain()));
        harness.setGraveyard(player2, List.of(new Shock()));
        harness.setHand(player1, List.of(new BlitzOfTheThunderRaptor()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exiles a creature that would die from its damage")
    void exilesCreatureInsteadOfPuttingItIntoGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new MagmaJet(), new Shock()));
        harness.setHand(player1, List.of(new BlitzOfTheThunderRaptor()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gameData.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(target.getCard().getId()));
        assertThat(gameData.exiledCards)
                .anyMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Can target and exile a planeswalker")
    void exilesPlaneswalkerInsteadOfPuttingItIntoGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraBoldPyromancer());
        target.setCounterCount(CounterType.LOYALTY, 2);
        harness.setGraveyard(player1, List.of(new MagmaJet(), new Shock()));
        harness.setHand(player1, List.of(new BlitzOfTheThunderRaptor()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gameData.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(target.getCard().getId()));
        assertThat(gameData.exiledCards)
                .anyMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
