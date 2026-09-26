package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarshlandHordemaster.class, GrizzlyBears.class, Shock.class})
class MarshlandHordemasterTest extends BaseCardTest {

    @Test
    @DisplayName("A Lizard entering perpetually gives Marshland Hordemaster battle cry")
    void lizardEntryPerpetuallyGrantsBattleCry() {
        Permanent hordemaster = harness.enterBattlefieldAndReturn(player1, new MarshlandHordemaster());
        harness.passBothPriorities();
        hordemaster.setSummoningSick(false);

        assertThat(gqs.hasKeyword(gd, hordemaster, Keyword.BATTLE_CRY)).isTrue();

        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, hordemaster)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("When Marshland Hordemaster or a Lizard dies, target opponent loses 1 life and you gain 1 life")
    void lizardDeathDrainsTargetOpponent() {
        harness.addToBattlefield(player1, new MarshlandHordemaster());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID hordemasterId = harness.getPermanentId(player1, "Marshland Hordemaster");
        harness.castInstant(player2, 0, hordemasterId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("A non-Lizard creature dying does not trigger Marshland Hordemaster")
    void nonLizardDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new MarshlandHordemaster());
        harness.addToBattlefield(player1, new GrizzlyBears());
        int opponentLife = gd.getLife(player2.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife);
    }
}
