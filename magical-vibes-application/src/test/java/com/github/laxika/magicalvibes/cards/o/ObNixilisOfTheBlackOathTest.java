package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EmblemActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ObNixilisOfTheBlackOath.class, GrizzlyBears.class})
class ObNixilisOfTheBlackOathTest extends BaseCardTest {

    @Test
    @DisplayName("+2 drains each opponent and gains the total life lost")
    void plusTwoDrainsEachOpponentAndGainsLife() {
        addReadyObNixilis(player1, 3);
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(11);
        assertThat(gd.getLife(player2.getId())).isEqualTo(9);
    }

    @Test
    @DisplayName("-2 creates a flying Demon and loses 2 life")
    void minusTwoCreatesDemonAndLosesLife() {
        addReadyObNixilis(player1, 2);
        harness.setLife(player1, 10);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(8);
        Permanent demon = findPermanent(player1, "Demon");
        assertThat(demon.getCard().getPower()).isEqualTo(5);
        assertThat(demon.getCard().getToughness()).isEqualTo(5);
        assertThat(demon.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("-8 creates an emblem with the sacrifice-for-power ability")
    void minusEightCreatesSacrificeAbilityEmblem() {
        addReadyObNixilis(player1, 8);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.emblems).singleElement().satisfies(this::assertEmblemAbility);
    }

    private void assertEmblemAbility(Emblem emblem) {
        assertThat(emblem.controllerId()).isEqualTo(player1.getId());
        EmblemActivatedAbilityEffect payload = (EmblemActivatedAbilityEffect) emblem.staticEffects().getFirst();
        ActivatedAbility ability = payload.ability();
        assertThat(ability.getManaCost()).isEqualTo("{1}{B}");
        assertThat(ability.getEffects()).containsExactly(
                new SacrificeCreatureCost(false, true),
                new GainLifeEffect(new XValue()),
                new DrawCardEffect(new XValue())
        );
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
