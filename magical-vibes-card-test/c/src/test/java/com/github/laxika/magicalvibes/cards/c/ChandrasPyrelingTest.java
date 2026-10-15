package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChandrasPyreling.class, Shock.class})
class ChandrasPyrelingTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 and double strike when your source deals noncombat damage to an opponent")
    void triggersOnControlledSourceNoncombatDamage() {
        harness.addToBattlefield(player1, new ChandrasPyreling());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        Permanent pyreling = findPermanent(player1, "Chandra's Pyreling");
        assertThat(pyreling.getPowerModifier()).isEqualTo(1);
        assertThat(pyreling.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not trigger when an opponent's source deals noncombat damage")
    void doesNotTriggerOnOpponentControlledSource() {
        harness.addToBattlefield(player1, new ChandrasPyreling());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Temporary boost and double strike wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new ChandrasPyreling());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        Permanent pyreling = findPermanent(player1, "Chandra's Pyreling");
        assertThat(pyreling.getPowerModifier()).isEqualTo(1);
        assertThat(pyreling.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();

        assertThat(pyreling.getPowerModifier()).isZero();
        assertThat(pyreling.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void repeatedDamageEventsGiveCumulativeBoosts() {
        Permanent pyreling = harness.addToBattlefieldAndReturn(player1, new ChandrasPyreling());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(pyreling.getPowerModifier()).isZero();
        assertThat(pyreling.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
        harness.passBothPriorities();
        assertThat(pyreling.getPowerModifier()).isEqualTo(1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(pyreling.getPowerModifier()).isEqualTo(2);
        assertThat(pyreling.getToughnessModifier()).isZero();
        assertThat(pyreling.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void damageToYourselfDoesNotTrigger() {
        Permanent pyreling = harness.addToBattlefieldAndReturn(player1, new ChandrasPyreling());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(pyreling.getPowerModifier()).isZero();
        assertThat(pyreling.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void damageToAnOpponentsCreatureDoesNotTrigger() {
        Permanent pyreling = harness.addToBattlefieldAndReturn(player1, new ChandrasPyreling());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandrasPyreling());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(pyreling.getPowerModifier()).isZero();
        assertThat(pyreling.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void combatDamageDoesNotTrigger() {
        Permanent pyreling = harness.addToBattlefieldAndReturn(player1, new ChandrasPyreling());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        pyreling.setSummoningSick(false);
        pyreling.setAttacking(true);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 19);
        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(pyreling.getPowerModifier()).isZero();
        assertThat(pyreling.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }
}
