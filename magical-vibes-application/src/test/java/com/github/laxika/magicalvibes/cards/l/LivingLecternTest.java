package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.u.UnassumingSage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LivingLectern.class, UnassumingSage.class, Forest.class})
class LivingLecternTest extends BaseCardTest {

    @Test
    void sacrificesDrawsAndCreatesSorcererRoleAttachedToAnotherCreature() {
        harness.addToBattlefield(player1, new LivingLectern());
        Permanent target = addCreatureReady(player1, new UnassumingSage());
        harness.setLibrary(player1, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Living Lectern");
        harness.assertInGraveyard(player1, "Living Lectern");
        harness.passBothPriorities();

        Permanent role = findPermanent(player1, "Sorcerer");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    void canChooseNoTargetAndStillDraws() {
        harness.addToBattlefield(player1, new LivingLectern());
        harness.setLibrary(player1, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(findPermanents(player1, "Sorcerer")).isEmpty();
    }

    @Test
    void cannotTargetOpponentCreatureOrTheLecternItself() {
        Permanent lectern = harness.addToBattlefieldAndReturn(player1, new LivingLectern());
        Permanent opponentCreature = addCreatureReady(player2, new UnassumingSage());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, lectern.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Living Lectern");
    }

    @Test
    void sorcererRoleScriesWhenEnchantedCreatureAttacks() {
        harness.addToBattlefield(player1, new LivingLectern());
        Permanent target = addCreatureReady(player1, new LivingLectern());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    @Test
    void newSorcererRoleReplacesTheOlderRoleControlledByTheSamePlayer() {
        harness.addToBattlefield(player1, new LivingLectern());
        harness.addToBattlefield(player1, new LivingLectern());
        Permanent target = addCreatureReady(player1, new LivingLectern());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        Permanent oldRole = findPermanent(player1, "Sorcerer");
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Sorcerer")).hasSize(1);
        Permanent newRole = findPermanent(player1, "Sorcerer");
        assertThat(newRole.getId()).isNotEqualTo(oldRole.getId());
        assertThat(newRole.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        harness.addToBattlefield(player1, new LivingLectern());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Living Lectern");
    }

    @Test
    void illegalTargetAtResolutionPreventsTheDrawAndRoleCreation() {
        harness.addToBattlefield(player1, new LivingLectern());
        Permanent target = addCreatureReady(player1, new LivingLectern());
        harness.setLibrary(player1, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(findPermanents(player1, "Sorcerer")).isEmpty();
        harness.assertInGraveyard(player1, "Living Lectern");
    }

    @Test
    void cannotActivateWithAnotherAbilityOnTheStack() {
        harness.addToBattlefield(player1, new LivingLectern());
        harness.addToBattlefield(player1, new LivingLectern());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Living Lectern");
        harness.passBothPriorities();
    }

    @Test
    void tappedSummoningSickLecternCanActivateWithoutATarget() {
        Permanent lectern = harness.addToBattlefieldAndReturn(player1, new LivingLectern());
        lectern.setTapped(true);
        lectern.setSummoningSick(true);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Living Lectern");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        assertThat(findPermanents(player1, "Sorcerer")).isEmpty();
    }
}
