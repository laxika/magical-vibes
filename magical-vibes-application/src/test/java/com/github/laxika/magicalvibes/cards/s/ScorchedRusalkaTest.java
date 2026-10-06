package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScorchedRusalka.class, StreetbreakerWurm.class, ChandraNalaar.class})
class ScorchedRusalkaTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature and deals 1 damage to target player")
    void sacrificesCreatureAndDealsDamageToPlayer() {
        addCreatureReady(player1, new ScorchedRusalka());
        Permanent fodder = addCreatureReady(player1, new StreetbreakerWurm());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.assertInGraveyard(player1, "Streetbreaker Wurm");
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Streetbreaker Wurm");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Sacrifices a creature and deals 1 damage to target planeswalker")
    void sacrificesCreatureAndDealsDamageToPlaneswalker() {
        addCreatureReady(player1, new ScorchedRusalka());
        Permanent fodder = addCreatureReady(player1, new StreetbreakerWurm());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, planeswalker.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Streetbreaker Wurm");
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can sacrifice itself as the creature cost")
    void canSacrificeItself() {
        addCreatureReady(player1, new ScorchedRusalka());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Scorched Rusalka");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        addCreatureReady(player1, new ScorchedRusalka());
        Permanent target = addCreatureReady(player2, new StreetbreakerWurm());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate while summoning sick and target its controller")
    void canActivateWhileSummoningSickAndTargetController() {
        harness.addToBattlefield(player1, new ScorchedRusalka());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player1.getId());

        harness.assertInGraveyard(player1, "Scorched Rusalka");
        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Cannot pay the red activation cost with colorless mana")
    void cannotActivateWithOnlyColorlessMana() {
        addCreatureReady(player1, new ScorchedRusalka());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Scorched Rusalka");
        harness.assertLife(player2, 20);
    }
}
