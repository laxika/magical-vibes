package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WarriorEnKor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IcewindStalwart.class, GrizzlyBears.class, WarriorEnKor.class})
class IcewindStalwartTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles and immediately returns a non-Warrior creature you control")
    void flickersTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        UUID originalId = bears.getId();
        castStalwart(originalId);

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(originalId);
        assertThat(returned.isSummoningSick()).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("ETB can resolve without a target")
    void canChooseNoTarget() {
        harness.castFromHand(player1, new IcewindStalwart(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Icewind Stalwart");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a Warrior")
    void cannotTargetWarrior() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new WarriorEnKor());
        harness.setHand(player1, List.of(new IcewindStalwart()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, warrior.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Warrior creature you control");
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IcewindStalwart()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Warrior creature you control");
    }

    @Test
    @DisplayName("A borrowed creature returns under its owner's control")
    void returnsBorrowedCreatureToOwner() {
        GrizzlyBears card = new GrizzlyBears();
        card.setOwnerId(player2.getId());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, card);
        gainControl(player1, bears);

        castStalwart(bears.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanent(player2, "Grizzly Bears").getId()).isNotEqualTo(bears.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Returning creates an untapped creature with no marked damage")
    void clearsPermanentStateOnReturn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.tap();
        bears.setMarkedDamage(1);

        castStalwart(bears.getId());

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.getMarkedDamage()).isZero();
        assertThat(returned.getId()).isNotEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Choosing no target leaves an eligible creature untouched")
    void canDeclineWithEligibleCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.tap();

        harness.castFromHand(player1, new IcewindStalwart(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Icewind Stalwart");
        assertThat(findPermanent(player1, "Grizzly Bears").getId()).isEqualTo(bears.getId());
        assertThat(bears.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A target no longer controlled by you is not flickered")
    void doesNotFlickerTargetAfterControlChanges() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new IcewindStalwart()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0, bears.getId());
        harness.passBothPriorities();

        gainControl(player2, bears);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanent(player2, "Grizzly Bears").getId()).isEqualTo(bears.getId());
        assertThat(gd.stack).isEmpty();
    }

    private void gainControl(Player newController, Permanent target) {
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(CreatureControlService.class)
                .applyControlEffect(gd, newController.getId(), target,
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT), EffectDuration.PERMANENT,
                        null, "Test setup"));
    }

    private void castStalwart(UUID targetId) {
        harness.setHand(player1, List.of(new IcewindStalwart()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
