package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhotonBlastBarrage.class, CrawWurm.class, Mountain.class})
class PhotonBlastBarrageTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to a target creature when X is zero")
    void dealsDamageWithoutCopies() {
        Permanent wurm = addWurm();
        castPhotonBlastBarrage(0, wurm);

        harness.passBothPriorities();

        assertThat(wurm.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Copies itself X times and each copy deals 1 damage")
    void copiesItselfForX() {
        Permanent wurm = addWurm();
        castPhotonBlastBarrage(2, wurm);

        harness.passBothPriorities();
        declineRetargetingCopies(2);
        resolveAllTriggers();

        assertThat(wurm.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("May choose a new creature target for a copy")
    void copyMayChooseNewTarget() {
        Permanent firstWurm = addWurm();
        Permanent secondWurm = addWurm();
        castPhotonBlastBarrage(1, firstWurm);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, secondWurm.getId());
        resolveAllTriggers();

        assertThat(firstWurm.getMarkedDamage()).isEqualTo(1);
        assertThat(secondWurm.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void rejectsNoncreatureTarget() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        assertThatThrownBy(() -> castPhotonBlastBarrage(0, mountain))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addWurm() {
        return harness.addToBattlefieldAndReturn(player2, new CrawWurm());
    }

    private void castPhotonBlastBarrage(int xValue, Permanent target) {
        harness.setHand(player1, List.of(new PhotonBlastBarrage()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.castSorcery(player1, 0, xValue, target.getId());
    }

    private void declineRetargetingCopies(int count) {
        for (int i = 0; i < count; i++) {
            assertThat(gd.interaction.activeInteraction())
                    .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, false);
        }
    }
}
