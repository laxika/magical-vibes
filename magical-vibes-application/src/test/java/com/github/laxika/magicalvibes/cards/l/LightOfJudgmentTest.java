package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LightOfJudgment.class, GrizzlyBears.class, LeoninScimitar.class})
class LightOfJudgmentTest extends BaseCardTest {

    @Test
    void dealsDamageAndDestroysOneAttachedEquipment() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent destroyedEquipment = addAttachedEquipment(player2, target);
        Permanent remainingEquipment = addAttachedEquipment(player2, target);
        Permanent unattachedEquipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());

        cast(target);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .containsExactly(destroyedEquipment.getId(), remainingEquipment.getId())
                .doesNotContain(unattachedEquipment.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(destroyedEquipment.getId()));

        harness.assertInGraveyard(player2, "Leonin Scimitar");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(remainingEquipment.getId()))
                .anyMatch(permanent -> permanent.getId().equals(unattachedEquipment.getId()));
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void mayChooseNoEquipmentToDestroy() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent equipment = addAttachedEquipment(player2, target);

        cast(target);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(equipment.getId()));
    }

    @Test
    void cannotTargetNoncreature() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        harness.setHand(player1, List.of(new LightOfJudgment()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, equipment.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new LightOfJudgment()));
        addMana();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private Permanent addAttachedEquipment(com.github.laxika.magicalvibes.model.Player player,
                                           Permanent target) {
        Permanent equipment = harness.addToBattlefieldAndReturn(player, new LeoninScimitar());
        equipment.setAttachedTo(target.getId());
        return equipment;
    }
}
