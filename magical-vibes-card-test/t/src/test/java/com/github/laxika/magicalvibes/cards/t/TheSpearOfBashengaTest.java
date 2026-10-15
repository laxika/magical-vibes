package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

@CardUsed({TheSpearOfBashenga.class, GrizzlyBears.class, Plains.class})
class TheSpearOfBashengaTest extends BaseCardTest {

    @Test
    void entryAbilityDoesNotTriggerWhenAMonarchAlreadyExists() {
        gd.monarchPlayerId = player2.getId();

        harness.enterBattlefieldAndReturn(player1, new TheSpearOfBashenga());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.monarchPlayerId).isEqualTo(player2.getId());
    }

    @Test
    void equipAttachesAndMovingItRemovesThePreviousCreaturesBonus() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent spear = harness.addToBattlefieldAndReturn(player1, new TheSpearOfBashenga());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 2, null, first.getId());
        harness.passBothPriorities();
        assertThat(spear.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);

        harness.activateAbility(player1, 2, null, second.getId());
        harness.passBothPriorities();
        assertThat(spear.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, second, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void doesNotBecomeMonarchIfAnotherPlayerBecomesMonarchBeforeEntryTriggerResolves() {
        harness.enterBattlefieldAndReturn(player1, new TheSpearOfBashenga());
        gd.monarchPlayerId = player2.getId();

        harness.passBothPriorities();

        assertThat(gd.monarchPlayerId).isEqualTo(player2.getId());
    }

    @Test
    void attackTriggerDoesNotDestroyATargetThatBecomesUntapped() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent spear = harness.addToBattlefieldAndReturn(player1, new TheSpearOfBashenga());
        spear.setAttachedTo(attacker.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        gd.monarchPlayerId = player2.getId();

        declareAttackers(List.of(0));
        assertThat(attacker.isTapped()).isFalse();
        harness.handlePermanentChosen(player1, target.getId());
        target.untap();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void attackTriggerStillDestroysTheOriginalMonarchsPermanentAfterMonarchChanges() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent spear = harness.addToBattlefieldAndReturn(player1, new TheSpearOfBashenga());
        spear.setAttachedTo(attacker.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        gd.monarchPlayerId = player2.getId();

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        gd.monarchPlayerId = player1.getId();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Enters and makes its controller the monarch when there is no monarch")
    void entersAndMakesControllerMonarchWhenThereIsNoMonarch() {
        harness.enterBattlefieldAndReturn(player1, new TheSpearOfBashenga());
        harness.passBothPriorities();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Does not change the monarch when a monarch already exists")
    void doesNotChangeExistingMonarch() {
        gd.monarchPlayerId = player2.getId();

        harness.enterBattlefieldAndReturn(player1, new TheSpearOfBashenga());

        assertThat(gd.monarchPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Equipped creature gets +2/+2 and vigilance")
    void equippedCreatureGetsBoostAndVigilance() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent spear = harness.addToBattlefieldAndReturn(player1, new TheSpearOfBashenga());
        spear.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Attacking the monarch lets the Equipment controller choose a tapped nonland permanent to destroy")
    void controllerChoosesTappedNonlandPermanent() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent spear = harness.addToBattlefieldAndReturn(player1, new TheSpearOfBashenga());
        spear.setAttachedTo(attacker.getId());

        Permanent validTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        validTarget.tap();
        Permanent untappedTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent tappedLand = harness.addToBattlefieldAndReturn(player2, new Plains());
        tappedLand.tap();
        gd.monarchPlayerId = player2.getId();

        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).contains(validTarget.getId())
                .doesNotContain(untappedTarget.getId(), tappedLand.getId());

        harness.handlePermanentChosen(player1, validTarget.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(validTarget);
    }

    @Test
    @DisplayName("The attack trigger does not fire when attacking a player who is not the monarch")
    void doesNotTriggerAgainstNonMonarch() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent spear = harness.addToBattlefieldAndReturn(player1, new TheSpearOfBashenga());
        spear.setAttachedTo(attacker.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        gd.monarchPlayerId = player1.getId();

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }
}
