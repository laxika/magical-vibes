package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BladeTribeBerserkers.class, Memnite.class})
class BladeTribeBerserkersTest extends BaseCardTest {

    @Test
    @DisplayName("ETB triggers when metalcraft is met (3+ artifacts)")
    void etbTriggersWithMetalcraft() {
        setupMetalcraft();
        castBerserkers();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(findBerserkers().getId());
    }

    @Test
    @DisplayName("ETB resolves: gets +3/+3 and haste until end of turn")
    void etbGrantsBoostAndHaste() {
        setupMetalcraft();
        castBerserkers();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent berserkers = findBerserkers();

        assertThat(berserkers.getPowerModifier()).isEqualTo(3);
        assertThat(berserkers.getToughnessModifier()).isEqualTo(3);
        assertThat(berserkers.getGrantedKeywords()).contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("ETB does NOT trigger without metalcraft (0 artifacts)")
    void etbDoesNotTriggerWithoutMetalcraft() {
        castBerserkers();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).isEmpty();

        harness.assertOnBattlefield(player1, "Blade-Tribe Berserkers");

        Permanent berserkers = findBerserkers();
        assertThat(berserkers.getPowerModifier()).isEqualTo(0);
        assertThat(berserkers.getToughnessModifier()).isEqualTo(0);
        assertThat(berserkers.getGrantedKeywords()).doesNotContain(Keyword.HASTE);
    }

    @Test
    @DisplayName("ETB does NOT trigger with only 2 artifacts")
    void etbDoesNotTriggerWithTwoArtifacts() {
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());

        castBerserkers();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).isEmpty();

        Permanent berserkers = findBerserkers();
        assertThat(berserkers.getPowerModifier()).isEqualTo(0);
        assertThat(berserkers.getToughnessModifier()).isEqualTo(0);
        assertThat(berserkers.getGrantedKeywords()).doesNotContain(Keyword.HASTE);
    }

    @Test
    @DisplayName("Opponent's artifacts don't count for metalcraft")
    void opponentArtifactsDontCount() {
        harness.addToBattlefield(player2, new Memnite());
        harness.addToBattlefield(player2, new Memnite());
        harness.addToBattlefield(player2, new Memnite());

        castBerserkers();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).isEmpty();

        Permanent berserkers = findBerserkers();
        assertThat(berserkers.getPowerModifier()).isEqualTo(0);
        assertThat(berserkers.getGrantedKeywords()).doesNotContain(Keyword.HASTE);
    }

    @Test
    @DisplayName("ETB does nothing if metalcraft is lost before resolution")
    void etbDoesNothingWhenMetalcraftLost() {
        setupMetalcraft();
        castBerserkers();
        harness.passBothPriorities(); // resolve creature spell - ETB trigger on stack

        // Remove artifacts before ETB resolves
        gd.playerBattlefields.get(player1.getId()).removeIf(
                p -> p.getCard().getName().equals("Memnite"));

        harness.passBothPriorities(); // resolve ETB trigger - metalcraft no longer met

        Permanent berserkers = findBerserkers();
        assertThat(berserkers.getPowerModifier()).isEqualTo(0);
        assertThat(berserkers.getToughnessModifier()).isEqualTo(0);
        assertThat(berserkers.getGrantedKeywords()).doesNotContain(Keyword.HASTE);
    }

    @Test
    @DisplayName("Bonus persists after an artifact is removed post-resolution")
    void bonusPersistsAfterArtifactRemoval() {
        setupMetalcraft();
        castBerserkers();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Remove an artifact after resolution
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Memnite"));

        Permanent berserkers = findBerserkers();
        assertThat(berserkers.getPowerModifier()).isEqualTo(3);
        assertThat(berserkers.getToughnessModifier()).isEqualTo(3);
        assertThat(berserkers.getGrantedKeywords()).contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("Creature enters battlefield regardless of metalcraft")
    void creatureEntersWithoutMetalcraft() {
        castBerserkers();
        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Blade-Tribe Berserkers");
    }

    @Test
    @DisplayName("Stack is empty after full resolution with metalcraft")
    void stackEmptyAfterResolution() {
        setupMetalcraft();
        castBerserkers();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Boost and haste expire at cleanup even while metalcraft remains met")
    void bonusExpiresAtCleanup() {
        setupMetalcraft();
        castBerserkers();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent berserkers = findBerserkers();
        assertThat(berserkers.getPowerModifier()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, berserkers, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(berserkers.getPowerModifier()).isZero();
        assertThat(berserkers.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, berserkers, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Gaining metalcraft after entry does not create a trigger retroactively")
    void metalcraftGainedAfterEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());
        castBerserkers();
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new Memnite());

        assertThat(gd.stack).isEmpty();
        Permanent berserkers = findBerserkers();
        assertThat(berserkers.getPowerModifier()).isZero();
        assertThat(berserkers.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, berserkers, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Metalcraft can be lost and regained before the trigger resolves")
    void metalcraftRegainedBeforeResolutionGrantsBonus() {
        setupMetalcraft();
        castBerserkers();
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Memnite"));
        harness.addToBattlefield(player1, new Memnite());
        harness.passBothPriorities();

        Permanent berserkers = findBerserkers();
        assertThat(berserkers.getPowerModifier()).isEqualTo(3);
        assertThat(berserkers.getToughnessModifier()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, berserkers, Keyword.HASTE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Metalcraft is checked at entry rather than when the creature spell is cast")
    void metalcraftGainedBeforeEntryTriggers() {
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());
        castBerserkers();
        harness.addToBattlefield(player1, new Memnite());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        Permanent berserkers = findBerserkers();
        assertThat(berserkers.getPowerModifier()).isEqualTo(3);
        assertThat(berserkers.getToughnessModifier()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, berserkers, Keyword.HASTE)).isTrue();
    }

    private void setupMetalcraft() {
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());
    }

    private void castBerserkers() {
        harness.castFromHand(player1, new BladeTribeBerserkers(), "{3}{R}");
    }

    private Permanent findBerserkers() {
        return findPermanent(player1, "Blade-Tribe Berserkers");
    }
}
