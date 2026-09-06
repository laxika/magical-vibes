package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DispersingOrb.class, Forest.class, GrizzlyBears.class})
class DispersingOrbTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a permanent returns the target permanent to its owner's hand")
    void sacrificesPermanentAndReturnsTarget() {
        addOrb();
        Permanent sacrificeTarget = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent target = findPermanent(player2, "Grizzly Bears");
        addAbilityMana();

        activate(target);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, sacrificeTarget.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Dispersing Orb");
    }

    @Test
    @DisplayName("The ability can target a land")
    void canTargetLand() {
        addOrb();
        Permanent sacrificeTarget = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        addAbilityMana();

        activate(target);
        harness.handlePermanentChosen(player1, sacrificeTarget.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInHand(player2, "Forest");
    }

    @Test
    @DisplayName("The Orb itself may be sacrificed as the cost")
    void maySacrificeItself() {
        Permanent orb = addOrb();
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent target = findPermanent(player2, "Grizzly Bears");
        addAbilityMana();

        activate(target);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dispersing Orb");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    private Permanent addOrb() {
        return harness.addToBattlefieldAndReturn(player1, new DispersingOrb());
    }

    private void addAbilityMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void activate(Permanent target) {
        harness.activateAbility(player1, 0, 0, null, target.getId());
    }
}
