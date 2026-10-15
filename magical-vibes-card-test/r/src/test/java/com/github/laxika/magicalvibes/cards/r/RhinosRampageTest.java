package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.j.JayemdaeTome;
import com.github.laxika.magicalvibes.cards.l.LoxodonWarhammer;
import com.github.laxika.magicalvibes.cards.m.MirriCatWarrior;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RhinosRampage.class, MirriCatWarrior.class, HillGiant.class, RagingGoblin.class,
        Millstone.class, Ornithopter.class, JayemdaeTome.class, LoxodonWarhammer.class})
class RhinosRampageTest extends BaseCardTest {

    @Test
    void boostsYourCreatureBeforeItFights() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new MirriCatWarrior());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        cast(ownCreature, opposingCreature);

        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    void excessDamageTriggersOptionalArtifactDestruction() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        cast(ownCreature, opposingCreature);

        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Millstone");
    }

    @Test
    void excessDamageTriggerCannotTargetAnArtifactCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        cast(ownCreature, opposingCreature);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, artifactCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exactLethalDamageDoesNotTriggerArtifactDestruction() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new MirriCatWarrior());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addToBattlefield(player2, new Millstone());
        cast(ownCreature, opposingCreature);

        harness.assertInGraveyard(player1, "Mirri, Cat Warrior");
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertOnBattlefield(player2, "Millstone");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void damageAlreadyMarkedCountsTowardExcessDamage() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new MirriCatWarrior());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        opposingCreature.setMarkedDamage(1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        cast(ownCreature, opposingCreature);

        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Millstone");
    }

    @Test
    void canChooseNoArtifactEvenWhenOneIsAvailable() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        harness.addToBattlefield(player2, new Millstone());
        cast(ownCreature, opposingCreature);

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Millstone");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canDestroyAnArtifactYouControl() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        cast(ownCreature, opposingCreature);

        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Millstone");
    }

    @Test
    void cannotDestroyAnArtifactWithManaValueGreaterThanThree() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new JayemdaeTome());
        cast(ownCreature, opposingCreature);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canDestroyAnArtifactWithManaValueExactlyThree() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LoxodonWarhammer());
        cast(ownCreature, opposingCreature);

        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Loxodon Warhammer");
    }

    @Test
    void noFightOrArtifactTriggerWhenYourTargetLeaves() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        harness.addToBattlefield(player2, new Millstone());
        harness.setHand(player1, List.of(new RhinosRampage()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, List.of(ownCreature.getId(), opposingCreature.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(ownCreature);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Raging Goblin");
        harness.assertOnBattlefield(player2, "Millstone");
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(1);
        assertThat(opposingCreature.getMarkedDamage()).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void stillBoostsYourCreatureWhenTheOpposingTargetLeaves() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        harness.setHand(player1, List.of(new RhinosRampage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, List.of(ownCreature.getId(), opposingCreature.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(opposingCreature);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void cast(Permanent ownCreature, Permanent opposingCreature) {
        harness.setHand(player1, List.of(new RhinosRampage()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveSorcery(player1, 0, List.of(ownCreature.getId(), opposingCreature.getId()));
    }
}
