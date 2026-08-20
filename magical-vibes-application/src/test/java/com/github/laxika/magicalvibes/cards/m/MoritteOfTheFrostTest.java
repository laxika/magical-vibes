package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MoritteOfTheFrostTest extends BaseCardTest {

    @Test
    @DisplayName("copies a creature with legendary, snow, changeling, and two counters")
    void copiesCreatureWithCopyExceptions() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castMoritte();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.handlePermanentChosen(player1, targetId);

        Permanent copy = morittePermanent();
        assertThat(copy.getCard().getSupertypes())
                .contains(CardSupertype.LEGENDARY, CardSupertype.SNOW);
        assertThat(copy.getCard().getKeywords()).contains(Keyword.CHANGELING);
        assertThat(copy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(copy.getEffectivePower()).isEqualTo(4);
        assertThat(copy.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("does not add creature-only copy exceptions to a noncreature copy")
    void copiesNoncreatureWithoutCreatureOnlyExceptions() {
        harness.addToBattlefield(player1, new DarksteelRelic());
        castMoritte();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        UUID targetId = harness.getPermanentId(player1, "Darksteel Relic");
        harness.handlePermanentChosen(player1, targetId);

        Permanent copy = morittePermanent();
        assertThat(copy.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(copy.getCard().hasType(CardType.CREATURE)).isFalse();
        assertThat(copy.getCard().getSupertypes())
                .contains(CardSupertype.LEGENDARY, CardSupertype.SNOW);
        assertThat(copy.getCard().getKeywords()).doesNotContain(Keyword.CHANGELING);
        assertThat(copy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("can copy only a permanent controlled by its controller")
    void cannotCopyOpponentPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castMoritte();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Moritte of the Frost");
        harness.assertInGraveyard(player1, "Moritte of the Frost");
    }

    private void castMoritte() {
        harness.setHand(player1, List.of(new MoritteOfTheFrost()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
    }

    private Permanent morittePermanent() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getName().equals("Moritte of the Frost"))
                .findFirst()
                .orElseThrow();
    }
}
