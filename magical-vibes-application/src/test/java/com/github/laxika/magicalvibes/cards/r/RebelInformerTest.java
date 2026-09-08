package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BlindingMage;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RamosianSergeant;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RebelInformerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a target nontoken Rebel on the bottom of its owner's library")
    void putsNontokenRebelOnBottomOfLibrary() {
        Permanent informer = addCreatureReady(player1, new RebelInformer());
        Permanent rebel = addCreatureReady(player2, new RamosianSergeant());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, rebel.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(informer);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(rebel);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player2.getId())).last()
                .extracting(Card::getName)
                .isEqualTo("Ramosian Sergeant");
    }

    @Test
    @DisplayName("Cannot target a non-Rebel permanent")
    void cannotTargetNonRebelPermanent() {
        addCreatureReady(player1, new RebelInformer());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nontoken Rebel");
    }

    @Test
    @DisplayName("Cannot be targeted by a white spell")
    void cannotBeTargetedByWhiteSpell() {
        Permanent informer = addCreatureReady(player1, new RebelInformer());
        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, informer.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("white");
    }

    @Test
    @DisplayName("Cannot be targeted by an ability from a white source")
    void cannotBeTargetedByWhiteSourceAbility() {
        Permanent informer = addCreatureReady(player1, new RebelInformer());
        addCreatureReady(player2, new BlindingMage());
        harness.addMana(player2, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, informer.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("white");
    }
}
