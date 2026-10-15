package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(VintaraElephant.class)
class VintaraElephantTest extends BaseCardTest {

    @Test
    void anyPlayerMayPayToRemoveTrample() {
        Permanent elephant = harness.addToBattlefieldAndReturn(player1, new VintaraElephant());
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, elephant, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void lostTrampleReturnsAtEndOfTurn() {
        Permanent elephant = harness.addToBattlefieldAndReturn(player1, new VintaraElephant());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.passUntil(TurnStep.END_STEP);
        assertThat(gqs.hasKeyword(gd, elephant, Keyword.TRAMPLE)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, elephant, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void trampleIsRemovedOnlyWhenAbilityResolvesAndOnlyFromItsSource() {
        Permanent elephant = harness.addToBattlefieldAndReturn(player1, new VintaraElephant());
        Permanent otherElephant = harness.addToBattlefieldAndReturn(player1, new VintaraElephant());
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.activateAbility(player2, 0, null, null);

        assertThat(gqs.hasKeyword(gd, elephant, Keyword.TRAMPLE)).isTrue();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, elephant, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherElephant, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void opponentCannotUseControllersManaToPayForAbility() {
        Permanent elephant = harness.addToBattlefieldAndReturn(player1, new VintaraElephant());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gqs.hasKeyword(gd, elephant, Keyword.TRAMPLE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityCanBeActivatedAgainAfterTrampleIsLost() {
        Permanent elephant = harness.addToBattlefieldAndReturn(player1, new VintaraElephant());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, elephant, Keyword.TRAMPLE)).isFalse();

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, elephant, Keyword.TRAMPLE)).isFalse();
    }
}
