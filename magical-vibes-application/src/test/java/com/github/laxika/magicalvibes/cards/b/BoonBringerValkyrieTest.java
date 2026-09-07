package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({BoonBringerValkyrie.class, GrizzlyBears.class})
class BoonBringerValkyrieTest extends BaseCardTest {

    @Test
    @DisplayName("Backup puts a counter on another creature and grants flying, first strike, and lifelink")
    void backsUpAnotherCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent valkyrie = castBoonBringerValkyrie();

        resolveEtbTargeting(bears);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getGrantedKeywords()).containsExactlyInAnyOrder(
                Keyword.FLYING, Keyword.FIRST_STRIKE, Keyword.LIFELINK);
        assertThat(valkyrie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Backup targeting the source puts on the counter but does not grant the abilities")
    void backingUpSourceDoesNotGrantAbilities() {
        Permanent valkyrie = castBoonBringerValkyrie();

        resolveEtbTargeting(valkyrie);

        assertThat(valkyrie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(valkyrie.getGrantedKeywords()).doesNotContain(
                Keyword.FLYING, Keyword.FIRST_STRIKE, Keyword.LIFELINK);
    }

    @Test
    @DisplayName("Backup's granted abilities expire at the end of the turn")
    void grantedAbilitiesExpireAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castBoonBringerValkyrie();
        resolveEtbTargeting(bears);

        assertThat(bears.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(bears.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(bears.hasKeyword(Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(bears.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(bears.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    private Permanent castBoonBringerValkyrie() {
        harness.setHand(player1, List.of(new BoonBringerValkyrie()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof BoonBringerValkyrie)
                .findFirst()
                .orElseThrow();
    }

    private void resolveEtbTargeting(Permanent target) {
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
