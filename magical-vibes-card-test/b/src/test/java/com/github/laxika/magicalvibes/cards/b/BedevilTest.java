package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.i.IllGottenInheritance;
import com.github.laxika.magicalvibes.cards.u.UnbreakableFormation;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Bedevil.class, Forest.class, GarrukWildspeaker.class, GrizzlyBears.class,
        HowlingMine.class, IllGottenInheritance.class, UnbreakableFormation.class})
class BedevilTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target artifact")
    void destroysArtifact() {
        harness.addToBattlefield(player2, new HowlingMine());
        castAndResolveBedevil(harness.getPermanentId(player2, "Howling Mine"));

        harness.assertNotOnBattlefield(player2, "Howling Mine");
        harness.assertInGraveyard(player2, "Howling Mine");
    }

    @Test
    @DisplayName("Destroys a target creature")
    void destroysCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castAndResolveBedevil(harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroys a target planeswalker")
    void destroysPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        castAndResolveBedevil(planeswalker.getId());

        harness.assertNotOnBattlefield(player2, "Garruk Wildspeaker");
        harness.assertInGraveyard(player2, "Garruk Wildspeaker");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Bedevil()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact, creature, or planeswalker");
    }

    @Test
    @DisplayName("Cannot target a noncreature enchantment")
    void cannotTargetEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new IllGottenInheritance());
        harness.setHand(player1, List.of(new Bedevil()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact, creature, or planeswalker");
    }

    @Test
    @DisplayName("Can destroy a creature its caster controls")
    void destroysOwnCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castAndResolveBedevil(creature.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not destroy an indestructible creature")
    void doesNotDestroyIndestructibleCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new UnbreakableFormation()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castAndResolveInstant(player1, 0);

        castAndResolveBedevil(creature.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Bedevil");
    }

    private void castAndResolveBedevil(UUID targetId) {
        harness.setHand(player1, List.of(new Bedevil()));
        addMana();
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 1);
    }

}
