package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({SquiresLightblade.class, GrizzlyBears.class})
class SquiresLightbladeTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Squire's Lightblade attaches it to target creature you control")
    void enteringAttachesToTargetCreatureYouControl() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SquiresLightblade()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castArtifact(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent lightblade = findPermanent(player1, "Squire's Lightblade");
        assertThat(lightblade.getAttachedTo()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Entry attachment gives the creature first strike until end of turn")
    void entryAttachmentGrantsFirstStrikeUntilEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SquiresLightblade()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castArtifact(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Equipped creature gets +1/+0")
    void equippedCreatureGetsBoost() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent lightblade = addCreatureReady(player1, new SquiresLightblade());
        lightblade.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip {3} attaches Squire's Lightblade to a creature you control")
    void equipAttachesToCreatureYouControl() {
        Permanent lightblade = addCreatureReady(player1, new SquiresLightblade());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(lightblade.getAttachedTo()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Moving the Equipment preserves the entry recipient's first strike without granting it again")
    void movingEquipmentDoesNotMoveFirstStrike() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SquiresLightblade()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castArtifact(player1, 0, first.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent lightblade = findPermanent(player1, "Squire's Lightblade");
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 2, null, second.getId());
        harness.passBothPriorities();

        assertThat(lightblade.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, first, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Flash allows entry and attachment during the opponent's turn")
    void canEnterDuringOpponentsTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new SquiresLightblade()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castArtifact(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Squire's Lightblade").getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The entry trigger still grants first strike if the Equipment leaves before resolution")
    void entryGrantsFirstStrikeWithoutEquipment() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SquiresLightblade()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castArtifact(player1, 0, bears.getId());
        harness.passBothPriorities();

        Permanent lightblade = findPermanent(player1, "Squire's Lightblade");
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, lightblade));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }
}
