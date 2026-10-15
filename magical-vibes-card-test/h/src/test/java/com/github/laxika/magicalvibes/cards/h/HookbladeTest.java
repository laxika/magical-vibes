package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.l.LoyalInventor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Hookblade.class, LoyalInventor.class})
class HookbladeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters attached to a creature you control and grants it +1/+0")
    void entersAttachedAndBoostsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LoyalInventor());
        harness.setHand(player1, List.of(new Hookblade()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent hookblade = findPermanent(player1, "Hookblade");
        assertThat(hookblade.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equipped creature has flying during Hookblade's controller's turn")
    void equippedCreatureHasFlyingDuringControllerTurn() {
        Permanent creature = addCreatureReady(player1, new LoyalInventor());
        Permanent hookblade = addCreatureReady(player1, new Hookblade());
        hookblade.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature loses flying during the opponent's turn")
    void equippedCreatureLosesFlyingDuringOpponentsTurn() {
        Permanent creature = addCreatureReady(player1, new LoyalInventor());
        Permanent hookblade = addCreatureReady(player1, new Hookblade());
        hookblade.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player2);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Equip {2} attaches Hookblade to a creature you control")
    void equipAttachesToCreature() {
        Permanent hookblade = addCreatureReady(player1, new Hookblade());
        Permanent creature = addCreatureReady(player1, new LoyalInventor());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(hookblade.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("ETB attach cannot target an opponent's creature")
    void etbCannotTargetOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new LoyalInventor());
        harness.setHand(player1, List.of(new Hookblade()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBeCastWithoutAnyCreatureToAttachTo() {
        harness.setHand(player1, List.of(new Hookblade()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Hookblade").getAttachedTo()).isNull();
    }

    @Test
    void reEquippingMovesBothBonusesToNewCreature() {
        Permanent hookblade = harness.addToBattlefieldAndReturn(player1, new Hookblade());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new LoyalInventor());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new LoyalInventor());
        hookblade.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isTrue();
        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(hookblade.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isTrue();
    }

    @Test
    void flyingFollowsEquipmentControllerEvenWhenCreatureHasDifferentController() {
        Permanent hookblade = harness.addToBattlefieldAndReturn(player1, new Hookblade());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LoyalInventor());
        hookblade.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    void equipCannotTargetOpponentsCreature() {
        harness.addToBattlefield(player1, new Hookblade());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LoyalInventor());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void equipCannotBeActivatedOutsideMainPhase() {
        harness.addToBattlefield(player1, new Hookblade());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LoyalInventor());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void equipRequiresTwoMana() {
        harness.addToBattlefield(player1, new Hookblade());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LoyalInventor());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
