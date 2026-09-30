package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HondenOfNightsReach;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChroniclerOfWorship.class, HondenOfNightsReach.class, GrizzlyBears.class})
class ChroniclerOfWorshipTest extends BaseCardTest {

    @Test
    void putsAShrineFromTheTopSevenIntoHandWithPerpetualCostReduction() {
        HondenOfNightsReach shrine = new HondenOfNightsReach();
        List<Card> library = new ArrayList<>(List.of(
                shrine,
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new HondenOfNightsReach()));
        harness.setLibrary(player1, library);
        castChronicler();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(shrine);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(7);

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .contains(shrine);
    }

    @Test
    void doesNotPutAShrineBelowTheTopSevenIntoHand() {
        HondenOfNightsReach shrine = new HondenOfNightsReach();
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            library.add(new GrizzlyBears());
        }
        library.add(shrine);
        harness.setLibrary(player1, library);
        castChronicler();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).contains(shrine);
    }

    @Test
    void tapsForManaOfAnyColor() {
        addCreatureReady(player1, new ChroniclerOfWorship());

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        Permanent chronicler = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(chronicler.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    private void castChronicler() {
        harness.setHand(player1, List.of(new ChroniclerOfWorship()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
