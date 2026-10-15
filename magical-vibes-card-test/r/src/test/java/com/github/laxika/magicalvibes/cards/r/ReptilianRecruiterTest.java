package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BakersbaneDuo;
import com.github.laxika.magicalvibes.cards.k.KindlesparkDuo;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReptilianRecruiter.class, KindlesparkDuo.class, BakersbaneDuo.class, RoughshodDuo.class, Mountain.class})
class ReptilianRecruiterTest extends BaseCardTest {

    @Test
    @DisplayName("Steals, untaps, and grants haste to a creature with power 2 or less")
    void stealsSmallCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BakersbaneDuo());
        target.tap();

        castRecruiter(target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isTrue();
    }

    @Test
    @DisplayName("Steals a creature with greater power when you control another Lizard")
    void stealsLargeCreatureWithAnotherLizard() {
        harness.addToBattlefield(player1, new KindlesparkDuo());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RoughshodDuo());
        target.tap();

        castRecruiter(target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Does nothing to a large creature when you control no other Lizard")
    void doesNothingWithoutQualifyingCondition() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RoughshodDuo());
        target.tap();

        castRecruiter(target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mountain());

        assertThatThrownBy(() -> castRecruiter(target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Control and haste expire at cleanup")
    void controlAndHasteExpireAtCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BakersbaneDuo());

        castRecruiter(target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
    }

    @Test
    @DisplayName("Checks target power when the ability resolves, not when it triggers")
    void stealsCreatureWhosePowerDecreasesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RoughshodDuo());
        target.tap();

        castRecruiter(target.getId());
        target.setPowerModifier(-1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Does nothing if the target's power rises above two before resolution")
    void doesNotStealCreatureWhosePowerIncreasesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BakersbaneDuo());
        target.tap();

        castRecruiter(target.getId());
        target.setPowerModifier(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Another Lizard arriving before resolution enables the control effect")
    void checksForAnotherLizardAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RoughshodDuo());
        target.tap();

        castRecruiter(target.getId());
        harness.addToBattlefield(player1, new KindlesparkDuo());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Losing the other Lizard before resolution disables stealing a large creature")
    void losingOtherLizardBeforeResolutionPreventsStealing() {
        Permanent lizard = harness.addToBattlefieldAndReturn(player1, new KindlesparkDuo());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RoughshodDuo());
        target.tap();

        castRecruiter(target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(lizard);
        gd.playerGraveyards.get(player1.getId()).add(lizard.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's Lizard does not satisfy the condition")
    void opponentsLizardDoesNotEnableStealingLargeCreature() {
        harness.addToBattlefield(player2, new KindlesparkDuo());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RoughshodDuo());
        target.tap();

        castRecruiter(target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Can untap and grant haste to a creature already controlled by you")
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new KindlesparkDuo());
        target.tap();

        castRecruiter(target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The ability still resolves if Reptilian Recruiter leaves the battlefield")
    void abilityResolvesWithoutSource() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BakersbaneDuo());
        target.tap();

        castRecruiter(target.getId());
        Permanent recruiter = findPermanent(player1, "Reptilian Recruiter");
        gd.playerBattlefields.get(player1.getId()).remove(recruiter);
        gd.playerGraveyards.get(player1.getId()).add(recruiter.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    private void castRecruiter(UUID targetId) {
        harness.setHand(player1, List.of(new ReptilianRecruiter()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
