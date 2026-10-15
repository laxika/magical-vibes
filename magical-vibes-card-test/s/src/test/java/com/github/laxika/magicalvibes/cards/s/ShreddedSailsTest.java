package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShreddedSails.class, AirElemental.class, GrizzlyBears.class, Millstone.class})
class ShreddedSailsTest extends BaseCardTest {

    @Test
    void destroysTargetArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        harness.setHand(player1, List.of(new ShreddedSails()));
        addSpellMana();

        harness.castInstant(player1, 0, 0, artifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Millstone");
        harness.assertInGraveyard(player2, "Millstone");
    }

    @Test
    void dealsFourDamageToTargetCreatureWithFlying() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new ShreddedSails()));
        addSpellMana();

        harness.castInstant(player1, 0, 1, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    void rejectsIllegalTargetsForEachMode() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShreddedSails()));
        addSpellMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new ShreddedSails()));
        addSpellMana();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cyclingDiscardsTheCardAndDrawsOne() {
        harness.setHand(player1, List.of(new ShreddedSails()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Shredded Sails");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void canDestroyAnArtifactYouControl() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        harness.setHand(player1, List.of(new ShreddedSails()));
        addSpellMana();

        harness.castInstant(player1, 0, 0, artifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Millstone");
        harness.assertInGraveyard(player1, "Millstone");
        harness.assertInGraveyard(player1, "Shredded Sails");
    }

    @Test
    void flyingCreatureWithFiveToughnessSurvivesFourDamage() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new ShreddedSails()));
        addSpellMana();

        harness.castInstant(player1, 0, 1, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Air Elemental");
        assertThat(creature.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    void doesNotDamageCreatureThatLosesFlyingBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new ShreddedSails()));
        addSpellMana();

        harness.castInstant(player1, 0, 1, creature.getId());
        creature.getRemovedKeywords().add(Keyword.FLYING);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Air Elemental");
        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Shredded Sails");
    }

    @Test
    void cyclingPaysDiscardBeforeDrawingAndAcceptsColoredMana() {
        harness.setHand(player1, List.of(new ShreddedSails()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Shredded Sails");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotCycleWithOnlyOneMana() {
        harness.setHand(player1, List.of(new ShreddedSails()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Shredded Sails");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    private void addSpellMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
