package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ValiantKnight.class, BenalishKnight.class, GrizzlyBears.class})
class ValiantKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Other Knights you control get +1/+1")
    void otherKnightGetsAnthem() {
        harness.addToBattlefield(player1, new ValiantKnight());
        harness.addToBattlefield(player1, new BenalishKnight());

        Permanent knight = findPermanent(player1, "Benalish Knight");
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not boost itself")
    void doesNotBoostItself() {
        harness.addToBattlefield(player1, new ValiantKnight());

        Permanent self = findPermanent(player1, "Valiant Knight");
        assertThat(gqs.getEffectivePower(gd, self)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, self)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not boost non-Knights or opponent Knights")
    void doesNotBoostOthers() {
        harness.addToBattlefield(player1, new ValiantKnight());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new BenalishKnight());

        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Grizzly Bears"))).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, findPermanent(player2, "Benalish Knight"))).isEqualTo(2);
    }

    @Test
    @DisplayName("Activated ability gives double strike to your Knights, including itself")
    void abilityGrantsDoubleStrike() {
        harness.addToBattlefield(player1, new ValiantKnight());
        harness.addToBattlefield(player1, new BenalishKnight());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new BenalishKnight());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Valiant Knight"), Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Benalish Knight"), Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Grizzly Bears"), Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Benalish Knight"), Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Multiple Valiant Knights boost each other and their anthem ends when they leave")
    void multipleKnightsBoostEachOther() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ValiantKnight());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ValiantKnight());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).remove(first);

        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
    }

    @Test
    @DisplayName("The ability affects Knights present at resolution, but not Knights entering afterward")
    void affectedKnightsAreDeterminedAtResolution() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ValiantKnight());
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.activateAbility(player1, 0, null, null);

        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new ValiantKnight());
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.DOUBLE_STRIKE)).isFalse();
        harness.passBothPriorities();

        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new ValiantKnight());
        assertThat(gqs.hasKeyword(gd, source, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The activated ability resolves after its source leaves the battlefield")
    void abilityResolvesWithoutSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ValiantKnight());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new ValiantKnight());
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.activateAbility(player1, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).remove(source);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, other, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Double strike expires at end of turn while the anthem remains")
    void doubleStrikeExpiresAtEndOfTurn() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ValiantKnight());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new ValiantKnight());
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, source, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.hasKeyword(gd, source, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(5);
    }
}
