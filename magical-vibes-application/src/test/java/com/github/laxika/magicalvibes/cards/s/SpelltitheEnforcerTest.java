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
    @DisplayName("Casting your own spell does not trigger Spelltithe Enforcer")
    void ownSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new SpelltitheEnforcer());
        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    private void castOptForPlayer2(int manaAmount) {
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Opt()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        if (manaAmount > 1) {
            harness.addMana(player2, ManaColor.COLORLESS, manaAmount - 1);
        }
        harness.castInstant(player2, 0);
    }
}
