package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.t.TorbranThaneOfRedFell;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DestructiveRevelry.class, Millstone.class, AngelicChorus.class,
        DarksteelIngot.class, GrizzlyBears.class, TorbranThaneOfRedFell.class})
class DestructiveRevelryTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys an artifact and deals 2 damage to its controller")
    void destroysArtifactAndDamagesController() {
        harness.addToBattlefield(player2, new Millstone());
        harness.setHand(player1, List.of(new DestructiveRevelry()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Millstone");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Millstone");
        harness.assertInGraveyard(player2, "Millstone");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Destroys an enchantment and deals 2 damage to its controller")
    void destroysEnchantmentAndDamagesController() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new DestructiveRevelry()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Deals damage even when the targeted artifact is indestructible")
    void damagesControllerWhenArtifactCannotBeDestroyed() {
        harness.addToBattlefield(player2, new DarksteelIngot());
        harness.setHand(player1, List.of(new DestructiveRevelry()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Darksteel Ingot");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertOnBattlefield(player2, "Darksteel Ingot");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Cannot target a non-artifact, non-enchantment permanent")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DestructiveRevelry()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damagesCasterWhenDestroyingTheirOwnArtifact() {
        harness.addToBattlefield(player1, new Millstone());
        harness.setHand(player1, List.of(new DestructiveRevelry()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Millstone"));

        harness.assertInGraveyard(player1, "Millstone");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    void doesNotDealDamageWhenTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player2, new Millstone());
        harness.setHand(player1, List.of(new DestructiveRevelry()));
        harness.setHand(player2, List.of(new DestructiveRevelry()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        UUID targetId = harness.getPermanentId(player2, "Millstone");

        harness.castInstant(player1, 0, targetId);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.assertLife(player2, 18);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Millstone");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Destructive Revelry");
    }

    @Test
    void damageRemainsFromCastersRedSourceForTorbran() {
        harness.addToBattlefield(player1, new TorbranThaneOfRedFell());
        harness.addToBattlefield(player2, new Millstone());
        harness.setHand(player1, List.of(new DestructiveRevelry()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Millstone"));

        harness.assertInGraveyard(player2, "Millstone");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
    }
}
