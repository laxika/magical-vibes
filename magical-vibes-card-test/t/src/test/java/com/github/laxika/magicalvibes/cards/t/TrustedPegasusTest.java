package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrustedPegasus.class, GrizzlyBears.class, SuntailHawk.class, Unsummon.class})
class TrustedPegasusTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking grants flying to an attacking creature without flying")
    void grantsFlyingToAttackingCreatureWithoutFlying() {
        addCreatureReady(player1, new TrustedPegasus());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Granted flying wears off at end of turn")
    void grantedFlyingWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new TrustedPegasus());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot target an attacking creature that already has flying")
    void cannotTargetCreatureWithFlying() {
        addCreatureReady(player1, new TrustedPegasus());
        Permanent flyer = addCreatureReady(player1, new SuntailHawk());

        declareAttackers(player1, List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, flyer.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttackingCreature() {
        addCreatureReady(player1, new TrustedPegasus());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Attacking alone does not require a target or leave a trigger on the stack")
    void attackingAloneHasNoLegalTarget() {
        addCreatureReady(player1, new TrustedPegasus());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The attack trigger still grants flying after Pegasus leaves the battlefield")
    void grantsFlyingAfterSourceLeavesBattlefield() {
        Permanent pegasus = addCreatureReady(player1, new TrustedPegasus());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.ensurePriority(player1);
        harness.castInstant(player1, 0, pegasus.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Trusted Pegasus");
        harness.assertNotOnBattlefield(player1, "Trusted Pegasus");
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The attack trigger does not resolve after its target leaves the battlefield")
    void targetLeavingBattlefieldMakesTriggerIllegal() {
        addCreatureReady(player1, new TrustedPegasus());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.ensurePriority(player1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Grizzly Bears");
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }
}
