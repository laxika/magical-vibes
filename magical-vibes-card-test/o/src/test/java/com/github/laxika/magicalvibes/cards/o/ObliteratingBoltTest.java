package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.ArgothianSprite;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IronCrawCrusher;
import com.github.laxika.magicalvibes.cards.t.TeferiTemporalPilgrim;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ObliteratingBolt.class, ArgothianSprite.class, TeferiTemporalPilgrim.class,
        Forest.class, IronCrawCrusher.class, Disenchant.class})
class ObliteratingBoltTest extends BaseCardTest {

    @Test
    @DisplayName("Kills a creature and exiles it instead of putting it into the graveyard")
    void killsAndExilesCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        harness.setHand(player1, List.of(new ObliteratingBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Argothian Sprite");
        harness.assertNotInGraveyard(player2, "Argothian Sprite");
        assertThat(gameData.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Argothian Sprite");
    }

    @Test
    @DisplayName("Kills a planeswalker and exiles it instead of putting it into the graveyard")
    void killsAndExilesPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TeferiTemporalPilgrim());
        target.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new ObliteratingBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Teferi, Temporal Pilgrim");
        harness.assertNotInGraveyard(player2, "Teferi, Temporal Pilgrim");
        assertThat(gameData.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Teferi, Temporal Pilgrim");
    }

    @Test
    @DisplayName("Cannot target a noncreature nonplaneswalker permanent")
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new ObliteratingBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A surviving creature is exiled if destroyed later the same turn")
    void survivingCreatureIsExiledOnLaterDestruction() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IronCrawCrusher());
        harness.setHand(player1, List.of(new ObliteratingBolt(), new Disenchant()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Iron-Craw Crusher");
        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Iron-Craw Crusher");
        harness.assertNotInGraveyard(player2, "Iron-Craw Crusher");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName()).contains("Iron-Craw Crusher");
    }

    @Test
    @DisplayName("The exile replacement expires when the turn ends")
    void exileReplacementExpiresAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IronCrawCrusher());
        harness.setHand(player1, List.of(new ObliteratingBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Iron-Craw Crusher");
        assertThat(target.getMarkedDamage()).isEqualTo(4);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Iron-Craw Crusher");
        harness.assertInGraveyard(player2, "Iron-Craw Crusher");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A surviving planeswalker loses four loyalty without being exiled")
    void survivingPlaneswalkerLosesFourLoyalty() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TeferiTemporalPilgrim());
        target.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new ObliteratingBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Teferi, Temporal Pilgrim");
        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }
}
