package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CaptainSisay;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DihadaBinderOfWills.class, CaptainSisay.class, Forest.class, GrizzlyBears.class})
class DihadaBinderOfWillsTest extends BaseCardTest {

    @Test
    @DisplayName("+2 grants the three keywords to up to one legendary creature you control")
    void plusTwoGrantsKeywordsToLegendaryCreatureYouControl() {
        Permanent dihada = addReadyDihada(player1, 5);
        Permanent sisay = harness.addToBattlefieldAndReturn(player1, new CaptainSisay());

        harness.activateAbility(player1, 0, 0, null, sisay.getId());
        harness.passBothPriorities();

        assertThat(dihada.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, sisay, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, sisay, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, sisay, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("+2 cannot target a nonlegendary creature")
    void plusTwoRejectsNonlegendaryCreature() {
        addReadyDihada(player1, 5);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-3 puts legendary cards in hand, the rest in the graveyard, and creates Treasures")
    void minusThreePutsLegendaryCardsInHandAndCreatesTreasures() {
        addReadyDihada(player1, 5);
        CaptainSisay legendary = new CaptainSisay();
        Forest firstLand = new Forest();
        GrizzlyBears bear = new GrizzlyBears();
        Forest secondLand = new Forest();
        harness.setLibrary(player1, List.of(legendary, firstLand, bear, secondLand));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(legendary.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(legendary);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(firstLand, bear, secondLand);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.TREASURE)))
                .hasSize(3);
    }

    @Test
    @DisplayName("-11 steals, untaps, and gives haste to all nonland permanents")
    void minusElevenStealsUntapsAndGivesHasteToAllNonlands() {
        Permanent dihada = addReadyDihada(player1, 11);
        Permanent sisay = harness.addToBattlefieldAndReturn(player1, new CaptainSisay());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        sisay.tap();
        bear.tap();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(dihada.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sisay, bear);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear);
        assertThat(bear.isTapped()).isFalse();
        assertThat(sisay.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, sisay, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.HASTE)).isTrue();
    }

    private Permanent addReadyDihada(Player player, int loyalty) {
        Permanent dihada = new Permanent(new DihadaBinderOfWills());
        dihada.setCounterCount(CounterType.LOYALTY, loyalty);
        dihada.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(dihada);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return dihada;
    }
}
