package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.e.EyeForAnEye;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({ScreamingNemesis.class, GrizzlyBears.class, AngelOfMercy.class, Shock.class, EyeForAnEye.class})
class ScreamingNemesisTest extends BaseCardTest {

    @Test
    @DisplayName("Reflects damage to another target creature")
    void reflectsDamageToAnotherCreature() {
        harness.addToBattlefield(player2, new ScreamingNemesis());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID nemesisId = harness.getPermanentId(player2, "Screaming Nemesis");
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, nemesisId);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(bearsId).doesNotContain(nemesisId);

        harness.handlePermanentChosen(player2, bearsId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Locks life gain for a player actually dealt reflected damage")
    void locksLifeGainForDamagedPlayer() {
        harness.addToBattlefield(player2, new ScreamingNemesis());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID nemesisId = harness.getPermanentId(player2, "Screaming Nemesis");
        harness.castAndResolveInstant(player1, 0, nemesisId);

        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 18);

        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new AngelOfMercy(), "{4}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Does not lock life gain when the reflected damage is prevented")
    void preventedReflectedDamageDoesNotLockLifeGain() {
        harness.addToBattlefield(player2, new ScreamingNemesis());
        gd.playersWithAllDamagePrevented.add(player1.getId());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID nemesisId = harness.getPermanentId(player2, "Screaming Nemesis");
        harness.castAndResolveInstant(player1, 0, nemesisId);

        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gqs.canPlayerGainLife(gd, player1.getId())).isTrue();
    }

    @Test
    void combatDamageExcludesNemesisFromTargetsAndReflectsActualDamage() {
        Permanent nemesis = harness.addToBattlefieldAndReturn(player2, new ScreamingNemesis());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        nemesis.setBlocking(true);
        nemesis.addBlockingTarget(0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(player1.getId(), player2.getId()).doesNotContain(nemesis.getId());
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gqs.canPlayerGainLife(gd, player1.getId())).isFalse();
    }

    @Test
    void lethalDamageStillReflectsAndLifeGainLockSurvivesSourceDeath() {
        Permanent nemesis = harness.addToBattlefieldAndReturn(player2, new ScreamingNemesis());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, nemesis.getId());
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.castAndResolveInstant(player1, 0, nemesis.getId());
        harness.assertInGraveyard(player2, "Screaming Nemesis");
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 16);

        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new AngelOfMercy(), "{4}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player1, 16);
    }

    @Test
    void canReflectToItsOwnControllerWithoutLockingTheOtherPlayer() {
        Permanent nemesis = harness.addToBattlefieldAndReturn(player2, new ScreamingNemesis());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, nemesis.getId());
        harness.handlePermanentChosen(player2, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gqs.canPlayerGainLife(gd, player2.getId())).isFalse();
        assertThat(gqs.canPlayerGainLife(gd, player1.getId())).isTrue();
    }

    @Test
    void eyeForAnEyeDamageDoesNotApplyNemesisLifeGainLockToItsController() {
        Permanent nemesis = harness.addToBattlefieldAndReturn(player2, new ScreamingNemesis());
        harness.castFromHand(player1, new EyeForAnEye(), "{W}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, nemesis.getId());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, nemesis.getId());
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(gqs.canPlayerGainLife(gd, player1.getId())).isFalse();
        assertThat(gqs.canPlayerGainLife(gd, player2.getId())).isTrue();
    }
}
