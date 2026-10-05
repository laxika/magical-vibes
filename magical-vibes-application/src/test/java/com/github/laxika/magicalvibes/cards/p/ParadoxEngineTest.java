package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.n.Negate;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ParadoxEngine.class, FountainOfYouth.class, GrizzlyBears.class, Island.class, Shock.class,
        Negate.class, Ornithopter.class})
class ParadoxEngineTest extends BaseCardTest {

    @Test
    void untapsAllNonlandPermanentsItsControllerControls() {
        Permanent engine = harness.addToBattlefieldAndReturn(player1, new ParadoxEngine());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        engine.tap();
        artifact.tap();
        creature.tap();
        land.tap();
        opponentCreature.tap();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(engine.isTapped()).isFalse();
        assertThat(artifact.isTapped()).isFalse();
        assertThat(creature.isTapped()).isFalse();
        assertThat(land.isTapped()).isTrue();
        assertThat(opponentCreature.isTapped()).isTrue();
    }

    @Test
    void doesNotTriggerForAnOpponentCastingASpell() {
        Permanent engine = harness.addToBattlefieldAndReturn(player1, new ParadoxEngine());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        engine.tap();
        creature.tap();

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(engine.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void zeroCostCreatureSpellTriggersBeforeItResolves() {
        Permanent engine = harness.addToBattlefieldAndReturn(player1, new ParadoxEngine());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        engine.tap();
        creature.tap();
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.castCreature(player1, 0);

        assertThat(engine.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(engine.isTapped()).isFalse();
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    void triggersAgainForEachSpellCastInTheSameTurn() {
        Permanent engine = harness.addToBattlefieldAndReturn(player1, new ParadoxEngine());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        engine.tap();
        creature.tap();
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(engine.isTapped()).isFalse();
        assertThat(creature.isTapped()).isFalse();
        harness.passBothPriorities();

        engine.tap();
        creature.tap();
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(engine.isTapped()).isFalse();
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void stillUntapsWhenTheTriggeringSpellIsCountered() {
        Permanent engine = harness.addToBattlefieldAndReturn(player1, new ParadoxEngine());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        engine.tap();
        creature.tap();
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.setHand(player2, List.of(new Negate()));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, shock.getId());
        harness.passBothPriorities();

        assertThat(engine.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Shock");
        harness.assertLife(player2, 20);

        harness.passBothPriorities();
        assertThat(engine.isTapped()).isFalse();
        assertThat(creature.isTapped()).isFalse();
        harness.assertLife(player2, 20);
    }

    @Test
    void playingALandDoesNotTrigger() {
        Permanent engine = harness.addToBattlefieldAndReturn(player1, new ParadoxEngine());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        engine.tap();
        creature.tap();
        harness.setHand(player1, List.of(new Island()));

        harness.playLand(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(engine.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Island");
    }

    @Test
    void activatingAnAbilityDoesNotTrigger() {
        Permanent engine = harness.addToBattlefieldAndReturn(player1, new ParadoxEngine());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        engine.tap();
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(engine.isTapped()).isTrue();
        assertThat(artifact.isTapped()).isTrue();
        harness.assertLife(player1, 21);
    }

    @Test
    void castingParadoxEngineDoesNotTriggerItsOwnAbility() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        creature.tap();
        harness.setHand(player1, List.of(new ParadoxEngine()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Paradox Engine");
        assertThat(creature.isTapped()).isTrue();
    }
}
