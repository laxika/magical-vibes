package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CracklingDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals your instant and sorcery cards in exile and graveyard; toughness stays 4")
    void powerCountsInstantAndSorceryCardsInExileAndGraveyard() {
        Permanent drake = addDrakeReady(player1);
        harness.setGraveyard(player1, List.of(new Shock(), new Divination()));
        harness.setExile(player1, List.of(new Opt()));

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(4);
    }

    @Test
    @DisplayName("Counts only your instant and sorcery cards")
    void ignoresOtherCardTypesAndOpponentsCards() {
        Permanent drake = addDrakeReady(player1);

        List<Card> ownGraveyard = new ArrayList<>();
        ownGraveyard.add(new Shock());
        ownGraveyard.add(new GrizzlyBears());
        harness.setGraveyard(player1, ownGraveyard);
        harness.setExile(player1, List.of(new Forest()));
        harness.setGraveyard(player2, List.of(new Opt()));
        harness.setExile(player2, List.of(new Divination()));

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(4);
    }

    @Test
    @DisplayName("When it enters, you draw a card")
    void enteringDrawsACard() {
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of(new CracklingDrake()));
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(drawnCard);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(drawnCard.getId()));
    }

    private Permanent addDrakeReady(Player player) {
        Permanent permanent = new Permanent(new CracklingDrake());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
