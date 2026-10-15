package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BuiltToLast;
import com.github.laxika.magicalvibes.cards.c.ConsulateSkygate;
import com.github.laxika.magicalvibes.cards.e.EraOfInnovation;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.cards.r.RiparianTiger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AppetiteForTheUnnatural.class, PropheticPrism.class, EraOfInnovation.class,
        RiparianTiger.class, ConsulateSkygate.class, BuiltToLast.class})
class AppetiteForTheUnnaturalTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys an artifact and gains 2 life")
    void destroysArtifactAndGainsLife() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PropheticPrism());
        harness.setHand(player1, List.of(new AppetiteForTheUnnatural()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, artifact.getId());

        harness.assertNotOnBattlefield(player2, "Prophetic Prism");
        harness.assertInGraveyard(player2, "Prophetic Prism");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Destroys an enchantment and gains 2 life")
    void destroysEnchantmentAndGainsLife() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new EraOfInnovation());
        harness.setHand(player1, List.of(new AppetiteForTheUnnatural()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, enchantment.getId());

        harness.assertNotOnBattlefield(player2, "Era of Innovation");
        harness.assertInGraveyard(player2, "Era of Innovation");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RiparianTiger());
        harness.setHand(player1, List.of(new AppetiteForTheUnnatural()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can destroy its caster's artifact and gain life")
    void destroysOwnArtifactAndGainsLife() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        harness.setHand(player1, List.of(new AppetiteForTheUnnatural()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, artifact.getId());

        harness.assertNotOnBattlefield(player1, "Prophetic Prism");
        harness.assertInGraveyard(player1, "Prophetic Prism");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can destroy an artifact creature")
    void destroysArtifactCreatureAndGainsLife() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ConsulateSkygate());
        harness.setHand(player1, List.of(new AppetiteForTheUnnatural()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player2, "Consulate Skygate");
        harness.assertInGraveyard(player2, "Consulate Skygate");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Gains no life when its only target has left the battlefield")
    void gainsNoLifeWhenTargetIsRemoved() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PropheticPrism());
        harness.setHand(player1, List.of(new AppetiteForTheUnnatural()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, artifact.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, artifact));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Prophetic Prism");
        harness.assertInGraveyard(player1, "Appetite for the Unnatural");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Still gains life when indestructible prevents destruction")
    void gainsLifeWhenTargetIsIndestructible() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ConsulateSkygate());
        harness.setHand(player2, List.of(new BuiltToLast()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.setHand(player1, List.of(new AppetiteForTheUnnatural()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertOnBattlefield(player2, "Consulate Skygate");
        harness.assertNotInGraveyard(player2, "Consulate Skygate");
        harness.assertInGraveyard(player1, "Appetite for the Unnatural");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }
}
