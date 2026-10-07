package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.q.QuilledWolf;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Tenacity.class, QuilledWolf.class, Forest.class})
class TenacityTest extends BaseCardTest {

    @Test
    @DisplayName("Tenacity untaps, boosts, and grants lifelink to your creatures")
    void untapsBoostsAndGrantsLifelinkToOwnCreatures() {
        Permanent mine = addCreatureReady(player1, new QuilledWolf());
        mine.tap();
        Permanent theirs = addCreatureReady(player2, new QuilledWolf());
        theirs.tap();

        castTenacity();

        assertThat(mine.isTapped()).isFalse();
        assertThat(mine.getEffectivePower()).isEqualTo(3);
        assertThat(mine.getEffectiveToughness()).isEqualTo(3);
        assertThat(mine.hasKeyword(Keyword.LIFELINK)).isTrue();
        assertThat(theirs.isTapped()).isTrue();
        assertThat(theirs.getEffectivePower()).isEqualTo(2);
        assertThat(theirs.getEffectiveToughness()).isEqualTo(2);
        assertThat(theirs.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Tenacity's lifelink gains life from combat damage")
    void lifelinkGainsLifeFromCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new QuilledWolf());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castTenacity();
        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(attacker.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Tenacity's effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new QuilledWolf());

        castTenacity();
        assertThat(creature.getEffectivePower()).isEqualTo(3);
        assertThat(creature.getEffectiveToughness()).isEqualTo(3);
        assertThat(creature.hasKeyword(Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
        assertThat(creature.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Tenacity affects every creature, including untapped and summoning-sick creatures")
    void affectsAllOwnCreatures() {
        Permanent tapped = addCreatureReady(player1, new QuilledWolf());
        tapped.tap();
        Permanent untapped = addCreatureReady(player1, new QuilledWolf());
        Permanent summoningSick = harness.addToBattlefieldAndReturn(player1, new QuilledWolf());
        summoningSick.setSummoningSick(true);
        summoningSick.tap();

        castTenacity();

        for (Permanent creature : List.of(tapped, untapped, summoningSick)) {
            assertThat(creature.isTapped()).isFalse();
            assertThat(creature.getEffectivePower()).isEqualTo(3);
            assertThat(creature.getEffectiveToughness()).isEqualTo(3);
            assertThat(creature.hasKeyword(Keyword.LIFELINK)).isTrue();
        }
        assertThat(summoningSick.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Tenacity does not affect noncreature permanents")
    void doesNotAffectNoncreaturePermanents() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.tap();
        addCreatureReady(player1, new QuilledWolf());

        castTenacity();

        assertThat(land.isTapped()).isTrue();
        assertThat(land.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Tenacity does not affect creatures entering after resolution")
    void doesNotAffectCreaturesEnteringLater() {
        Permanent original = addCreatureReady(player1, new QuilledWolf());
        castTenacity();

        harness.castFromHand(player1, new QuilledWolf(), "{1}{G}");
        harness.passBothPriorities();
        Permanent later = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(original.getId()))
                .findFirst().orElseThrow();

        assertThat(later.getEffectivePower()).isEqualTo(2);
        assertThat(later.getEffectiveToughness()).isEqualTo(2);
        assertThat(later.hasKeyword(Keyword.LIFELINK)).isFalse();
        assertThat(original.getEffectivePower()).isEqualTo(3);
        assertThat(original.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Tenacity resolves with no creatures controlled")
    void resolvesWithNoCreatures() {
        Permanent opponent = addCreatureReady(player2, new QuilledWolf());
        opponent.tap();

        castTenacity();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Tenacity");
        assertThat(opponent.isTapped()).isTrue();
        assertThat(opponent.getEffectivePower()).isEqualTo(2);
        assertThat(opponent.getEffectiveToughness()).isEqualTo(2);
        assertThat(opponent.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    private void castTenacity() {
        harness.castFromHand(player1, new Tenacity(), "{3}{W}");
        harness.passBothPriorities();
    }
}
