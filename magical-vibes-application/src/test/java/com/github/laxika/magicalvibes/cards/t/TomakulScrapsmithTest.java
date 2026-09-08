package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TomakulScrapsmithTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mills three and offers a milled artifact for the hand")
    void acceptsMilledArtifact() {
        Millstone millstone = new Millstone();
        setLibrary(millstone, new Forest(), new Shock());

        Permanent scrapsmith = castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(millstone);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(scrapsmith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Declining the milled artifact puts a +1/+1 counter on Tomakul Scrapsmith")
    void declinesMilledArtifactAndGetsCounter() {
        Millstone millstone = new Millstone();
        setLibrary(millstone, new Forest(), new Shock());

        Permanent scrapsmith = castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(millstone);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(scrapsmith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("No milled artifact automatically puts a +1/+1 counter on Tomakul Scrapsmith")
    void noMilledArtifactGetsCounter() {
        setLibrary(new Forest(), new Shock(), new Forest());

        Permanent scrapsmith = castAndResolveEtb();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(scrapsmith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    private Permanent castAndResolveEtb() {
        harness.setHand(player1, List.of(new TomakulScrapsmith()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Tomakul Scrapsmith");
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
