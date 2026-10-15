package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RedtoothGenealogist.class, GrizzlyBears.class, GiantGrowth.class})
class RedtoothGenealogistTest extends BaseCardTest {

    @Test
    void entersAndAttachesRoyalRoleToAnotherCreatureYouControl() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        RedtoothGenealogist genealogist = new RedtoothGenealogist();
        harness.castFromHand(player1, genealogist, "{2}{G}");
        harness.passBothPriorities();

        Permanent source = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == genealogist)
                .findFirst()
                .orElseThrow();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId()).doesNotContain(source.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent role = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> target.getId().equals(permanent.getAttachedTo()))
                .findFirst()
                .orElseThrow();
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    void royalRoleCountersOpponentSpellTargetingEnchantedCreatureWhenUnpaid() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new RedtoothGenealogist());
        attachRoyalRoleTo(target);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player2, 0, target.getId());

        harness.assertInGraveyard(player2, "Giant Growth");
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    void ownSpellDoesNotTriggerRoyalRoleWard() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        attachRoyalRoleTo(target);
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player1, "Giant Growth");
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
    }

    @Test
    void payingRoyalRoleWardAllowsOpponentSpellToResolve() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        attachRoyalRoleTo(target);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Giant Growth");
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
    }

    @Test
    void secondRoyalRoleReplacesTheFirstInsteadOfStackingBonuses() {
        Permanent target = addCreatureReady(player1, new RedtoothGenealogist());
        attachRoyalRoleTo(target);
        Permanent firstRole = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> target.getId().equals(permanent.getAttachedTo()))
                .findFirst().orElseThrow();

        attachRoyalRoleTo(target);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(firstRole)
                .filteredOn(permanent -> target.getId().equals(permanent.getAttachedTo()))
                .hasSize(1);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    void noOtherControlledCreatureMeansNoRoleIsCreated() {
        addCreatureReady(player2, new RedtoothGenealogist());
        harness.castFromHand(player1, new RedtoothGenealogist(), "{2}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Redtooth Genealogist");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    private void attachRoyalRoleTo(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new RedtoothGenealogist(), "{2}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
