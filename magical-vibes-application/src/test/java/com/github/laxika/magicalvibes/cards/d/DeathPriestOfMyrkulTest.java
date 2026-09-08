package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.Gravecrawler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SkeletonArcher;
import com.github.laxika.magicalvibes.cards.v.VampireNoble;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeathPriestOfMyrkul.class, SkeletonArcher.class, VampireNoble.class, Gravecrawler.class,
        WalkingCorpse.class, GrizzlyBears.class})
class DeathPriestOfMyrkulTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts Skeletons, Vampires, and Zombies you control")
    void boostsUndeadSubtypes() {
        Permanent skeleton = harness.addToBattlefieldAndReturn(player1, new SkeletonArcher());
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new VampireNoble());
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new Gravecrawler());

        int skeletonPower = gqs.getEffectivePower(gd, skeleton);
        int skeletonToughness = gqs.getEffectiveToughness(gd, skeleton);
        int vampirePower = gqs.getEffectivePower(gd, vampire);
        int vampireToughness = gqs.getEffectiveToughness(gd, vampire);
        int zombiePower = gqs.getEffectivePower(gd, zombie);
        int zombieToughness = gqs.getEffectiveToughness(gd, zombie);

        harness.addToBattlefield(player1, new DeathPriestOfMyrkul());

        assertThat(gqs.getEffectivePower(gd, skeleton)).isEqualTo(skeletonPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, skeleton)).isEqualTo(skeletonToughness + 1);
        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(vampirePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(vampireToughness + 1);
        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(zombiePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(zombieToughness + 1);
    }

    @Test
    @DisplayName("Does not boost creatures without a listed subtype or creatures an opponent controls")
    void onlyBoostsMatchingCreaturesYouControl() {
        Permanent ownZombie = harness.addToBattlefieldAndReturn(player1, new Gravecrawler());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentZombie = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());

        int ownZombiePower = gqs.getEffectivePower(gd, ownZombie);
        int ownZombieToughness = gqs.getEffectiveToughness(gd, ownZombie);
        int ownBearPower = gqs.getEffectivePower(gd, ownBear);
        int ownBearToughness = gqs.getEffectiveToughness(gd, ownBear);
        int opponentZombiePower = gqs.getEffectivePower(gd, opponentZombie);
        int opponentZombieToughness = gqs.getEffectiveToughness(gd, opponentZombie);

        harness.addToBattlefield(player1, new DeathPriestOfMyrkul());

        assertThat(gqs.getEffectivePower(gd, opponentZombie)).isEqualTo(opponentZombiePower);
        assertThat(gqs.getEffectiveToughness(gd, opponentZombie)).isEqualTo(opponentZombieToughness);
        assertThat(gqs.getEffectivePower(gd, ownZombie)).isEqualTo(ownZombiePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, ownZombie)).isEqualTo(ownZombieToughness + 1);
        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(ownBearPower);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(ownBearToughness);
    }

    @Test
    @DisplayName("Does not trigger at the end step when no creature died")
    void doesNotTriggerWithoutMorbid() {
        harness.addToBattlefield(player1, new DeathPriestOfMyrkul());

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Skeleton")).isEmpty();
    }

    @Test
    @DisplayName("Pays {1} after any creature dies to create a Skeleton")
    void paysToCreateSkeletonAfterAnyCreatureDies() {
        harness.addToBattlefield(player1, new DeathPriestOfMyrkul());
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Skeleton")).hasSize(1);
    }

    @Test
    @DisplayName("Declining the payment creates no Skeleton")
    void declinesToCreateSkeleton() {
        harness.addToBattlefield(player1, new DeathPriestOfMyrkul());
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Skeleton")).isEmpty();
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
