package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.Regeneration;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpreadingPlague.class, GrizzlyBears.class, HillGiant.class, Ornithopter.class,
        Forest.class, Regeneration.class})
class SpreadingPlagueTest extends BaseCardTest {

    @Test
    @DisplayName("A creature entering destroys other creatures sharing a color")
    void destroysOtherCreaturesOfSharedColor() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new HillGiant());
        addCreatureReady(player2, new Ornithopter());
        harness.addToBattlefield(player1, new SpreadingPlague());

        GrizzlyBears entering = new GrizzlyBears();
        harness.castFromHand(player1, entering, "{1}{G}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == entering);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player2, "Ornithopter");
    }

    @Test
    @DisplayName("A creature entering under either player's control destroys matching creatures")
    void triggersForAnOpponentCreatureEntering() {
        Permanent ownGreenCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentGreenCreature = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new HillGiant());
        harness.addToBattlefield(player1, new SpreadingPlague());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        GrizzlyBears entering = new GrizzlyBears();
        harness.castFromHand(player2, entering, "{1}{G}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent == ownGreenCreature);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent == opponentGreenCreature)
                .anyMatch(permanent -> permanent.getCard() == entering);
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("A colorless creature entering does not destroy colored creatures")
    void colorlessCreatureDoesNotDestroyAnyCreature() {
        Permanent ownGreenCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentGreenCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new SpreadingPlague());

        Ornithopter entering = new Ornithopter();
        harness.castFromHand(player1, entering, "{0}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(ownGreenCreature)
                .anyMatch(permanent -> permanent.getCard() == entering);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentGreenCreature);
    }

    @Test
    @DisplayName("A noncreature permanent entering does not trigger Spreading Plague")
    void noncreaturePermanentDoesNotTrigger() {
        Permanent ownGreenCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentGreenCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new SpreadingPlague());

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownGreenCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentGreenCreature);
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("The destruction cannot be regenerated")
    void matchingCreatureCannotBeRegenerated() {
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());
        Permanent regeneration = harness.addToBattlefieldAndReturn(player2, new Regeneration());
        regeneration.setAttachedTo(victim.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.activateAbility(player2, 1, null, null);
        harness.passBothPriorities();
        assertThat(victim.getRegenerationShield()).isEqualTo(1);

        harness.addToBattlefield(player1, new SpreadingPlague());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        GrizzlyBears entering = new GrizzlyBears();
        harness.castFromHand(player1, entering, "{1}{G}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(permanent -> permanent == victim);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == entering);
    }
}
