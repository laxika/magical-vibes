package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PersonalDecoy.class, GrizzlyBears.class, Shock.class})
class PersonalDecoyTest extends BaseCardTest {

    @Test
    @DisplayName("Personal Decoy enters with loyalty equal to its controller's life total")
    void entersWithLifeTotalLoyalty() {
        harness.setLife(player1, 7);

        Permanent decoy = harness.enterBattlefieldAndReturn(player1, new PersonalDecoy());

        assertThat(decoy.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
    }

    @Test
    @DisplayName("Creatures cannot attack Personal Decoy's controller")
    void creaturesCannotAttackController() {
        harness.addToBattlefield(player2, new PersonalDecoy());
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't attack that player");
    }

    @Test
    @DisplayName("Personal Decoy is exiled instead of going to the graveyard")
    void isExiledWhenDestroyed() {
        harness.setLife(player1, 1);
        Permanent decoy = harness.enterBattlefieldAndReturn(player1, new PersonalDecoy());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, decoy.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName())
                .contains("Personal Decoy");
    }

    @Test
    @DisplayName("Personal Decoy's plus one ability gains life")
    void plusOneGainsLife() {
        harness.setLife(player1, 3);
        Permanent decoy = addReadyDecoy(player1, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 4);
        assertThat(decoy.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("Personal Decoy's minus four ability draws a card")
    void minusFourDrawsCard() {
        Permanent decoy = addReadyDecoy(player1, 4);
        harness.setLibrary(player1, List.of(new Shock()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Shock");
    }

    private Permanent addReadyDecoy(Player player, int loyalty) {
        Permanent decoy = new Permanent(new PersonalDecoy());
        decoy.setCounterCount(CounterType.LOYALTY, loyalty);
        decoy.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(decoy);
        return decoy;
    }

}
