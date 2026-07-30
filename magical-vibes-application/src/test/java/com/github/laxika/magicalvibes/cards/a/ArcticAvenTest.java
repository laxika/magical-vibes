package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArcticAvenTest extends BaseCardTest {

    @Test
    @DisplayName("Base 2/1 with no Plains controlled")
    void noBoostWithoutPlains() {
        harness.addToBattlefield(player1, new ArcticAven());
        harness.addToBattlefield(player1, new Island());

        Permanent aven = findPermanent(player1, "Arctic Aven");
        assertThat(gqs.getEffectivePower(gd, aven)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, aven)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets +1/+1 while controller controls a Plains")
    void boostWithPlains() {
        harness.addToBattlefield(player1, new ArcticAven());
        harness.addToBattlefield(player1, new Plains());

        Permanent aven = findPermanent(player1, "Arctic Aven");
        assertThat(gqs.getEffectivePower(gd, aven)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, aven)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost does not stack with multiple Plains")
    void boostDoesNotStack() {
        harness.addToBattlefield(player1, new ArcticAven());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());

        Permanent aven = findPermanent(player1, "Arctic Aven");
        assertThat(gqs.getEffectivePower(gd, aven)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, aven)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's Plains does not grant the boost")
    void opponentPlainsDoesNotCount() {
        harness.addToBattlefield(player1, new ArcticAven());
        harness.addToBattlefield(player2, new Plains());

        Permanent aven = findPermanent(player1, "Arctic Aven");
        assertThat(gqs.getEffectivePower(gd, aven)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, aven)).isEqualTo(1);
    }

    @Test
    @DisplayName("Loses the boost when the Plains leaves the battlefield")
    void losesBoostWhenPlainsLeaves() {
        harness.addToBattlefield(player1, new ArcticAven());
        harness.addToBattlefield(player1, new Plains());

        Permanent aven = findPermanent(player1, "Arctic Aven");
        assertThat(gqs.getEffectivePower(gd, aven)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Plains"));

        assertThat(gqs.getEffectivePower(gd, aven)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, aven)).isEqualTo(1);
    }

    @Test
    @DisplayName("{W} grants lifelink until end of turn")
    void activatedAbilityGrantsLifelink() {
        Permanent aven = addCreatureReady(player1, new ArcticAven());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(aven.getGrantedKeywords()).contains(Keyword.LIFELINK);
    }

    @Test
    @DisplayName("Lifelink wears off at end of turn")
    void lifelinkWearsOffAtEndOfTurn() {
        Permanent aven = addCreatureReady(player1, new ArcticAven());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(aven.getGrantedKeywords()).contains(Keyword.LIFELINK);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(aven.getGrantedKeywords()).doesNotContain(Keyword.LIFELINK);
    }
}
