package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({ChainOfAcid.class, Forest.class, GlorySeeker.class})
class ChainOfAcidTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target noncreature permanent and asks its controller to copy the spell")
    void destroysNoncreaturePermanentAndOffersCopy() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        castAt(target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("The destroyed permanent's controller may create a copy")
    void targetControllerMayCopySpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        castAt(target.getId());

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Declining to retarget a copy leaves its destroyed original target unchanged")
    void decliningRetargetLeavesCopyWithDestroyedTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent otherTarget = harness.addToBattlefieldAndReturn(player1, new Forest());
        castAt(target.getId());

        harness.handleMayAbilityChosen(player2, true);
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(otherTarget.getId()));
    }

    @Test
    @DisplayName("A copy may retarget another noncreature permanent and its controller gets the next copy choice")
    void copyMayRetargetAndRepeatForNewTargetController() {
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent newTarget = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
        castAt(originalTarget.getId());

        harness.handleMayAbilityChosen(player2, true);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(newTarget.getId())
                .doesNotContain(originalTarget.getId(), creature.getId());
        harness.handlePermanentChosen(player2, newTarget.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Declining the copy creates no copy")
    void decliningCopyCreatesNoCopy() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        castAt(target.getId());

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        harness.setHand(player1, List.of(new ChainOfAcid()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An indestructible target survives and its controller can copy without changing targets")
    void indestructibleTargetCanBeCopiedWithSameTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        target.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        castAt(target.getId());

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        harness.assertNotInGraveyard(player1, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof ChainOfAcid)
                .hasSize(1);
    }

    @Test
    @DisplayName("Regeneration saves the target without preventing its controller from copying")
    void regeneratedTargetStillOffersCopy() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        target.setRegenerationShield(1);
        castAt(target.getId());

        harness.assertOnBattlefield(player2, "Forest");
        harness.assertNotInGraveyard(player2, "Forest");
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getRegenerationShield()).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Forest");
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Chain of Acid");
        harness.assertNotInGraveyard(player2, "Chain of Acid");
    }

    @Test
    @DisplayName("A target that becomes a creature makes the spell fail to resolve without a copy choice")
    void targetBecomingCreaturePreventsResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new ChainOfAcid()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castSorcery(player1, 0, target.getId());

        target.setAnimatedUntilEndOfTurn(true);
        target.setAnimatedPower(2);
        target.setAnimatedToughness(2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Forest");
        harness.assertNotInGraveyard(player2, "Forest");
        harness.assertInGraveyard(player1, "Chain of Acid");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castAt(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new ChainOfAcid()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveSorcery(player1, 0, targetId);
    }
}
