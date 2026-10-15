package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.o.Ovinize;
import com.github.laxika.magicalvibes.cards.t.TheWarGames;
import com.github.laxika.magicalvibes.cards.y.YasminKhan;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DisplacedDinosaurs.class, Millstone.class, GrizzlyBears.class,
        Clone.class, TheWarGames.class, YasminKhan.class, Ovinize.class})
class DisplacedDinosaursTest extends BaseCardTest {

    @Test
    @DisplayName("Historic permanents enter as 7/7 Dinosaur creatures while retaining their other types")
    void historicPermanentsEnterAsDinosaurs() {
        harness.addToBattlefield(player1, new DisplacedDinosaurs());

        Permanent millstone = harness.enterBattlefieldAndReturn(player1, new Millstone());

        assertThat(gqs.isArtifact(gd, millstone)).isTrue();
        assertThat(gqs.isCreature(gd, millstone)).isTrue();
        assertThat(gqs.getEffectivePower(gd, millstone)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, millstone)).isEqualTo(7);
        assertThat(gqs.effectiveCreatureSubtypes(gd, millstone)).contains(CardSubtype.DINOSAUR);
    }

    @Test
    @DisplayName("Only historic permanents entering under its controller's control are affected")
    void nonHistoricAndOpponentPermanentsAreUnaffected() {
        harness.addToBattlefield(player1, new DisplacedDinosaurs());

        Permanent bears = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentArtifact = harness.enterBattlefieldAndReturn(player2, new Millstone());

        assertThat(gqs.isCreature(gd, bears)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.isCreature(gd, opponentArtifact)).isFalse();
    }

    @Test
    @DisplayName("The effect only applies as a historic permanent enters")
    void existingHistoricPermanentsAreUnaffected() {
        Permanent millstone = harness.enterBattlefieldAndReturn(player1, new Millstone());
        assertThat(gqs.isCreature(gd, millstone)).isFalse();

        harness.addToBattlefield(player1, new DisplacedDinosaurs());

        assertThat(gqs.isCreature(gd, millstone)).isFalse();
        assertThat(gqs.getEffectivePower(gd, millstone)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, millstone)).isEqualTo(0);
    }

    @Test
    @DisplayName("Legendary creatures retain their original creature subtypes when entering as Dinosaurs")
    void legendaryCreatureEntersAsDinosaur() {
        harness.addToBattlefield(player1, new DisplacedDinosaurs());

        Permanent yasmin = harness.enterBattlefieldAndReturn(player1, new YasminKhan());

        assertThat(gqs.getEffectivePower(gd, yasmin)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, yasmin)).isEqualTo(7);
        assertThat(gqs.effectiveCreatureSubtypes(gd, yasmin))
                .contains(CardSubtype.HUMAN, CardSubtype.DETECTIVE, CardSubtype.DINOSAUR);
    }

    @Test
    @DisplayName("Nonlegendary Sagas enter as Dinosaur creatures while remaining enchantments")
    void sagaEntersAsDinosaur() {
        harness.addToBattlefield(player1, new DisplacedDinosaurs());

        Permanent saga = harness.enterBattlefieldAndReturn(player1, new TheWarGames());

        assertThat(gqs.isEnchantment(gd, saga)).isTrue();
        assertThat(gqs.isCreature(gd, saga)).isTrue();
        assertThat(gqs.getEffectivePower(gd, saga)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, saga)).isEqualTo(7);
        assertThat(gqs.effectiveCreatureSubtypes(gd, saga)).contains(CardSubtype.DINOSAUR);
    }

    @Test
    @DisplayName("An entering historic permanent remains a Dinosaur after Displaced Dinosaurs leaves")
    void conversionPersistsAfterSourceLeaves() {
        Permanent dinosaurs = harness.addToBattlefieldAndReturn(player1, new DisplacedDinosaurs());
        Permanent millstone = harness.enterBattlefieldAndReturn(player1, new Millstone());

        gd.playerBattlefields.get(player1.getId()).remove(dinosaurs);

        assertThat(gqs.isArtifact(gd, millstone)).isTrue();
        assertThat(gqs.isCreature(gd, millstone)).isTrue();
        assertThat(gqs.getEffectivePower(gd, millstone)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, millstone)).isEqualTo(7);
        assertThat(gqs.effectiveCreatureSubtypes(gd, millstone)).contains(CardSubtype.DINOSAUR);
    }

    @Test
    @DisplayName("Copying a converted historic permanent copies its Dinosaur type and 7/7 stats")
    void conversionIsCopiableByOpponent() {
        harness.addToBattlefield(player1, new DisplacedDinosaurs());
        Permanent millstone = harness.enterBattlefieldAndReturn(player1, new Millstone());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, millstone.getId());

        Permanent copy = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(gqs.isArtifact(gd, copy)).isTrue();
        assertThat(gqs.isCreature(gd, copy)).isTrue();
        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(7);
        assertThat(gqs.effectiveCreatureSubtypes(gd, copy)).contains(CardSubtype.DINOSAUR);
    }

    @Test
    @DisplayName("Displaced Dinosaurs cannot convert entrants after losing its abilities")
    void losingAbilitiesStopsConversionOfNewEntrants() {
        Permanent dinosaurs = harness.addToBattlefieldAndReturn(player1, new DisplacedDinosaurs());
        Permanent converted = harness.enterBattlefieldAndReturn(player1, new Millstone());
        harness.setHand(player1, List.of(new Ovinize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, dinosaurs.getId());

        Permanent newcomer = harness.enterBattlefieldAndReturn(player1, new Millstone());

        assertThat(gqs.isCreature(gd, newcomer)).isFalse();
        assertThat(gqs.isCreature(gd, converted)).isTrue();
        assertThat(gqs.getEffectivePower(gd, converted)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, converted)).isEqualTo(7);
    }
}
