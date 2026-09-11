package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HerdchaserDragon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheBlackArrow.class, FountainOfYouth.class, GrizzlyBears.class, HerdchaserDragon.class})
class TheBlackArrowTest extends BaseCardTest {

    @Test
    @DisplayName("Entering The Black Arrow deals 1 damage to a player")
    void enteringDealsDamageToPlayer() {
        castArrowTargeting(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Entering The Black Arrow destroys a Dragon that was dealt damage")
    void enteringDestroysDamagedDragon() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player2, new HerdchaserDragon());
        castArrowTargeting(dragon.getId());

        harness.assertNotOnBattlefield(player2, "Herdchaser Dragon");
        harness.assertInGraveyard(player2, "Herdchaser Dragon");
    }

    @Test
    @DisplayName("Entering The Black Arrow only damages a non-Dragon creature")
    void enteringDoesNotDestroyNonDragon() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castArrowTargeting(creature.getId());

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Equipped creature gets +1/+1 and reach")
    void equippedCreatureGetsBoostAndReach() {
        Permanent arrow = harness.addToBattlefieldAndReturn(player1, new TheBlackArrow());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(arrow.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("The Black Arrow cannot target a non-any-target permanent")
    void cannotTargetNonAnyTargetPermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new TheBlackArrow()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be any target");
    }

    private void castArrowTargeting(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new TheBlackArrow()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
