package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.Bombard;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.o.OrazcaRaptor;
import com.github.laxika.magicalvibes.cards.s.SunCrestedPterodon;
import com.github.laxika.magicalvibes.cards.s.ShakeTheFoundations;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NeedletoothRaptor.class, Bombard.class, SunCrestedPterodon.class,
        ShakeTheFoundations.class, ColossalDreadmaw.class, OrazcaRaptor.class})
class NeedletoothRaptorTest extends BaseCardTest {

    @Test
    void damageTriggersFiveDamageToAnOpponentsCreature() {
        harness.addToBattlefield(player2, new NeedletoothRaptor());
        harness.addToBattlefield(player1, new SunCrestedPterodon());
        harness.setHand(player1, List.of(new Bombard()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID raptorId = harness.getPermanentId(player2, "Needletooth Raptor");
        UUID pterodonId = harness.getPermanentId(player1, "Sun-Crested Pterodon");
        harness.castAndResolveInstant(player1, 0, raptorId);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(pterodonId);
        harness.handlePermanentChosen(player2, pterodonId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sun-Crested Pterodon");
        harness.assertInGraveyard(player2, "Needletooth Raptor");
    }

    @Test
    void damageTriggerCannotTargetYourOwnCreature() {
        harness.addToBattlefield(player2, new NeedletoothRaptor());
        harness.addToBattlefield(player2, new SunCrestedPterodon());
        harness.setHand(player1, List.of(new Bombard()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID raptorId = harness.getPermanentId(player2, "Needletooth Raptor");
        harness.castAndResolveInstant(player1, 0, raptorId);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Needletooth Raptor");
        harness.assertOnBattlefield(player2, "Sun-Crested Pterodon");
    }

    @Test
    void nonlethalDamageTriggersExactlyFiveDamage() {
        harness.addToBattlefield(player2, new NeedletoothRaptor());
        harness.addToBattlefield(player1, new SunCrestedPterodon());
        Permanent dreadmaw = harness.addToBattlefieldAndReturn(player1, new ColossalDreadmaw());
        harness.setLibrary(player1, List.of(new ColossalDreadmaw()));
        harness.setHand(player1, List.of(new ShakeTheFoundations()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(dreadmaw.getId(),
                        harness.getPermanentId(player1, "Sun-Crested Pterodon"));
        harness.handlePermanentChosen(player2, harness.getPermanentId(player1, "Sun-Crested Pterodon"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sun-Crested Pterodon");
        assertThat(findPermanent(player2, "Needletooth Raptor").getMarkedDamage()).isEqualTo(1);
        assertThat(dreadmaw.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void separateDamageEventsTriggerAgainEvenWhenTheSecondKillsTheRaptor() {
        harness.addToBattlefield(player2, new NeedletoothRaptor());
        Permanent dreadmaw = harness.addToBattlefieldAndReturn(player1, new ColossalDreadmaw());
        harness.setLibrary(player1, List.of(new ColossalDreadmaw()));
        harness.setHand(player1, List.of(new ShakeTheFoundations(), new Bombard()));
        harness.addMana(player1, ManaColor.RED, 6);
        UUID raptorId = harness.getPermanentId(player2, "Needletooth Raptor");

        harness.castAndResolveInstant(player1, 0);
        harness.handlePermanentChosen(player2, dreadmaw.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Colossal Dreadmaw");

        Permanent pterodon = harness.addToBattlefieldAndReturn(player1, new SunCrestedPterodon());
        harness.castAndResolveInstant(player1, 0, raptorId);
        harness.handlePermanentChosen(player2, pterodon.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Needletooth Raptor");
        harness.assertInGraveyard(player1, "Sun-Crested Pterodon");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void lethalCombatDamageStillTriggersFiveDamage() {
        Permanent attacker = addCreatureReady(player1, new OrazcaRaptor());
        Permanent raptor = harness.addToBattlefieldAndReturn(player2, new NeedletoothRaptor());
        Permanent dreadmaw = harness.addToBattlefieldAndReturn(player1, new ColossalDreadmaw());
        attacker.setAttacking(true);
        raptor.setBlocking(true);
        raptor.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Needletooth Raptor");
        harness.handlePermanentChosen(player2, dreadmaw.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Colossal Dreadmaw");
        assertThat(dreadmaw.getMarkedDamage()).isEqualTo(5);
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
}
