package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KessigDireSwineTest extends BaseCardTest {

    @Test
    @DisplayName("Does not have trample without delirium")
    void noDelirium() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));
        harness.addToBattlefield(player1, new KessigDireSwine());

        assertThat(gqs.hasKeyword(gd, findSwine(), Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Has trample with four card types in its controller's graveyard")
    void delirium() {
        setDelirium();
        harness.addToBattlefield(player1, new KessigDireSwine());

        assertThat(gqs.hasKeyword(gd, findSwine(), Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's graveyard does not count toward delirium")
    void opponentGraveyardDoesNotCount() {
        harness.setGraveyard(player2, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
        harness.addToBattlefield(player1, new KessigDireSwine());

        assertThat(gqs.hasKeyword(gd, findSwine(), Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Loses trample when its graveyard drops below four card types")
    void losesTrampleWhenDeliriumIsLost() {
        setDelirium();
        harness.addToBattlefield(player1, new KessigDireSwine());

        Permanent swine = findSwine();
        assertThat(gqs.hasKeyword(gd, swine, Keyword.TRAMPLE)).isTrue();

        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));

        assertThat(gqs.hasKeyword(gd, swine, Keyword.TRAMPLE)).isFalse();
    }

    private void setDelirium() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
    }

    private Permanent findSwine() {
        return findPermanent(player1, "Kessig Dire Swine");
    }
}
