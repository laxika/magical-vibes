package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DirectCurrent;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CracklingDrake.class, ChemistersInsight.class, DirectCurrent.class, Forest.class})
class CracklingDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals your instant and sorcery cards in exile and graveyard; toughness stays 4")
    void powerCountsInstantAndSorceryCardsInExileAndGraveyard() {
        Permanent drake = addDrakeReady(player1);
        harness.setGraveyard(player1, List.of(new ChemistersInsight(), new DirectCurrent()));
        harness.setExile(player1, List.of(new ChemistersInsight()));

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(4);
    }

    @Test
    @DisplayName("Counts only your instant and sorcery cards")
    void ignoresOtherCardTypesAndOpponentsCards() {
        Permanent drake = addDrakeReady(player1);

        List<Card> ownGraveyard = new ArrayList<>();
        ownGraveyard.add(new ChemistersInsight());
        ownGraveyard.add(new CracklingDrake());
        harness.setGraveyard(player1, ownGraveyard);
        harness.setExile(player1, List.of(new Forest()));
        harness.setGraveyard(player2, List.of(new ChemistersInsight()));
        harness.setExile(player2, List.of(new DirectCurrent()));

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(4);
    }

    @Test
    @DisplayName("When it enters, you draw a card")
    void enteringDrawsACard() {
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of(new CracklingDrake()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(drawnCard.getId()));
    }

    private Permanent addDrakeReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new CracklingDrake());
        permanent.setSummoningSick(false);
        return permanent;
    }

    @Test
    void faceDownExiledSpellsDoNotIncreasePower() {
        Permanent drake = addDrakeReady(player1);
        harness.setGraveyard(player1, List.of(new DirectCurrent()));
        harness.setExile(player1, List.of(new ChemistersInsight()));
        gd.addToExile(player1.getId(), new ChemistersInsight(), null, true);
        gd.addToExile(player1.getId(), new DirectCurrent(), null, true);

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(2);
    }

    @Test
    void powerUpdatesWhenSpellsMoveBetweenZones() {
        Permanent drake = addDrakeReady(player1);
        harness.setGraveyard(player1, List.of());
        gd.exiledCards.removeIf(entry -> entry.ownerId().equals(player1.getId()));
        assertThat(gqs.getEffectivePower(gd, drake)).isZero();

        ChemistersInsight spell = new ChemistersInsight();
        harness.setGraveyard(player1, List.of(spell));
        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(1);

        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(spell));
        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(1);

        gd.exiledCards.removeIf(entry -> entry.ownerId().equals(player1.getId()));
        harness.setHand(player1, List.of(spell));
        assertThat(gqs.getEffectivePower(gd, drake)).isZero();
    }

    @Test
    void powerIsDefinedInHandGraveyardAndExile() {
        CracklingDrake drake = new CracklingDrake();
        ChemistersInsight instant = new ChemistersInsight();
        DirectCurrent sorcery = new DirectCurrent();
        harness.setHand(player1, List.of(drake));
        harness.setGraveyard(player1, List.of(instant));
        harness.setExile(player1, List.of(sorcery));
        assertThat(gqs.getEffectiveCardPower(gd, drake)).isEqualTo(2);

        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(instant, drake));
        assertThat(gqs.getEffectiveCardPower(gd, drake)).isEqualTo(2);

        harness.setGraveyard(player1, List.of(instant));
        gd.exiledCards.removeIf(entry -> entry.ownerId().equals(player1.getId()));
        harness.setExile(player1, List.of(sorcery, drake));
        assertThat(gqs.getEffectiveCardPower(gd, drake)).isEqualTo(2);
    }

    @Test
    void countsCurrentControllersCardsRatherThanDrakesOwnersCards() {
        CracklingDrake card = new CracklingDrake();
        card.setOwnerId(player1.getId());
        Permanent drake = harness.addToBattlefieldAndReturn(player2, card);
        harness.setGraveyard(player1, List.of(new ChemistersInsight(), new DirectCurrent()));
        harness.setExile(player1, List.of(new ChemistersInsight()));
        harness.setGraveyard(player2, List.of(new DirectCurrent()));
        harness.setExile(player2, List.of());
        gd.addToExile(player2.getId(), new ChemistersInsight(), null, false, player1.getId());

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(2);
    }

    @Test
    void drawTriggerStillResolvesAfterDrakeLeavesBattlefield() {
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of(new CracklingDrake()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        Card drake = gd.playerBattlefields.get(player1.getId()).getFirst().getCard();
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.setGraveyard(player1, List.of(drake));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }
}
