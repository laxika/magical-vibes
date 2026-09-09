package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Alms.class, Plains.class, Forest.class, GrizzlyBears.class, Shock.class})
class AlmsTest extends BaseCardTest {

    private void addAlmsReady() {
        harness.addToBattlefield(player1, new Alms());
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    @Test
    @DisplayName("Shields the target creature for 1 and exiles the top card of the graveyard")
    void shieldsTargetAndExilesTopGraveyardCard() {
        addAlmsReady();
        harness.setGraveyard(player1, List.of(new Plains(), new Forest()));
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getDamagePreventionShield()).isEqualTo(1);

        // The most recently added card (the last of the list) is the top of the graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Plains");
        assertThat(gd.exiledCards)
                .extracting(e -> e.card().getName())
                .contains("Forest");
    }

    @Test
    @DisplayName("Prevents only the next 1 damage dealt to the target creature")
    void preventsOnlyOneDamageToTargetCreature() {
        addAlmsReady();
        harness.setGraveyard(player1, List.of(new Plains()));
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        assertThat(bears.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("Prevention shield expires at the end of the turn")
    void preventionShieldExpiresAtEndOfTurn() {
        addAlmsReady();
        harness.setGraveyard(player1, List.of(new Plains()));
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("Cannot be activated with an empty graveyard")
    void requiresNonEmptyGraveyard() {
        addAlmsReady();
        harness.setGraveyard(player1, List.of());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        addAlmsReady();
        harness.setGraveyard(player1, List.of(new Plains()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
