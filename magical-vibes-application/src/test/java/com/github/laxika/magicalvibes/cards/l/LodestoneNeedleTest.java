package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GuidestoneCompass;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LodestoneNeedle.class, GuidestoneCompass.class, Forest.class,
        GrizzlyBears.class, Millstone.class})
class LodestoneNeedleTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by tapping a creature and putting two stun counters on it")
    void entersAndStunsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LodestoneNeedle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.STUN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target a noncreature artifact with its enter-the-battlefield ability")
    void entersAndStunsArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        harness.setHand(player1, List.of(new LodestoneNeedle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0, artifact.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
        assertThat(artifact.getCounterCount(CounterType.STUN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Craft returns it transformed and exiles another artifact")
    void craftsIntoGuidestoneCompass() {
        Permanent needle = harness.addToBattlefieldAndReturn(player1, new LodestoneNeedle());
        Millstone material = new Millstone();
        Permanent battlefieldMaterial = harness.addToBattlefieldAndReturn(player1, material);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(needle, battlefieldMaterial);
        assertThat(gd.findExiledCard(material.getId())).isNotNull();

        harness.passBothPriorities();

        Permanent compass = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof GuidestoneCompass)
                .findFirst().orElseThrow();
        assertThat(compass.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Guidestone Compass lets a controlled creature explore")
    void compassExploresControlledCreature() {
        Permanent compass = harness.addToBattlefieldAndReturn(player1, new GuidestoneCompass());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(compass.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}
