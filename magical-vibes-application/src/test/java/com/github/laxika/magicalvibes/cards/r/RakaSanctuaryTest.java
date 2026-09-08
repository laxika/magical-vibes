package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PhantomWarrior;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RakaSanctuary.class, GrizzlyBears.class, PhantomWarrior.class, SavannahLions.class})
class RakaSanctuaryTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to a target creature with only a white permanent")
    void dealsOneDamageWithWhitePermanentOnly() {
        harness.addToBattlefield(player1, new RakaSanctuary());
        harness.addToBattlefield(player1, new SavannahLions());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals 1 damage to a target creature with only a blue permanent")
    void dealsOneDamageWithBluePermanentOnly() {
        harness.addToBattlefield(player1, new RakaSanctuary());
        harness.addToBattlefield(player1, new PhantomWarrior());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals 3 damage to a target creature with both a white and blue permanent")
    void dealsThreeDamageWithWhiteAndBluePermanents() {
        harness.addToBattlefield(player1, new RakaSanctuary());
        harness.addToBattlefield(player1, new SavannahLions());
        harness.addToBattlefield(player1, new PhantomWarrior());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not trigger without a white or blue permanent")
    void doesNotTriggerWithoutWhiteOrBluePermanent() {
        harness.addToBattlefield(player1, new RakaSanctuary());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }
}
