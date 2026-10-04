package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AncestralReminiscence;
import com.github.laxika.magicalvibes.cards.d.DeadWeight;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.t.TithingBlade;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EchoOfDusk.class, AncestralReminiscence.class, DeadWeight.class, Swamp.class, TithingBlade.class})
class EchoOfDuskTest extends BaseCardTest {

    @Test
    @DisplayName("Does not get the bonus without four permanent cards in its controller's graveyard")
    void noBonusBelowThreshold() {
        Permanent echo = addEcho();
        harness.setGraveyard(player1, List.of(
                new EchoOfDusk(), new EchoOfDusk(), new EchoOfDusk(), new AncestralReminiscence()));

        assertBaseStatsAndNoLifelink(echo);
    }

    @Test
    @DisplayName("Gets +1/+1 and lifelink with four permanent cards in its controller's graveyard")
    void getsBonusAtThreshold() {
        Permanent echo = addEcho();
        harness.setGraveyard(player1, List.of(
                new EchoOfDusk(), new EchoOfDusk(), new EchoOfDusk(), new EchoOfDusk()));

        assertThat(gqs.getEffectivePower(gd, echo)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, echo)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, echo, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Loses the bonus when its controller's graveyard drops below four permanent cards")
    void losesBonusBelowThreshold() {
        Permanent echo = addEcho();
        harness.setGraveyard(player1, List.of(
                new EchoOfDusk(), new EchoOfDusk(), new EchoOfDusk(), new EchoOfDusk()));
        assertThat(gqs.hasKeyword(gd, echo, Keyword.LIFELINK)).isTrue();

        gd.playerGraveyards.get(player1.getId()).removeFirst();

        assertBaseStatsAndNoLifelink(echo);
    }

    @Test
    @DisplayName("Counts lands, artifacts, and enchantments as permanent cards")
    void countsNoncreaturePermanents() {
        Permanent echo = addEcho();
        harness.setGraveyard(player1, List.of(
                new EchoOfDusk(), new Swamp(), new TithingBlade(), new DeadWeight()));

        assertThat(gqs.getEffectivePower(gd, echo)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, echo)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, echo, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Does not count permanent cards in the opponent's graveyard")
    void ignoresOpponentsGraveyard() {
        Permanent echo = addEcho();
        harness.setGraveyard(player1, List.of(new Swamp(), new Swamp(), new Swamp()));
        harness.setGraveyard(player2, List.of(new Swamp(), new Swamp(), new Swamp(), new Swamp()));

        assertBaseStatsAndNoLifelink(echo);
    }

    @Test
    @DisplayName("Uses the new controller's graveyard after changing control")
    void checksCurrentControllersGraveyard() {
        Permanent echo = addEcho();
        harness.setGraveyard(player1, List.of(new Swamp(), new Swamp(), new Swamp(), new Swamp()));
        assertThat(gqs.hasKeyword(gd, echo, Keyword.LIFELINK)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(echo);
        gd.playerBattlefields.get(player2.getId()).add(echo);

        assertBaseStatsAndNoLifelink(echo);

        harness.setGraveyard(player2, List.of(
                new Swamp(), new Swamp(), new Swamp(), new Swamp(), new Swamp()));

        assertThat(gqs.getEffectivePower(gd, echo)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, echo)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, echo, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Gains life from combat damage while descend 4 is satisfied")
    void lifelinkGainsLifeFromCombatDamage() {
        addCreatureReady(player1, new EchoOfDusk());
        harness.setGraveyard(player1, List.of(new Swamp(), new Swamp(), new Swamp(), new Swamp()));
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 13);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Does not gain life from combat damage below descend 4")
    void noLifelinkBelowThresholdInCombat() {
        addCreatureReady(player1, new EchoOfDusk());
        harness.setGraveyard(player1, List.of(new Swamp(), new Swamp(), new Swamp()));
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 18);
    }

    private Permanent addEcho() {
        return harness.addToBattlefieldAndReturn(player1, new EchoOfDusk());
    }

    private void assertBaseStatsAndNoLifelink(Permanent echo) {
        assertThat(gqs.getEffectivePower(gd, echo)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, echo)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, echo, Keyword.LIFELINK)).isFalse();
    }
}
