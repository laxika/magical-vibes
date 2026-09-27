package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BugenhagenWiseElder.class, AvatarOfMight.class, ColossalDreadmaw.class})
class BugenhagenWiseElderTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card during your upkeep when you control a creature with power 7 or greater")
    void drawsWithCreaturePowerAtLeastSeven() {
        harness.addToBattlefield(player1, new BugenhagenWiseElder());
        harness.addToBattlefield(player1, new AvatarOfMight());
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    @DisplayName("Does not draw when your creatures all have power less than 7")
    void doesNotDrawBelowPowerThreshold() {
        harness.addToBattlefield(player1, new BugenhagenWiseElder());
        harness.addToBattlefield(player1, new ColossalDreadmaw());
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    @DisplayName("Does not draw from a creature with sufficient power controlled by an opponent")
    void doesNotDrawFromOpponentsCreature() {
        harness.addToBattlefield(player1, new BugenhagenWiseElder());
        harness.addToBattlefield(player2, new AvatarOfMight());
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    @DisplayName("Taps for one mana of any color")
    void tapsForAnyColor() {
        Permanent elder = harness.addToBattlefieldAndReturn(player1, new BugenhagenWiseElder());
        elder.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(elder.isTapped()).isTrue();
    }
}
