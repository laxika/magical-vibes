package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OsseousSticktwister.class, Forest.class, GrizzlyBears.class, Millstone.class, Shock.class})
class OsseousSticktwisterTest extends BaseCardTest {

    @Test
    void doesNotTriggerWithoutDelirium() {
        harness.addToBattlefield(player1, new OsseousSticktwister());
        harness.setGraveyard(player1, List.of(new Millstone(), new GrizzlyBears(), new Shock()));

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentMayDiscardInsteadOfTakingDamage() {
        harness.addToBattlefield(player1, new OsseousSticktwister());
        enableDelirium();
        harness.setHand(player2, List.of(new Shock()));

        resolveTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void opponentMaySacrificeNonlandInsteadOfTakingDamage() {
        harness.addToBattlefield(player1, new OsseousSticktwister());
        enableDelirium();
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player2, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        resolveTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(findPermanent(player2, "Forest"));
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
    }

    @Test
    void landCannotBeSacrificedAndDamageUsesCurrentPower() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new OsseousSticktwister());
        source.setPowerModifier(2);
        enableDelirium();
        harness.setHand(player2, List.of());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        resolveTrigger();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void enableDelirium() {
        harness.setLibrary(player2, List.of());
        harness.setGraveyard(player1, List.of(
                new Millstone(), new GrizzlyBears(), new Shock(), new Forest()));
    }

    private void resolveTrigger() {
        advanceToEndStep(player1);
        harness.passBothPriorities();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
