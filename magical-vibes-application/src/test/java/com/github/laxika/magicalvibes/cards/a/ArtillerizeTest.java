package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GlistenerElf;
import com.github.laxika.magicalvibes.cards.k.KarnLiberated;
import com.github.laxika.magicalvibes.cards.p.PristineTalisman;
import com.github.laxika.magicalvibes.cards.s.SpinedThopter;
import com.github.laxika.magicalvibes.cards.x.Xenograft;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Artillerize.class, GlistenerElf.class, PristineTalisman.class, Xenograft.class,
        KarnLiberated.class, SpinedThopter.class})
class ArtillerizeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with artifact sacrifice puts spell on stack targeting creature")
    void castWithArtifactSacrificeTargetingCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PristineTalisman());

        Permanent targetCreature = harness.addToBattlefieldAndReturn(player2, new GlistenerElf());

        harness.setHand(player1, List.of(new Artillerize()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstantWithSacrifice(player1, 0, targetCreature.getId(), artifact.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        harness.assertNotOnBattlefield(player1, "Pristine Talisman");
        harness.assertInGraveyard(player1, "Pristine Talisman");
    }

    @Test
    @DisplayName("Resolving deals 5 damage to target creature")
    void resolvingDeals5DamageToCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PristineTalisman());

        Permanent targetCreature = harness.addToBattlefieldAndReturn(player2, new GlistenerElf());

        harness.setHand(player1, List.of(new Artillerize()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstantWithSacrifice(player1, 0, targetCreature.getId(), artifact.getId());
        harness.passBothPriorities();

        // GlistenerElf is 1/1, so 5 damage kills it
        harness.assertNotOnBattlefield(player2, "Glistener Elf");
        harness.assertInGraveyard(player2, "Glistener Elf");
    }

    @Test
    @DisplayName("Resolving deals 5 damage to target player")
    void resolvingDeals5DamageToPlayer() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PristineTalisman());

        harness.setHand(player1, List.of(new Artillerize()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstantWithSacrifice(player1, 0, player2.getId(), artifact.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Can sacrifice a creature instead of an artifact")
    void canSacrificeCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GlistenerElf());

        harness.setHand(player1, List.of(new Artillerize()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstantWithSacrifice(player1, 0, player2.getId(), creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Glistener Elf");
        harness.assertInGraveyard(player1, "Glistener Elf");
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Cannot cast without an artifact or creature to sacrifice")
    void cannotCastWithoutSacrifice() {
        harness.setHand(player1, List.of(new Artillerize()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, player2.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Cannot sacrifice a non-artifact non-creature permanent")
    void cannotSacrificeNonArtifactNonCreature() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new Xenograft());

        harness.setHand(player1, List.of(new Artillerize()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, player2.getId(), enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or creature");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's permanent")
    void cannotSacrificeOpponentsPermanent() {
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new PristineTalisman());

        harness.setHand(player1, List.of(new Artillerize()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, player2.getId(), opponentArtifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    @Test
    @DisplayName("Spell goes to graveyard after resolution")
    void spellGoesToGraveyardAfterResolution() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PristineTalisman());

        harness.setHand(player1, List.of(new Artillerize()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstantWithSacrifice(player1, 0, player2.getId(), artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Artillerize");
    }

    @Test
    @DisplayName("Deals exactly 5 damage to a planeswalker")
    void deals5DamageToPlaneswalker() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PristineTalisman());
        Permanent karn = harness.addToBattlefieldAndReturn(player2, new KarnLiberated());
        karn.setCounterCount(CounterType.LOYALTY, 6);
        harness.setHand(player1, List.of(new Artillerize()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstantWithSacrifice(player1, 0, karn.getId(), artifact.getId());
        harness.passBothPriorities();

        assertThat(karn.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Karn Liberated");
        harness.assertInGraveyard(player1, "Pristine Talisman");
    }

    @Test
    @DisplayName("An artifact creature satisfies the sacrifice cost by itself")
    void canSacrificeArtifactCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SpinedThopter());
        harness.setHand(player1, List.of(new Artillerize()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstantWithSacrifice(player1, 0, player2.getId(), creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spined Thopter");
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Can target the creature sacrificed as the cost, then fails to resolve")
    void canTargetSacrificedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GlistenerElf());
        harness.setHand(player1, List.of(new Artillerize()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstantWithSacrifice(player1, 0, creature.getId(), creature.getId());

        assertThat(gd.stack).hasSize(1);
        harness.assertInGraveyard(player1, "Glistener Elf");

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Artillerize");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can target its controller")
    void canTargetController() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PristineTalisman());
        harness.setHand(player1, List.of(new Artillerize()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstantWithSacrifice(player1, 0, player1.getId(), artifact.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A noncreature artifact is not a legal damage target")
    void cannotTargetNoncreatureArtifact() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GlistenerElf());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PristineTalisman());
        harness.setHand(player1, List.of(new Artillerize()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(
                player1, 0, target.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Glistener Elf");
        harness.assertOnBattlefield(player2, "Pristine Talisman");
    }
}
