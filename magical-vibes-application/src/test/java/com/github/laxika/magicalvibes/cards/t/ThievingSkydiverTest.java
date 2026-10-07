package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GildedLotus;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThievingSkydiver.class, FountainOfYouth.class, GildedLotus.class, LeoninScimitar.class})
class ThievingSkydiverTest extends BaseCardTest {

    @Test
    void withoutKickerDoesNotTargetOrStealAnArtifact() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());

        harness.setHand(player1, List.of(new ThievingSkydiver()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(equipment);
        assertThat(equipment.getAttachedTo()).isNull();
    }

    @Test
    void kickedStealsAnEligibleArtifactAndAttachesAnEquipment() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());

        harness.setHand(player1, List.of(new ThievingSkydiver()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.ensurePriority(player1);
        gs.playCard(gd, player1, 0, 2, equipment.getId(), null, List.of(), List.of(), false,
                null, null, null, null, null, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent skydiver = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof ThievingSkydiver)
                .findFirst()
                .orElseThrow();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(equipment);
        assertThat(equipment.getAttachedTo()).isEqualTo(skydiver.getId());
    }

    @Test
    void kickedStealsAnEligibleNonEquipmentArtifactWithoutAttachingIt() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        harness.setHand(player1, List.of(new ThievingSkydiver()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.ensurePriority(player1);
        gs.playCard(gd, player1, 0, 1, artifact.getId(), null, List.of(), List.of(), false,
                null, null, null, null, null, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        assertThat(artifact.getAttachedTo()).isNull();
    }

    @Test
    void kickedCannotTargetAnArtifactWithManaValueAboveX() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GildedLotus());

        harness.setHand(player1, List.of(new ThievingSkydiver()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.ensurePriority(player1);
        gs.playCard(gd, player1, 0, 2, null, null, List.of(), List.of(),
                false, null, null, null, null, null, true);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotKickWithZeroXEvenWhenAZeroManaArtifactIsAvailable() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new ThievingSkydiver()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void unkickedEntryDoesNotCreateAnArtifactTheftTrigger() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new ThievingSkydiver()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
    }

    @Test
    void canCastKickedWithoutAnyEligibleArtifactOnTheBattlefield() {
        harness.setHand(player1, List.of(new ThievingSkydiver()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.ensurePriority(player1);

        gs.playCard(gd, player1, 0, 1, null, null, List.of(), List.of(), false,
                null, null, null, null, null, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thieving Skydiver");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canAttachAnEquipmentAlreadyControlledByTheCaster() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.setHand(player1, List.of(new ThievingSkydiver()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.ensurePriority(player1);

        gs.playCard(gd, player1, 0, 1, equipment.getId(), null, List.of(), List.of(), false,
                null, null, null, null, null, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(harness.getPermanentId(player1, "Thieving Skydiver"));
    }
}
