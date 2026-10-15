package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoonsnareSpecialist.class, GrizzlyBears.class})
class MoonsnareSpecialistTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns up to one target creature to its owner's hand")
    void etbBouncesTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MoonsnareSpecialist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, 0, target.getId());
        resolveAllTriggers();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Moonsnare Specialist");
    }

    @Test
    @DisplayName("ETB can decline its optional target")
    void etbCanDeclineTarget() {
        harness.setHand(player1, List.of(new MoonsnareSpecialist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Moonsnare Specialist");
        harness.assertNotInHand(player1, "Moonsnare Specialist");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ninjutsu returns the unblocked attacker and puts Moonsnare Specialist in tapped and attacking")
    void ninjutsuSwapsTheUnblockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        MoonsnareSpecialist ninja = new MoonsnareSpecialist();
        harness.setHand(player1, List.of(ninja));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, attacker.getId());

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).contains(ninja);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, harness::passBothPriorities);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, this::resolveAllTriggers);

        Permanent specialist = findPermanent(player1, "Moonsnare Specialist");
        assertThat(specialist.isTapped()).isTrue();
        assertThat(specialist.isAttacking()).isTrue();
        assertThat(specialist.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(attacker.getCard());
    }

    @Test
    @CardUsed(MoonsnareSpecialist.class)
    @DisplayName("Moonsnare Specialist can return itself to hand with its ETB ability")
    void etbCanBounceItself() {
        MoonsnareSpecialist card = new MoonsnareSpecialist();
        Permanent specialist = harness.enterBattlefieldAndReturn(player1, card);

        harness.handlePermanentChosen(player1, specialist.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Moonsnare Specialist");
        assertThat(gd.playerHands.get(player1.getId())).contains(card);
    }

    @Test
    @CardUsed(MoonsnareSpecialist.class)
    @DisplayName("ETB can return another creature you control")
    void etbCanBounceAnotherOwnCreature() {
        MoonsnareSpecialist targetCard = new MoonsnareSpecialist();
        Permanent target = harness.addToBattlefieldAndReturn(player1, targetCard);
        Permanent source = harness.enterBattlefieldAndReturn(player1, new MoonsnareSpecialist());

        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(targetCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source).doesNotContain(target);
    }

    @Test
    @DisplayName("Entering through ninjutsu also triggers the creature bounce")
    void ninjutsuEntryBouncesOpposingCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new MoonsnareSpecialist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, harness::passBothPriorities);
        harness.handlePermanentChosen(player1, target.getId());
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, this::resolveAllTriggers);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        Permanent specialist = findPermanent(player1, "Moonsnare Specialist");
        assertThat(specialist.isTapped()).isTrue();
        assertThat(specialist.isAttacking()).isTrue();
    }
}
