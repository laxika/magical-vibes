package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheSpearOfBashenga.class, GrizzlyBears.class, Plains.class})
class TheSpearOfBashengaTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and makes its controller the monarch when there is no monarch")
    void entersAndMakesControllerMonarchWhenThereIsNoMonarch() {
        harness.enterBattlefieldAndReturn(player1, new TheSpearOfBashenga());

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
        validTarget.setTapped(true);
        Permanent untappedTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent tappedLand = harness.addToBattlefieldAndReturn(player2, new Plains());
        tappedLand.setTapped(true);
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
        target.setTapped(true);
        gd.monarchPlayerId = player1.getId();

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }
}
