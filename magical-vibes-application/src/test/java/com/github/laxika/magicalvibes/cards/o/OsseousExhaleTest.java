package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DragonWhelp;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    private Permanent attackingCreature() {
        harness.forceActivePlayer(player1);
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
