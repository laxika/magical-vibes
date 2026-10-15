package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DisappearingAct.class, GrizzlyBears.class, Island.class})
class DisappearingActTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a permanent you control as an additional cost and counters target spell")
    void returnsControlledPermanentAndCountersSpell() {
        GrizzlyBears spell = new GrizzlyBears();
        Permanent returnedPermanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new DisappearingAct()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstantWithSacrifice(player2, 0, spell.getId(), returnedPermanent.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Disappearing Act");
    }

    @Test
    @DisplayName("Cannot pay the additional cost with an opponent's permanent")
    void cannotReturnOpponentPermanentAsCost() {
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        GrizzlyBears spell = new GrizzlyBears();

        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new DisappearingAct()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(
                player2, 0, spell.getId(), opponentPermanent.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A land is returned immediately as a casting cost before the spell resolves")
    void returnsLandBeforeResolution() {
        GrizzlyBears spell = new GrizzlyBears();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new DisappearingAct()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstantWithSacrifice(player2, 0, spell.getId(), land.getId());

        harness.assertInHand(player2, "Island");
        harness.assertNotOnBattlefield(player2, "Island");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Disappearing Act");
    }

    @Test
    @DisplayName("Cannot cast without choosing a permanent to return")
    void cannotOmitAdditionalCost() {
        GrizzlyBears spell = new GrizzlyBears();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new DisappearingAct()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player2, "Disappearing Act");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A controlled permanent owned by an opponent returns to its owner's hand")
    void returnsBorrowedPermanentToOwner() {
        GrizzlyBears spell = new GrizzlyBears();
        Island land = new Island();
        land.setOwnerId(player1.getId());
        Permanent borrowedLand = harness.addToBattlefieldAndReturn(player2, land);
        gd.stolenCreatures.put(borrowedLand.getId(), player1.getId());
        gd.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(), "Borrowed land", null,
                player2.getId(), new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                borrowedLand.getId(), null, null, EffectDuration.PERMANENT, 0));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new DisappearingAct()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        harness.castInstantWithSacrifice(player2, 0, spell.getId(), borrowedLand.getId());

        harness.assertInHand(player1, "Island");
        harness.assertNotOnBattlefield(player2, "Island");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
