package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

@CardUsed({ArcticAven.class, Island.class, Plains.class})
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

    @Test
    @DisplayName("Lifelink gains life from combat damage without controlling a Plains")
    void lifelinkGainsLifeWithoutPlains() {
        addCreatureReady(player1, new ArcticAven());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Repeated lifelink activations do not multiply life gained from boosted damage")
    void repeatedLifelinkDoesNotMultiplyLifeGain() {
        addCreatureReady(player1, new ArcticAven());
        harness.addToBattlefield(player1, new Plains());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Lifelink can be activated while summoning sick and affects only its source")
    void lifelinkCanBeActivatedWhileSummoningSick() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ArcticAven());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new ArcticAven());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, source, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.LIFELINK)).isFalse();
        assertThat(source.isTapped()).isFalse();
    }
}
