package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HerdchaserDragon;
import com.github.laxika.magicalvibes.cards.n.NomadsEnKor;
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

@CardUsed({TheBlackArrow.class, FountainOfYouth.class, GrizzlyBears.class, HerdchaserDragon.class, NomadsEnKor.class})
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

    @Test
    @DisplayName("A Dragon receiving redirected Arrow damage is destroyed")
    void redirectedDamageDestroysDragon() {
        Permanent nomads = harness.addToBattlefieldAndReturn(player2, new NomadsEnKor());
        Permanent dragon = harness.addToBattlefieldAndReturn(player2, new HerdchaserDragon());
        harness.activateAbility(player2, 0, null, dragon.getId());
        harness.passBothPriorities();

        castArrowTargeting(nomads.getId());

        assertThat(nomads.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Nomads en-Kor");
        harness.assertNotOnBattlefield(player2, "Herdchaser Dragon");
        harness.assertInGraveyard(player2, "Herdchaser Dragon");
    }

    @Test
    @DisplayName("A Dragon is not destroyed when all Arrow damage is prevented")
    void preventedDamageDoesNotDestroyDragon() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player2, new HerdchaserDragon());
        dragon.setDamagePreventionShield(1);

        castArrowTargeting(dragon.getId());

        assertThat(dragon.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Herdchaser Dragon");
        harness.assertNotInGraveyard(player2, "Herdchaser Dragon");
    }

    @Test
    @DisplayName("Re-equipping transfers the bonus and reach to the new creature")
    void reequippingTransfersBonusAndReach() {
        Permanent arrow = harness.addToBattlefieldAndReturn(player1, new TheBlackArrow());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(arrow.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.REACH)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, second, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Flash allows the Arrow to enter during the opponent's turn")
    void canCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);

        castArrowTargeting(player2.getId());

        harness.assertOnBattlefield(player1, "The Black Arrow");
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Equip cannot attach the Arrow to an opponent's creature")
    void cannotEquipOpponentsCreature() {
        harness.addToBattlefield(player1, new TheBlackArrow());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    private void castArrowTargeting(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new TheBlackArrow()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
