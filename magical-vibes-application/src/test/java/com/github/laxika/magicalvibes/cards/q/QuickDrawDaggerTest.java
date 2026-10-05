package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.b.BrokersVeteran;
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

@CardUsed({QuickDrawDagger.class, BrokersVeteran.class})
class QuickDrawDaggerTest extends BaseCardTest {

    @Test
    void entersAttachedAndGrantsFirstStrikeUntilEndOfTurn() {
        Permanent veteran = harness.addToBattlefieldAndReturn(player1, new BrokersVeteran());
        harness.setHand(player1, List.of(new QuickDrawDagger()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, veteran.getId());
        harness.passBothPriorities();

        Permanent dagger = findPermanent(player1, "Quick-Draw Dagger");
        assertThat(dagger.getAttachedTo()).isEqualTo(veteran.getId());
        assertThat(gqs.getEffectivePower(gd, veteran)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, veteran)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, veteran, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, veteran, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void equipAttachesDaggerAndGrantsItsBonus() {
        Permanent dagger = harness.addToBattlefieldAndReturn(player1, new QuickDrawDagger());
        Permanent veteran = harness.addToBattlefieldAndReturn(player1, new BrokersVeteran());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, veteran.getId());
        harness.passBothPriorities();

        assertThat(dagger.getAttachedTo()).isEqualTo(veteran.getId());
        assertThat(gqs.getEffectivePower(gd, veteran)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, veteran)).isEqualTo(2);
    }

    @Test
    void etbTargetMustBeCreatureYouControl() {
        harness.addToBattlefield(player1, new BrokersVeteran());
        Permanent opponentVeteran = harness.addToBattlefieldAndReturn(player2, new BrokersVeteran());
        harness.setHand(player1, List.of(new QuickDrawDagger()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentVeteran.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void flashAllowsCastingDuringCombat() {
        Permanent veteran = harness.addToBattlefieldAndReturn(player1, new BrokersVeteran());
        harness.setHand(player1, List.of(new QuickDrawDagger()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, veteran.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Quick-Draw Dagger").getAttachedTo()).isEqualTo(veteran.getId());
        assertThat(gqs.hasKeyword(gd, veteran, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void movingDaggerLeavesFirstStrikeOnOriginalCreature() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new BrokersVeteran());
        Permanent next = harness.addToBattlefieldAndReturn(player1, new BrokersVeteran());
        harness.setHand(player1, List.of(new QuickDrawDagger()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, original.getId());
        harness.passBothPriorities();
        Permanent dagger = findPermanent(player1, "Quick-Draw Dagger");
        int daggerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(dagger);
        harness.activateAbility(player1, daggerIndex, null, next.getId());
        harness.passBothPriorities();

        assertThat(dagger.getAttachedTo()).isEqualTo(next.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, original, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, next)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, next)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, next, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void canBeCastWithNoCreaturesToAttachTo() {
        harness.setHand(player1, List.of(new QuickDrawDagger()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Quick-Draw Dagger").getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void flashDoesNotAllowEquippingDuringCombat() {
        harness.addToBattlefield(player1, new QuickDrawDagger());
        Permanent veteran = harness.addToBattlefieldAndReturn(player1, new BrokersVeteran());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, veteran.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

}
