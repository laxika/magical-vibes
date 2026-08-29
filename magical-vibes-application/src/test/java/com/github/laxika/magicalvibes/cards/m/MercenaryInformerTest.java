package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AbyssalHunter;
import com.github.laxika.magicalvibes.cards.d.DauthiMercenary;
import com.github.laxika.magicalvibes.cards.d.DarkBanishing;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MercenaryInformerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a target nontoken Mercenary on the bottom of its owner's library")
    void putsNontokenMercenaryOnBottomOfLibrary() {
        Permanent informer = addCreatureReady(player1, new MercenaryInformer());
        Permanent mercenary = addCreatureReady(player2, new DauthiMercenary());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, mercenary.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(informer);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(mercenary);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player2.getId())).last()
                .extracting(Card::getName)
                .isEqualTo("Dauthi Mercenary");
    }

    @Test
    @DisplayName("Cannot target a non-Mercenary permanent")
    void cannotTargetNonMercenaryPermanent() {
        addCreatureReady(player1, new MercenaryInformer());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nontoken Mercenary");
    }

    @Test
    @DisplayName("Cannot be targeted by a black spell")
    void cannotBeTargetedByBlackSpell() {
        Permanent informer = addCreatureReady(player1, new MercenaryInformer());
        harness.setHand(player2, List.of(new DarkBanishing()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, informer.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("black");
    }

    @Test
    @DisplayName("Cannot be targeted by an ability from a black source")
    void cannotBeTargetedByBlackSourceAbility() {
        Permanent informer = addCreatureReady(player1, new MercenaryInformer());
        addCreatureReady(player2, new AbyssalHunter());
        harness.addMana(player2, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, informer.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("black");
    }
}
