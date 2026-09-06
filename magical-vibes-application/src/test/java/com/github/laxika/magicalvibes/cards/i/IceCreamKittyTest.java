package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WilyGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IceCreamKitty.class, GrizzlyBears.class, WilyGoblin.class})
class IceCreamKittyTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature draws a card")
    void sacrificesAnotherCreatureToDraw() {
        Permanent kitty = harness.addToBattlefieldAndReturn(player1, new IceCreamKitty());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(kitty), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kitty).doesNotContain(bears);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrificing another token draws a card")
    void sacrificesAnotherTokenToDraw() {
        Permanent kitty = harness.addToBattlefieldAndReturn(player1, new IceCreamKitty());
        harness.setHand(player1, List.of(new WilyGoblin()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent treasure = findPermanent(player1, "Treasure");
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(kitty), 0, null, null);
        harness.handlePermanentChosen(player1, treasure.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kitty);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Tapping and sacrificing itself gains 3 life")
    void sacrificesItselfToGainLife() {
        Permanent kitty = harness.addToBattlefieldAndReturn(player1, new IceCreamKitty());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(kitty), 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        harness.assertNotOnBattlefield(player1, "Ice Cream Kitty");
        harness.assertInGraveyard(player1, "Ice Cream Kitty");
    }

    @Test
    @DisplayName("The draw ability can only be activated at sorcery speed")
    void drawAbilityIsSorcerySpeedOnly() {
        Permanent kitty = harness.addToBattlefieldAndReturn(player1, new IceCreamKitty());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(kitty), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
