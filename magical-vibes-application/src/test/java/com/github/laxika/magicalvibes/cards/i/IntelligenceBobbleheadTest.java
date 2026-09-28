package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(IntelligenceBobblehead.class)
class IntelligenceBobbleheadTest extends BaseCardTest {

    @Test
    void manaAbilityAddsChosenColor() {
        Permanent bobblehead = harness.addToBattlefieldAndReturn(player1, new IntelligenceBobblehead());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(bobblehead.isTapped()).isTrue();
    }

    @Test
    void drawsOneCardPerBobbleheadYouControl() {
        harness.addToBattlefield(player1, new IntelligenceBobblehead());
        harness.addToBattlefield(player1, new IntelligenceBobblehead());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new IntelligenceBobblehead(), new IntelligenceBobblehead()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
