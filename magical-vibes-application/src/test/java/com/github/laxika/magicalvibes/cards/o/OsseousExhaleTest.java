package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DragonWhelp;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OsseousExhale.class, DragonWhelp.class, GrizzlyBears.class})
class OsseousExhaleTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage and gains 2 life when a Dragon is beheld from the battlefield")
    void beheldDragonPermanentGivesLifeGain() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new DragonWhelp());
        attackingCreature();
        harness.setHand(player1, List.of(new OsseousExhale()));
        castWithBehold(List.of(dragon.getId()), List.of());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Gains 2 life when a Dragon is beheld from hand")
    void beheldDragonCardGivesLifeGain() {
        attackingCreature();
        DragonWhelp dragon = new DragonWhelp();
        harness.setHand(player1, List.of(new OsseousExhale(), dragon));
        castWithBehold(List.of(), List.of(1));

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Deals damage without gaining life when behold is declined")
    void declinedBeholdOmitsLifeGain() {
        attackingCreature();
        harness.setHand(player1, List.of(new OsseousExhale()));
        castWithBehold(List.of(), List.of());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetIdleCreature() {
        harness.forceActivePlayer(player1);
        Permanent idle = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OsseousExhale()));
        addMana();
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, idle.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an attacking or blocking creature");
    }

    @Test
    void dealsExactlyFiveDamageToSurvivingCreature() {
        Permanent attacker = attackingCreature();
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        harness.setHand(player1, List.of(new OsseousExhale()));
        castWithBehold(List.of(), List.of());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(attacker.getMarkedDamage()).isEqualTo(5);
        harness.assertLife(player1, 20);
    }

    @Test
    void canTargetBlockingCreature() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        harness.setHand(player1, List.of(new OsseousExhale()));
        addMana();
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.castAndResolveInstant(player1, 0, blocker.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 20);
    }

    @Test
    void illegalTargetPreventsDamageAndLifeGain() {
        Permanent attacker = attackingCreature();
        harness.setHand(player1, List.of(new OsseousExhale(), new DragonWhelp()));
        addMana();
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.castInstantWithBehold(player1, 0, attacker.getId(), List.of(), List.of(1));
        attacker.setAttacking(false);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Osseous Exhale");
        harness.assertInHand(player1, "Dragon Whelp");
    }

    @Test
    void lifeGainDoesNotRequireBeheldDragonToRemainOnBattlefield() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new DragonWhelp());
        Permanent attacker = attackingCreature();
        harness.setHand(player1, List.of(new OsseousExhale()));
        addMana();
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.castInstantWithBehold(player1, 0, attacker.getId(), List.of(dragon.getId()), List.of());
        gd.playerBattlefields.get(player1.getId()).remove(dragon);
        gd.playerGraveyards.get(player1.getId()).add(dragon.getCard());

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 22);
    }

    @Test
    void cannotBeholdOpponentsDragon() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player2, new DragonWhelp());
        Permanent attacker = attackingCreature();
        harness.setHand(player1, List.of(new OsseousExhale()));
        addMana();
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.castInstantWithBehold(player1, 0, attacker.getId(),
                List.of(dragon.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must behold a permanent you control");
    }

    @Test
    void mayDeclineBeholdEvenWhenDragonIsAvailable() {
        harness.addToBattlefield(player1, new DragonWhelp());
        attackingCreature();
        harness.setHand(player1, List.of(new OsseousExhale(), new DragonWhelp()));

        castWithBehold(List.of(), List.of());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Dragon Whelp");
        harness.assertInHand(player1, "Dragon Whelp");
    }

    private Permanent attackingCreature() {
        harness.forceActivePlayer(player2);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        return attacker;
    }

    private void castWithBehold(List<UUID> beholdPermanentIds, List<Integer> beholdHandCardIndices) {
        addMana();
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        Permanent attacker = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.isAttacking())
                .findFirst()
                .orElseThrow();
        harness.castInstantWithBehold(player1, 0, attacker.getId(), beholdPermanentIds, beholdHandCardIndices);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
