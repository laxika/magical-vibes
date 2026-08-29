package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LoxodonWarhammer;
import com.github.laxika.magicalvibes.cards.m.Mindslaver;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnitedBattlefront.class, DarksteelIngot.class, GrizzlyBears.class, LoxodonWarhammer.class, Mindslaver.class, MindStone.class, Plains.class, Shock.class})
class UnitedBattlefrontTest extends BaseCardTest {

    @Test
    @DisplayName("Offers up to two noncreature, nonland permanents with mana value 3 or less")
    void offersEligiblePermanentCards() {
        Card mindStone = new MindStone();
        Card darksteelIngot = new DarksteelIngot();
        Card warhammer = new LoxodonWarhammer();
        setLibrary(mindStone, darksteelIngot, warhammer,
                new Mindslaver(), new GrizzlyBears(), new Plains(), new Shock());

        castAndResolve();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                mindStone.getId(), darksteelIngot.getId(), warhammer.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.randomRemainingToBottom()).isTrue();
    }

    @Test
    @DisplayName("Puts the chosen cards onto the battlefield and bottoms the rest")
    void putsChosenCardsOntoBattlefield() {
        Card mindStone = new MindStone();
        Card darksteelIngot = new DarksteelIngot();
        Card warhammer = new LoxodonWarhammer();
        setLibrary(mindStone, darksteelIngot, warhammer,
                new Mindslaver(), new GrizzlyBears(), new Plains(), new Shock());

        castAndResolve();
        harness.handleMultipleCardsChosen(player1, List.of(mindStone.getId(), darksteelIngot.getId()));

        harness.assertOnBattlefield(player1, "Mind Stone");
        harness.assertOnBattlefield(player1, "Darksteel Ingot");
        harness.assertNotOnBattlefield(player1, "Loxodon Warhammer");
        harness.assertNotOnBattlefield(player1, "Mindslaver");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May put nothing onto the battlefield")
    void mayPutNothing() {
        Card mindStone = new MindStone();
        setLibrary(mindStone, new GrizzlyBears(), new Plains(), new Shock());

        castAndResolve();
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertNotOnBattlefield(player1, "Mind Stone");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new UnitedBattlefront()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        List<Card> library = gd.playerDecks.get(player1.getId());
        library.clear();
        library.addAll(List.of(cards));
    }
}
