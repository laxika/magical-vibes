package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ObNixilisOfTheBlackOath.class, GrizzlyBears.class})
class ObNixilisOfTheBlackOathTest extends BaseCardTest {

    @Test
    @DisplayName("+2 drains each opponent for 1 life")
    void plusTwoDrainsEachOpponent() {
        addReadyObNixilis(player1, 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("-2 creates a flying 5/5 Demon and costs 2 life")
    void minusTwoCreatesDemon() {
        addReadyObNixilis(player1, 3);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent demon = findPermanent(player1, "Demon");
        assertThat(demon.getEffectivePower()).isEqualTo(5);
        assertThat(demon.getEffectiveToughness()).isEqualTo(5);
        assertThat(demon.getCard().getColors()).containsExactly(CardColor.BLACK);
        assertThat(demon.getCard().getKeywords()).contains(com.github.laxika.magicalvibes.model.Keyword.FLYING);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("-8 creates an emblem whose ability sacrifices a creature for life and cards")
    void minusEightCreatesFunctionalEmblem() {
        addReadyObNixilis(player1, 8);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.emblems).hasSize(1);
        Emblem emblem = gd.emblems.getFirst();
        assertThat(emblem.controllerId()).isEqualTo(player1.getId());
        assertThat(emblem.activatedAbilities()).hasSize(1);

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateEmblemAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private Permanent addReadyObNixilis(Player player, int loyalty) {
        Permanent permanent = new Permanent(new ObNixilisOfTheBlackOath());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
