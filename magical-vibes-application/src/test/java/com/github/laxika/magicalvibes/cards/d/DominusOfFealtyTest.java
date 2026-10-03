package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GuardianBeast;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DominusOfFealty.class, GrizzlyBears.class, GuardianBeast.class, LeoninScimitar.class})
class DominusOfFealtyTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the upkeep trigger gains control of, untaps, and hastes the target")
    void acceptGainsControlUntapsAndHastes() {
        addCreatureReady(player1, new DominusOfFealty());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isTrue();
    }

    @Test
    @DisplayName("Declining the upkeep trigger leaves the target untouched")
    void declineDoesNothing() {
        addCreatureReady(player1, new DominusOfFealty());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.isTapped()).isTrue();
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Control and haste revert at end of turn")
    void controlAndHasteExpireAtCleanup() {
        addCreatureReady(player1, new DominusOfFealty());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
    }

    @Test
    @DisplayName("Can target a noncreature permanent")
    void canTargetNoncreaturePermanent() {
        addCreatureReady(player1, new DominusOfFealty());
        Permanent artifact = addCreatureReady(player2, new LeoninScimitar());
        artifact.tap();

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(artifact.getId()));
        assertThat(artifact.isTapped()).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(artifact.getId())).isTrue();
    }

    @Test
    void canUntapAndGrantHasteToOwnPermanent() {
        addCreatureReady(player1, new DominusOfFealty());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        target.tap();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        addCreatureReady(player1, new DominusOfFealty());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void abilityResolvesAfterDominusLeavesBattlefield() {
        Permanent dominus = addCreatureReady(player1, new DominusOfFealty());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(dominus);
        gd.playerGraveyards.get(player1.getId()).add(dominus.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    void cannotUntapOrGrantHasteWhenControlGainIsProhibited() {
        addCreatureReady(player1, new DominusOfFealty());
        addCreatureReady(player2, new GuardianBeast());
        Permanent target = addCreatureReady(player2, new LeoninScimitar());
        target.tap();

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }
}
