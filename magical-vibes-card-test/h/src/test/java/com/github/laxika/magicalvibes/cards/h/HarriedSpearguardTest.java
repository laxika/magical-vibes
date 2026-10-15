package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.ArmoryMice;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HarriedSpearguard.class, ArmoryMice.class})
class HarriedSpearguardTest extends BaseCardTest {

    @Test
    void createsANonblockingRatWhenItDies() {
        Permanent spearguard = harness.addToBattlefieldAndReturn(player1, new HarriedSpearguard());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, spearguard));

        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        List<Permanent> rats = findPermanents(player1, "Rat");
        assertThat(rats).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, rats.getFirst())).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, rats.getFirst())).isEqualTo(1);
        assertThat(rats.getFirst().getCard().getColors()).containsExactly(CardColor.BLACK);
        assertThat(rats.getFirst().getCard().getSubtypes()).containsExactly(CardSubtype.RAT);
        assertThat(bls.canBlock(gd, rats.getFirst())).isFalse();
    }

    @Test
    void doesNotTriggerWhenAnotherCreatureDies() {
        harness.addToBattlefield(player1, new HarriedSpearguard());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, new ArmoryMice());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, otherCreature));

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Rat")).isEmpty();
    }

    @Test
    void canAttackTheTurnItIsCast() {
        harness.castFromHand(player1, new HarriedSpearguard(), "{R}");
        harness.passBothPriorities();

        Permanent spearguard = findPermanent(player1, "Harried Spearguard");
        assertThat(als.canAttack(gd, spearguard, player1.getId())).isTrue();
        declareAttackers(List.of(0));
        resolveCombat();
        harness.assertLife(player2, 19);
    }

    @Test
    void createsRatForTheControllerAtDeathRatherThanTheOwner() {
        Permanent spearguard = harness.addToBattlefieldAndReturn(player1, new HarriedSpearguard());
        gd.playerBattlefields.get(player1.getId()).remove(spearguard);
        gd.playerBattlefields.get(player2.getId()).add(spearguard);
        gd.stolenCreatures.put(spearguard.getId(), player1.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, spearguard));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Rat")).isEmpty();
        assertThat(findPermanents(player2, "Rat")).hasSize(1);
        harness.assertInGraveyard(player1, "Harried Spearguard");
    }

    @Test
    void exileDoesNotCreateARat() {
        Permanent spearguard = harness.addToBattlefieldAndReturn(player1, new HarriedSpearguard());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, spearguard));

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Rat")).isEmpty();
    }

    @Test
    void ratDoesNotInheritSpearguardsDeathAbility() {
        Permanent spearguard = harness.addToBattlefieldAndReturn(player1, new HarriedSpearguard());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, spearguard));
        harness.passBothPriorities();
        Permanent rat = findPermanent(player1, "Rat");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, rat));

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Rat")).isEmpty();
    }

    @Test
    void eachSpearguardCreatesOnlyItsOwnRatWhenBothDie() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new HarriedSpearguard());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new HarriedSpearguard());

        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, first);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, second);
        });
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Rat")).hasSize(2);
        assertThat(findPermanents(player2, "Rat")).isEmpty();
    }
}
