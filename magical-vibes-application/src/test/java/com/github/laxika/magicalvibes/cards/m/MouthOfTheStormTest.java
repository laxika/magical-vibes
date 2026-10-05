package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MouthOfTheStorm.class, GrizzlyBears.class, Shock.class, Unsummon.class, ProdigalPyromancer.class})
class MouthOfTheStormTest extends BaseCardTest {

    @Test
    @DisplayName("ETB weakens existing opposing creatures until your next turn")
    void etbWeakensOpposingCreaturesUntilNextTurn() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        castMouthOfTheStorm();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);

        Permanent lateOpposingCreature = addCreatureReady(player2, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, lateOpposingCreature)).isEqualTo(2);

        gd.expireFloatingEffectsAtTurnStart(player2.getId());
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(-1);

        gd.expireFloatingEffectsAtTurnStart(player1.getId());
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ward counters an opponent's spell when they do not pay")
    void wardCountersUnpaidSpell() {
        Permanent mouth = addCreatureReady(player1, new MouthOfTheStorm());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, mouth.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
    }

    private void castMouthOfTheStorm() {
        harness.castFromHand(player1, new MouthOfTheStorm(), "{6}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    void payingWardAllowsOpposingSpellToResolve() {
        Permanent mouth = addCreatureReady(player1, new MouthOfTheStorm());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, mouth.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Mouth of the Storm");
        assertThat(gd.playerHands.get(player1.getId())).contains(mouth.getCard());
    }

    @Test
    void decliningAffordableWardCountersSpell() {
        Permanent mouth = addCreatureReady(player1, new MouthOfTheStorm());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, mouth.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Mouth of the Storm");
        harness.assertInGraveyard(player2, "Unsummon");
    }

    @Test
    void wardCountersOpposingActivatedAbility() {
        Permanent mouth = addCreatureReady(player1, new MouthOfTheStorm());
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());

        harness.activateAbility(player2, 0, null, mouth.getId());
        resolveAllTriggers();

        assertThat(mouth.getMarkedDamage()).isZero();
        assertThat(pyromancer.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void ownSpellDoesNotTriggerWardAndEntryTriggerSurvivesSourceLeaving() {
        Permanent opposingCreature = addCreatureReady(player2, new MouthOfTheStorm());
        Permanent mouth = harness.enterBattlefieldAndReturn(player1, new MouthOfTheStorm());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, mouth.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Mouth of the Storm");
        assertThat(gd.playerHands.get(player1.getId())).contains(mouth.getCard());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(6);
    }

    @Test
    void entryEffectSnapshotsCreaturesWhenTriggerResolves() {
        harness.enterBattlefieldAndReturn(player1, new MouthOfTheStorm());
        Permanent beforeResolution = addCreatureReady(player2, new MouthOfTheStorm());

        resolveAllTriggers();

        Permanent afterResolution = addCreatureReady(player2, new MouthOfTheStorm());
        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, afterResolution)).isEqualTo(6);
    }

}
