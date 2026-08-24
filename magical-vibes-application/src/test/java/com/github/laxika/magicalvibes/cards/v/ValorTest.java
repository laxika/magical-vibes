package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Valor.class, Plains.class, GrizzlyBears.class})
class ValorTest extends BaseCardTest {

    @Test
    @DisplayName("Grants first strike to the controller's creatures while in the graveyard and a Plains is controlled")
    void grantsFirstStrikeWithPlainsInGraveyard() {
        harness.setGraveyard(player1, List.of(new Valor()));
        harness.addToBattlefield(player1, new Plains());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, own, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Requires a Plains controlled by the graveyard card's controller")
    void requiresOwnPlains() {
        harness.setGraveyard(player1, List.of(new Valor()));
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Plains());

        assertThat(gqs.hasKeyword(gd, own, Keyword.FIRST_STRIKE)).isFalse();

        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.hasKeyword(gd, own, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not use its graveyard ability while on the battlefield")
    void doesNotFunctionFromBattlefield() {
        harness.addToBattlefield(player1, new Valor());
        harness.addToBattlefield(player1, new Plains());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, own, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Stops granting first strike when Valor leaves the graveyard")
    void stopsWhenValorLeavesGraveyard() {
        harness.setGraveyard(player1, List.of(new Valor()));
        harness.addToBattlefield(player1, new Plains());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, own, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerGraveyards.get(player1.getId()).clear();

        assertThat(gqs.hasKeyword(gd, own, Keyword.FIRST_STRIKE)).isFalse();
    }
}
