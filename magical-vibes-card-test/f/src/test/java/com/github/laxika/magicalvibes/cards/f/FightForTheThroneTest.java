package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.k.KamahlPitFighter;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FightForTheThrone.class, GrizzlyBears.class, HillGiant.class, KamahlPitFighter.class,
        LlanowarElves.class, Unsummon.class})
class FightForTheThroneTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a counter on your creature, fights, and makes you the monarch when the opposing creature dies")
    void fightsAndBecomesMonarchWithCommander() {
        addCommanderToBattlefield();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        castAndResolveFightForTheThrone(ownCreature, opposingCreature);
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Does not make you the monarch when you do not control your commander")
    void doesNotBecomeMonarchWithoutCommander() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        castAndResolveFightForTheThrone(ownCreature, opposingCreature);
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        assertThat(gd.monarchPlayerId).isNull();
    }

    @Test
    @DisplayName("Requires a creature you control and a creature an opponent controls")
    void requiresCorrectTargets() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new FightForTheThrone()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(opposingCreature.getId(), ownCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void counterIsAppliedBeforeFightDamage() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndResolveFightForTheThrone(ownCreature, opposingCreature);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(ownCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void opposingCreatureCanDieLaterInTheTurn() {
        addCommanderToBattlefield();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castAndResolveFightForTheThrone(ownCreature, opposingCreature);

        harness.assertInGraveyard(player1, "Llanowar Elves");
        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.monarchPlayerId).isNull();
        harness.activateAbility(player1, 0, null, opposingCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void controllingOnlyAnOpponentsCommanderDoesNotGrantMonarch() {
        Card stolenCommander = new KamahlPitFighter();
        stolenCommander.setOwnerId(player2.getId());
        gd.makeCommander(player2.getId(), stolenCommander);
        harness.addToBattlefield(player1, stolenCommander);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        castAndResolveFightForTheThrone(ownCreature, opposingCreature);

        harness.assertInGraveyard(player2, "Llanowar Elves");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.monarchPlayerId).isNull();
    }

    @Test
    void commanderMustStillBeControlledWhenMonarchTriggerResolves() {
        Permanent commander = addCommanderToBattlefield();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        castAndResolveFightForTheThrone(ownCreature, opposingCreature);
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToOutsideGame(gd, commander));
        harness.passBothPriorities();

        assertThat(gd.monarchPlayerId).isNull();
    }

    @Test
    void acquiringCommanderAfterDeathDoesNotCreateMonarchTrigger() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        castAndResolveFightForTheThrone(ownCreature, opposingCreature);

        assertThat(gd.stack).isEmpty();
        addCommanderToBattlefield();
        assertThat(gd.monarchPlayerId).isNull();
    }

    @Test
    void commanderCanEnterAfterSpellResolutionBeforeOpposingCreatureDies() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castAndResolveFightForTheThrone(ownCreature, opposingCreature);
        addCommanderToBattlefield();
        harness.activateAbility(player1, 0, null, opposingCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void missingOpposingTargetStillAllowsCounterButNoFight() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        castFightForTheThrone(ownCreature, opposingCreature);
        bounceWithUnsummon(opposingCreature);
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(gd.monarchPlayerId).isNull();
    }

    @Test
    void missingOwnTargetStillRegistersOpposingDeathTrigger() {
        addCommanderToBattlefield();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        castFightForTheThrone(ownCreature, opposingCreature);
        bounceWithUnsummon(ownCreature);
        harness.passBothPriorities();

        assertThat(opposingCreature.getMarkedDamage()).isZero();
        harness.activateAbility(player1, 0, null, opposingCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void returnedCreatureIsANewObjectAndDoesNotGrantMonarchOnDeath() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of());
        addCommanderToBattlefield();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castAndResolveFightForTheThrone(ownCreature, opposingCreature);
        bounceWithUnsummon(opposingCreature);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Hill Giant"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.monarchPlayerId).isNull();
    }

    private void bounceWithUnsummon(Permanent creature) {
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());
    }

    private Permanent addCommanderToBattlefield() {
        Card commander = new KamahlPitFighter();
        commander.setOwnerId(player1.getId());
        gd.makeCommander(player1.getId(), commander);
        return harness.addToBattlefieldAndReturn(player1, commander);
    }

    private void castAndResolveFightForTheThrone(Permanent ownCreature, Permanent opposingCreature) {
        harness.setHand(player1, List.of(new FightForTheThrone()));
        addMana();
        harness.castAndResolveInstant(player1, 0, List.of(ownCreature.getId(), opposingCreature.getId()));
    }

    private void castFightForTheThrone(Permanent ownCreature, Permanent opposingCreature) {
        harness.setHand(player1, List.of(new FightForTheThrone()));
        addMana();
        harness.castInstant(player1, 0, List.of(ownCreature.getId(), opposingCreature.getId()));
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
