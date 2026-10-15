package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.t.ThreeTreeMascot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ValleyRally.class, ThreeTreeMascot.class})
class ValleyRallyTest extends BaseCardTest {

    @Test
    void withoutGiftBoostsAllYourCreaturesAndNeedsNoTarget() {
        Permanent firstBear = harness.addToBattlefieldAndReturn(player1, new ThreeTreeMascot());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player1, new ThreeTreeMascot());

        cast(null, false);

        assertThat(firstBear.getPowerModifier()).isEqualTo(2);
        assertThat(secondBear.getPowerModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, firstBear, Keyword.FIRST_STRIKE)).isFalse();
        harness.assertNotOnBattlefield(player2, "Food");
    }

    @Test
    void promisingGiftCreatesFoodBoostsYourCreaturesAndGrantsFirstStrikeToTheTarget() {
        Permanent firstBear = harness.addToBattlefieldAndReturn(player1, new ThreeTreeMascot());
        Permanent targetBear = harness.addToBattlefieldAndReturn(player1, new ThreeTreeMascot());

        cast(targetBear, true);

        assertThat(firstBear.getPowerModifier()).isEqualTo(2);
        assertThat(targetBear.getPowerModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, firstBear, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, targetBear, Keyword.FIRST_STRIKE)).isTrue();
        harness.assertOnBattlefield(player2, "Food");
    }

    @Test
    void promisedGiftRequiresATargetCreatureYouControl() {
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new ThreeTreeMascot());
        prepareSpell();

        assertThatThrownBy(() -> harness.castInstantWithGift(player1, 0, opponentBear.getId(), true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    private void cast(Permanent target, boolean giftPromised) {
        prepareSpell();
        harness.castInstantWithGift(player1, 0, target == null ? null : target.getId(), giftPromised);
        harness.passBothPriorities();
    }

    @Test
    void promisedGiftCannotBeCastWithoutACreatureTarget() {
        harness.addToBattlefield(player1, new ThreeTreeMascot());
        prepareSpell();

        assertThatThrownBy(() -> harness.castInstantWithGift(player1, 0, null, true))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Food");
    }

    @Test
    void illegalGiftTargetPreventsTheFoodAndTheBoost() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ThreeTreeMascot());
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new ThreeTreeMascot());
        prepareSpell();
        harness.castInstantWithGift(player1, 0, target.getId(), true);

        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(survivor.getPowerModifier()).isZero();
        harness.assertNotOnBattlefield(player2, "Food");
        harness.assertInGraveyard(player1, "Valley Rally");
    }

    @Test
    void boostAndFirstStrikeExpireAndDoNotAffectLaterCreaturesOrOpponents() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ThreeTreeMascot());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new ThreeTreeMascot());

        cast(target, true);
        Permanent newcomer = harness.addToBattlefieldAndReturn(player1, new ThreeTreeMascot());

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(opponent.getPowerModifier()).isZero();
        assertThat(newcomer.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, newcomer, Keyword.FIRST_STRIKE)).isFalse();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(target.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isFalse();
        harness.assertOnBattlefield(player2, "Food");
    }

    @Test
    void opponentCanSacrificeTheGiftedFoodForThreeLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ThreeTreeMascot());
        harness.setLife(player2, 10);
        cast(target, true);

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, null);
        harness.assertNotOnBattlefield(player2, "Food");
        harness.assertLife(player2, 10);
        harness.passBothPriorities();

        harness.assertLife(player2, 13);
    }

    @Test
    void canResolveWithoutGiftOrAnyCreatures() {
        cast(null, false);

        harness.assertNotOnBattlefield(player2, "Food");
        harness.assertInGraveyard(player1, "Valley Rally");
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new ValleyRally()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
