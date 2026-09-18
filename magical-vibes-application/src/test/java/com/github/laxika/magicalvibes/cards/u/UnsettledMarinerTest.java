package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnsettledMariner.class, GrizzlyBears.class, Shock.class, ZuranSpellcaster.class})
class UnsettledMarinerTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an opponent's spell that targets you")
    void countersOpponentSpellTargetingPlayer() {
        harness.addToBattlefield(player1, new UnsettledMariner());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Counters an opponent's spell that targets a permanent you control")
    void countersOpponentSpellTargetingPermanent() {
        harness.addToBattlefield(player1, new UnsettledMariner());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Counters an opponent's ability that targets you")
    void countersOpponentAbilityTargetingPlayer() {
        harness.addToBattlefield(player1, new UnsettledMariner());
        Permanent spellcaster = addCreatureReady(player2, new ZuranSpellcaster());

        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(spellcaster), null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(spellcaster.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The opponent may pay {1} to keep the spell")
    void opponentMayPay() {
        harness.addToBattlefield(player1, new UnsettledMariner());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Does not trigger for its controller's own spell")
    void doesNotTriggerForControllersOwnSpell() {
        harness.addToBattlefield(player1, new UnsettledMariner());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Shock");
    }
}
