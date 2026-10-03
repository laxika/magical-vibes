package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        assertThat(gd.stack).isEmpty();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    @DisplayName("Does not draw from a creature with sufficient power controlled by an opponent")
    void doesNotDrawFromOpponentsCreature() {
        harness.addToBattlefield(player1, new BugenhagenWiseElder());
        harness.addToBattlefield(player2, new AvatarOfMight());
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    @DisplayName("Taps for one mana of any color")
    void tapsForAnyColor() {
        Permanent elder = addCreatureReady(player1, new BugenhagenWiseElder());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(elder.isTapped()).isTrue();
    }

    @Test
    void drawsWithExactlySevenEffectivePower() {
        harness.addToBattlefield(player1, new BugenhagenWiseElder());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AvatarOfMight());
        creature.setPowerModifier(-1);
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    void doesNotDrawIfPowerFallsBelowSevenBeforeResolution() {
        harness.addToBattlefield(player1, new BugenhagenWiseElder());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AvatarOfMight());
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        creature.setPowerModifier(-2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    void doesNotTriggerWhenPowerReachesSevenAfterUpkeepBegins() {
        harness.addToBattlefield(player1, new BugenhagenWiseElder());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ColossalDreadmaw());
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
        creature.setPowerModifier(1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new BugenhagenWiseElder());
        harness.addToBattlefield(player1, new AvatarOfMight());
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    void drawsOnlyOneCardWithMultipleQualifyingCreatures() {
        harness.addToBattlefield(player1, new BugenhagenWiseElder());
        harness.addToBattlefield(player1, new AvatarOfMight());
        harness.addToBattlefield(player1, new AvatarOfMight());
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    void cannotActivateTapAbilityWhileSummoningSick() {
        Permanent elder = harness.addToBattlefieldAndReturn(player1, new BugenhagenWiseElder());
        elder.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(elder.isTapped()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLACK", "RED", "GREEN"})
    void tapsForEachOtherColor(ManaColor color) {
        Permanent elder = addCreatureReady(player1, new BugenhagenWiseElder());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(elder.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void elderItselfCanSatisfyPowerCondition() {
        Permanent elder = harness.addToBattlefieldAndReturn(player1, new BugenhagenWiseElder());
        elder.setPowerModifier(6);
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }
}
