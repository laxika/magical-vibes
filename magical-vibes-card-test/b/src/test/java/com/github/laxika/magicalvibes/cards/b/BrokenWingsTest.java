package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.d.DarksteelColossus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IntangibleVirtue;
import com.github.laxika.magicalvibes.cards.l.LiquimetalCoating;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrokenWings.class, AirElemental.class, GrizzlyBears.class, IntangibleVirtue.class,
        LiquimetalCoating.class, DarksteelColossus.class})
class BrokenWingsTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target artifact")
    void destroysArtifact() {
        harness.addToBattlefield(player2, new LiquimetalCoating());
        Permanent target = findPermanent(player2, "Liquimetal Coating");

        castBrokenWings(target);

        harness.assertNotOnBattlefield(player2, "Liquimetal Coating");
        harness.assertInGraveyard(player2, "Liquimetal Coating");
    }

    @Test
    @DisplayName("Destroys a target enchantment")
    void destroysEnchantment() {
        harness.addToBattlefield(player2, new IntangibleVirtue());
        Permanent target = findPermanent(player2, "Intangible Virtue");

        castBrokenWings(target);

        harness.assertNotOnBattlefield(player2, "Intangible Virtue");
        harness.assertInGraveyard(player2, "Intangible Virtue");
    }

    @Test
    @DisplayName("Destroys a target creature with flying")
    void destroysFlyingCreature() {
        harness.addToBattlefield(player2, new AirElemental());
        Permanent target = findPermanent(player2, "Air Elemental");

        castBrokenWings(target);

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetCreatureWithoutFlying() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new AirElemental());
        Permanent target = findPermanent(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new BrokenWings()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact, enchantment, or creature with flying");
    }

    @Test
    @DisplayName("Can destroy a flying creature controlled by the caster")
    void destroysOwnFlyingCreature() {
        harness.addToBattlefield(player1, new AirElemental());

        castBrokenWings(findPermanent(player1, "Air Elemental"));

        harness.assertNotOnBattlefield(player1, "Air Elemental");
        harness.assertInGraveyard(player1, "Air Elemental");
    }

    @Test
    @DisplayName("Can target a nonflying artifact creature but cannot destroy it if indestructible")
    void doesNotDestroyIndestructibleArtifactCreature() {
        harness.addToBattlefield(player2, new DarksteelColossus());

        castBrokenWings(findPermanent(player2, "Darksteel Colossus"));

        harness.assertOnBattlefield(player2, "Darksteel Colossus");
        harness.assertInGraveyard(player1, "Broken Wings");
    }

    private void castBrokenWings(Permanent target) {
        harness.setHand(player1, List.of(new BrokenWings()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
