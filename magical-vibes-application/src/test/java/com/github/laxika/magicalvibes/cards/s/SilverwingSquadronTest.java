package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilverwingSquadron.class, GrizzlyBears.class})
class SilverwingSquadronTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the number of creatures you control")
    void powerAndToughnessEqualControlledCreatures() {
        Permanent squadron = addCreatureReady(player1, new SilverwingSquadron());

        assertThat(gqs.getEffectivePower(gd, squadron)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, squadron)).isEqualTo(1);

        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, squadron)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, squadron)).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking creates one vigilant Knight token per opponent")
    void attackingCreatesKnightTokenForEachOpponent() {
        Permanent squadron = addCreatureReady(player1, new SilverwingSquadron());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        List<Permanent> knights = findPermanents(player1, "Knight").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(knights).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, knights.getFirst())).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, knights.getFirst())).isEqualTo(2);
        assertThat(knights.getFirst().getCard().getKeywords()).contains(Keyword.VIGILANCE);
        assertThat(knights.getFirst().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Each attacking Squadron creates a Knight and counts the new creatures")
    void multipleSquadronsEachTriggerAndGrow() {
        Permanent first = addCreatureReady(player1, new SilverwingSquadron());
        Permanent second = addCreatureReady(player1, new SilverwingSquadron());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Knight")).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opposing Squadron's attack creates tokens only for its controller")
    void opposingSquadronCreatesTokensForItsController() {
        Permanent defending = addCreatureReady(player1, new SilverwingSquadron());
        Permanent attacking = addCreatureReady(player2, new SilverwingSquadron());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Knight")).isZero();
        assertThat(countPermanents(player2, "Knight")).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, defending)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, defending)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, attacking)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacking)).isEqualTo(2);
    }
}
