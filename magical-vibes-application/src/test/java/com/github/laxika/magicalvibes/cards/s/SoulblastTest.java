package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Soulblast.class, GrizzlyBears.class, RagingGoblin.class})
class SoulblastTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Soulblast sacrifices all your creatures as an additional cost")
    void castingSacrificesAllYourCreaturesAsAdditionalCost() {
        harness.setHand(player1, List.of(new Soulblast()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new RagingGoblin());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.castInstant(player1, 0, player2.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Raging Goblin");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Raging Goblin");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Soulblast deals damage equal to sacrificed total power")
    void dealsDamageEqualToSacrificedTotalPower() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Soulblast()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new RagingGoblin());

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Soulblast can be cast with no creatures and deals 0 damage")
    void canCastWithNoCreatures() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Soulblast()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Soulblast ignores opponent creature power when calculating damage")
    void ignoresOpponentCreaturePower() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Soulblast()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.addToBattlefield(player1, new RagingGoblin());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 19);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Soulblast can deal its damage to an opposing creature")
    void dealsDamageToCreatureTarget() {
        var target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Soulblast()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.addToBattlefield(player1, new RagingGoblin());

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A creature sacrificed as an additional cost becomes an illegal target")
    void sacrificedTargetMakesSoulblastFizzle() {
        var target = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        harness.setHand(player1, List.of(new Soulblast()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Raging Goblin");
    }

    @Test
    @DisplayName("If mana payment fails, Soulblast does not sacrifice creatures")
    void manaPaymentFailureDoesNotSacrificeCreatures() {
        harness.setHand(player1, List.of(new Soulblast()));
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }
}

