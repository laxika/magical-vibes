package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VisageBandit.class, GrizzlyBears.class})
class VisageBanditTest extends BaseCardTest {

    @Test
    @DisplayName("May copy a creature you control and keeps the added Shapeshifter and Rogue types")
    void copiesCreatureYouControlWithAdditionalSubtypes() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        VisageBandit bandit = new VisageBandit();
        harness.setHand(player1, List.of(bandit));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());

        Permanent copied = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(bandit.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, copied)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, copied)).isEqualTo(2);
        assertThat(copied.getCard().getSubtypes())
                .contains(CardSubtype.BEAR, CardSubtype.SHAPESHIFTER, CardSubtype.ROGUE);
    }

    @Test
    @DisplayName("May decline copying")
    void mayDeclineCopy() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        VisageBandit bandit = new VisageBandit();
        harness.setHand(player1, List.of(bandit));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent entered = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(bandit.getId()))
                .findFirst().orElseThrow();
        assertThat(entered.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.SHAPESHIFTER, CardSubtype.ROGUE);
    }
}
