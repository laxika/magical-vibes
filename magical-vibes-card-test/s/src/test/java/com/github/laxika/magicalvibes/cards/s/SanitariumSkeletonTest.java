package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LightningAxe;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SanitariumSkeleton.class, LightningAxe.class})
class SanitariumSkeletonTest extends BaseCardTest {

    @Test
    @DisplayName("Graveyard ability returns Sanitarium Skeleton to hand")
    void resolvingGraveyardAbilityReturnsToHand() {
        harness.setGraveyard(player1, List.of(new SanitariumSkeleton()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Sanitarium Skeleton");
        harness.assertNotInGraveyard(player1, "Sanitarium Skeleton");
    }

    @Test
    @DisplayName("Cannot activate graveyard ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        harness.setGraveyard(player1, List.of(new SanitariumSkeleton()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsOnlyTheActivatedSkeleton() {
        SanitariumSkeleton first = new SanitariumSkeleton();
        SanitariumSkeleton second = new SanitariumSkeleton();
        SanitariumSkeleton opponents = new SanitariumSkeleton();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setGraveyard(player2, List.of(opponents));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponents);
    }

    @Test
    void cannotPayTheBlackRequirementWithOnlyColorlessMana() {
        harness.setGraveyard(player1, List.of(new SanitariumSkeleton()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Sanitarium Skeleton");
        harness.assertNotInHand(player1, "Sanitarium Skeleton");
    }

    @Test
    void multipleActivationsReturnTheCardOnlyOnce() {
        SanitariumSkeleton skeleton = new SanitariumSkeleton();
        harness.setGraveyard(player1, List.of(skeleton));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(skeleton);
        harness.assertNotInGraveyard(player1, "Sanitarium Skeleton");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void oldActivationCannotReturnSkeletonAfterItIsDiscardedAgain() {
        SanitariumSkeleton skeleton = new SanitariumSkeleton();
        harness.setGraveyard(player1, List.of(skeleton));
        harness.setHand(player1, List.of(new LightningAxe()));
        var target = harness.addToBattlefieldAndReturn(player2, new SanitariumSkeleton());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Sanitarium Skeleton");

        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sanitarium Skeleton");
        harness.assertNotInHand(player1, "Sanitarium Skeleton");
        assertThat(gd.stack).isEmpty();
    }
}
