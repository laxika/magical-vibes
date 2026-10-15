package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ChromaticStar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.Reminisce;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThranVigil.class, ChromaticStar.class, GrizzlyBears.class, Reminisce.class, Shock.class})
class ThranVigilTest extends BaseCardTest {

    @Test
    void putsOneCounterWhenArtifactAndCreatureCardsLeaveTogether() {
        Permanent target = addSetup();
        harness.setGraveyard(player1, List.of(new ChromaticStar(), new GrizzlyBears(), new Shock()));
        castReminisce(player1, player1.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(target.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerForNonArtifactNonCreatureCards() {
        Permanent target = addSetup();
        harness.setGraveyard(player1, List.of(new Shock()));
        castReminisce(player1, player1.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerDuringOpponentTurn() {
        Permanent target = addSetup();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setGraveyard(player1, List.of(new ChromaticStar()));
        harness.setHand(player2, List.of(new Reminisce()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player2, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void triggersForAnArtifactCardAlone() {
        Permanent target = addSetup();
        harness.setGraveyard(player1, List.of(new ChromaticStar()));
        castReminisce(player1, player1.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void triggersForACreatureCardAlone() {
        Permanent target = addSetup();
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castReminisce(player1, player1.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerWhenCardsLeaveOpponentsGraveyardDuringYourTurn() {
        Permanent target = addSetup();
        harness.setGraveyard(player2, List.of(new ChromaticStar(), new GrizzlyBears()));
        castReminisce(player1, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void triggersAgainForASeparateGraveyardDepartureInTheSameTurn() {
        Permanent target = addSetup();
        harness.setGraveyard(player1, List.of(new ChromaticStar()));
        castReminisce(player1, player1.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castReminisce(player1, player1.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doesNotPutACounterOnATargetDestroyedInResponse() {
        Permanent target = addSetup();
        harness.setGraveyard(player1, List.of(new ChromaticStar()));
        castReminisce(player1, player1.getId());
        harness.handlePermanentChosen(player1, target.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addSetup() {
        harness.addToBattlefield(player1, new ThranVigil());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return target;
    }

    private void castReminisce(Player caster, UUID targetPlayerId) {
        harness.setHand(caster, List.of(new Reminisce()));
        harness.addMana(caster, ManaColor.BLUE, 3);
        harness.castAndResolveSorcery(caster, 0, targetPlayerId);
    }
}
