package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.t.TrueFaithCenser;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MilitantInquisitor.class, TrueFaithCenser.class})
class MilitantInquisitorTest extends BaseCardTest {

    @Test
    @DisplayName("Militant Inquisitor gets +1/+0 for each Equipment you control")
    void getsPowerForEachControlledEquipment() {
        Permanent inquisitor = harness.addToBattlefieldAndReturn(player1, new MilitantInquisitor());
        harness.addToBattlefield(player1, new TrueFaithCenser());
        harness.addToBattlefield(player1, new TrueFaithCenser());

        assertThat(gqs.getEffectivePower(gd, inquisitor)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, inquisitor)).isEqualTo(3);
    }

    @Test
    @DisplayName("The bonus updates as Equipment enters and leaves the battlefield")
    void bonusUpdatesAsEquipmentEntersAndLeaves() {
        Permanent inquisitor = harness.addToBattlefieldAndReturn(player1, new MilitantInquisitor());
        assertThat(gqs.getEffectivePower(gd, inquisitor)).isEqualTo(2);

        harness.castFromHand(player1, new TrueFaithCenser(), "{2}");
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, inquisitor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, inquisitor)).isEqualTo(3);

        Permanent equipment = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof TrueFaithCenser)
                .findFirst().orElseThrow();
        harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, equipment);
        assertThat(gqs.getEffectivePower(gd, inquisitor)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, inquisitor)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equipment attached to another creature still counts")
    void equipmentAttachedToAnotherCreatureCounts() {
        Permanent inquisitor = harness.addToBattlefieldAndReturn(player1, new MilitantInquisitor());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new MilitantInquisitor());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new TrueFaithCenser());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 2, null, other.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(other.getId());
        assertThat(gqs.getEffectivePower(gd, inquisitor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, inquisitor)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(4);
    }

    @Test
    @DisplayName("Equipment cards outside the battlefield do not count")
    void equipmentOutsideBattlefieldDoesNotCount() {
        Permanent inquisitor = harness.addToBattlefieldAndReturn(player1, new MilitantInquisitor());
        harness.setHand(player1, List.of(new TrueFaithCenser()));
        harness.setGraveyard(player1, List.of(new TrueFaithCenser()));

        assertThat(gqs.getEffectivePower(gd, inquisitor)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, inquisitor)).isEqualTo(3);
    }

    @Test
    @DisplayName("Opponent-controlled Equipment does not boost Militant Inquisitor")
    void opponentEquipmentDoesNotCount() {
        Permanent inquisitor = harness.addToBattlefieldAndReturn(player1, new MilitantInquisitor());
        harness.addToBattlefield(player2, new TrueFaithCenser());
        harness.addToBattlefield(player2, new TrueFaithCenser());

        assertThat(gqs.getEffectivePower(gd, inquisitor)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, inquisitor)).isEqualTo(3);
    }
}
