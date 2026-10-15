package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AuriokGlaivemaster;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Memnarch.class, AuriokGlaivemaster.class, DarksteelIngot.class})
class MemnarchTest extends BaseCardTest {

    @Test
    void turnsTargetPermanentIntoAnArtifactIndefinitely() {
        addReadyMemnarch(player1);
        Permanent target = addCreatureReady(player2, new AuriokGlaivemaster());

        assertThat(gqs.isArtifact(target)).isFalse();

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(target)).isTrue();
        assertThat(gqs.isCreature(gd, target)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(target)).isTrue();
    }

    @Test
    void gainsPermanentControlOfTargetArtifact() {
        addReadyMemnarch(player1);
        Permanent ingot = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 1, null, ingot.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Darksteel Ingot").getId()).isEqualTo(ingot.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(ingot.getId()));

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Darksteel Ingot").getId()).isEqualTo(ingot.getId());
    }

    @Test
    void secondAbilityCannotTargetNonartifactPermanent() {
        addReadyMemnarch(player1);
        Permanent target = addCreatureReady(player2, new AuriokGlaivemaster());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    @Test
    void canStealAConvertedCreatureAndBothEffectsSurviveMemnarchLeaving() {
        Permanent memnarch = addReadyMemnarch(player1);
        Permanent target = addCreatureReady(player2, new AuriokGlaivemaster());

        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, memnarch);
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Auriok Glaivemaster").getId()).isEqualTo(target.getId());
        assertThat(gqs.isArtifact(target)).isTrue();
        assertThat(gqs.isCreature(gd, target)).isTrue();
        harness.assertNotOnBattlefield(player2, "Auriok Glaivemaster");
        harness.assertInGraveyard(player1, "Memnarch");
    }

    @Test
    void controlAbilityResolvesEvenIfMemnarchLeavesInResponse() {
        Permanent memnarch = addReadyMemnarch(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, memnarch);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Darksteel Ingot").getId()).isEqualTo(target.getId());
        harness.assertNotOnBattlefield(player2, "Darksteel Ingot");
    }

    @Test
    void artifactConversionDoesNotFollowTheCardWhenItLeavesAndReturns() {
        addReadyMemnarch(player1);
        Permanent target = addCreatureReady(player2, new AuriokGlaivemaster());

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.isArtifact(target)).isTrue();

        harness.setHand(player2, java.util.List.of());
        harness.getPermanentRemovalService().removePermanentToHand(gd, target);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player2, "Auriok Glaivemaster");
        assertThat(returned.getId()).isNotEqualTo(target.getId());
        assertThat(gqs.isArtifact(returned)).isFalse();
    }

    private Permanent addReadyMemnarch(Player player) {
        return addCreatureReady(player, new Memnarch());
    }
}
