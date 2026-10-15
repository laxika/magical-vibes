package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.t.TitansNest;
import com.github.laxika.magicalvibes.cards.c.CrystallineGiant;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Gemrazer.class, TitansNest.class, CrystallineGiant.class})
class GemrazerTest extends BaseCardTest {

    @Test
    void mutatingDestroysTargetArtifactAnOpponentControls() {
        Permanent gemrazer = addCreatureReady(player1, new Gemrazer());
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new CrystallineGiant());

        triggerMutation(gemrazer);
        harness.handlePermanentChosen(player1, fountain.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Crystalline Giant");
        harness.assertInGraveyard(player2, "Crystalline Giant");
    }

    @Test
    void mutatingDestroysTargetEnchantmentAnOpponentControls() {
        Permanent gemrazer = addCreatureReady(player1, new Gemrazer());
        Permanent chorus = harness.addToBattlefieldAndReturn(player2, new TitansNest());

        triggerMutation(gemrazer);
        harness.handlePermanentChosen(player1, chorus.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Titans' Nest");
        harness.assertInGraveyard(player2, "Titans' Nest");
    }

    @Test
    void mutatingCannotTargetOwnArtifact() {
        Permanent gemrazer = addCreatureReady(player1, new Gemrazer());
        Permanent ownFountain = harness.addToBattlefieldAndReturn(player1, new CrystallineGiant());
        harness.addToBattlefield(player2, new CrystallineGiant());

        triggerMutation(gemrazer);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownFountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    void mutatingCannotTargetOwnEnchantmentOrAnOpposingNonartifactCreature() {
        Permanent gemrazer = addCreatureReady(player1, new Gemrazer());
        Permanent ownEnchantment = harness.addToBattlefieldAndReturn(player1, new TitansNest());
        Permanent opposingCreature = addCreatureReady(player2, new Gemrazer());
        harness.addToBattlefield(player2, new CrystallineGiant());

        triggerMutation(gemrazer);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownEnchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opposingCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    void mutationWithoutLegalTargetsDoesNotPutAnAbilityOnTheStack() {
        Permanent gemrazer = addCreatureReady(player1, new Gemrazer());
        harness.addToBattlefield(player1, new CrystallineGiant());
        harness.addToBattlefield(player1, new TitansNest());
        addCreatureReady(player2, new Gemrazer());

        triggerMutation(gemrazer);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void destructionStillResolvesAfterSourceLeavesBattlefield() {
        Permanent gemrazer = addCreatureReady(player1, new Gemrazer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CrystallineGiant());

        triggerMutation(gemrazer);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(gemrazer);
        gd.playerGraveyards.get(player1.getId()).add(gemrazer.getCard());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Crystalline Giant");
        harness.assertNotOnBattlefield(player2, "Crystalline Giant");
    }

    @Test
    void castingNormallyDoesNotTriggerDestruction() {
        harness.addToBattlefield(player2, new CrystallineGiant());
        harness.castFromHand(player1, new Gemrazer(), "{3}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gemrazer");
        harness.assertOnBattlefield(player2, "Crystalline Giant");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void triggerMutation(Permanent gemrazer) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, gemrazer, List.of(gemrazer.getCard()), player1.getId()));
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextSelfTriggeredAbilityTarget(gd));
    }
}
