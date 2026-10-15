package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpelltitheEnforcer.class, GrizzlyBears.class, Millstone.class, Opt.class})
class SpelltitheEnforcerTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent may pay {1} when they cast a spell")
    void opponentMayPayToKeepTheirPermanent() {
        harness.addToBattlefield(player1, new SpelltitheEnforcer());
        harness.addToBattlefield(player2, new Millstone());
        castOptForPlayer2(2);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player2, "Millstone");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An opponent who declines sacrifices a permanent of their choice")
    void opponentDeclinesAndChoosesPermanentToSacrifice() {
        harness.addToBattlefield(player1, new SpelltitheEnforcer());
        harness.addToBattlefield(player2, new Millstone());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castOptForPlayer2(1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2,
                List.of(harness.getPermanentId(player2, "Grizzly Bears")));

        harness.assertOnBattlefield(player2, "Millstone");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("An opponent who accepts but cannot pay sacrifices a permanent")
    void opponentAcceptsButCannotPay() {
        harness.addToBattlefield(player1, new SpelltitheEnforcer());
        harness.addToBattlefield(player2, new Millstone());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castOptForPlayer2(1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2,
                List.of(harness.getPermanentId(player2, "Grizzly Bears")));

        harness.assertOnBattlefield(player2, "Millstone");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("An opponent with no permanents may still pay {1}")
    void opponentWithoutPermanentsMayStillPay() {
        harness.addToBattlefield(player1, new SpelltitheEnforcer());
        castOptForPlayer2(2);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Casting your own spell does not trigger Spelltithe Enforcer")
    void ownSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new SpelltitheEnforcer());
        harness.castFromHand(player1, new Opt(), "{U}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("The tax resolves before an opponent's permanent spell enters the battlefield")
    void permanentSpellResolvesAfterSacrifice() {
        harness.addToBattlefield(player1, new SpelltitheEnforcer());
        harness.addToBattlefield(player2, new SpelltitheEnforcer());
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new SpelltitheEnforcer(), "{3}{W}{W}");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Spelltithe Enforcer");
        harness.assertOnBattlefield(player2, "Spelltithe Enforcer");
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Each Spelltithe Enforcer requires a separate payment for the same spell")
    void multipleEnforcersRequireSeparatePayments() {
        harness.addToBattlefield(player1, new SpelltitheEnforcer());
        harness.addToBattlefield(player1, new SpelltitheEnforcer());
        harness.addToBattlefield(player2, new Millstone());
        castOptForPlayer2(3);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        harness.assertOnBattlefield(player2, "Millstone");
    }

    private void castOptForPlayer2(int manaAmount) {
        harness.castFromHand(player2, new Opt(), "{U}");
        if (manaAmount > 1) {
            harness.addMana(player2, ManaColor.COLORLESS, manaAmount - 1);
        }
    }
}
