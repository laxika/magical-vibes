package com.github.laxika.magicalvibes.cards.f;

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

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FalconsWingHarness.class, GrizzlyBears.class, Shock.class})
class FalconsWingHarnessTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Falcon's Wing Harness attaches it to a targeted creature and grants its abilities")
    void enteringAttachesAndGrantsAbilities() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FalconsWingHarness()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.getGameService().playCard(gd, player1, 0, 0, bears.getId(), null);
        harness.passBothPriorities();

        Permanent harnessPermanent = findPermanent(player1, "Falcon's Wing Harness");
        assertThat(harnessPermanent.getAttachedTo()).isNull();

        harness.passBothPriorities();

        assertThat(harnessPermanent.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Equip attaches Falcon's Wing Harness to another creature you control")
    void equipAttachesToAnotherCreature() {
        Permanent harnessPermanent = addHarnessReady();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(harnessPermanent.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Ward {1} counters an opponent's spell when its controller does not pay")
    void wardCountersUnpaidSpell() {
        Permanent harnessPermanent = addHarnessReady();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harnessPermanent.setAttachedTo(bears.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Ward lets an opponent's spell resolve when its controller pays {1}")
    void wardCanBePaid() {
        Permanent harnessPermanent = addHarnessReady();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harnessPermanent.setAttachedTo(bears.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Shock");
    }

    private Permanent addHarnessReady() {
        Permanent permanent = new Permanent(new FalconsWingHarness());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }
}
