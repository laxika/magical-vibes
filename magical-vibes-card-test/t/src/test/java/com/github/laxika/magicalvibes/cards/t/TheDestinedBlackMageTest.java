package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BoggartBrute;
import com.github.laxika.magicalvibes.cards.f.FaerieMiscreant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheDestinedBlackMage.class, BoggartBrute.class, FaerieMiscreant.class,
        GrizzlyBears.class, Shock.class, SoulWarden.class})
class TheDestinedBlackMageTest extends BaseCardTest {

    @Test
    @DisplayName("The activated ability grants another creature you control deathtouch until end of turn")
    void activatedAbilityGrantsDeathtouchUntilEndOfTurn() {
        harness.addToBattlefield(player1, new TheDestinedBlackMage());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void activatedAbilityCannotTargetThisCreatureOrAnOpposingCreature() {
        Permanent mage = harness.addToBattlefieldAndReturn(player1, new TheDestinedBlackMage());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, mage.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casting a noncreature spell deals 1 damage to each opponent without a full party")
    void noncreatureSpellDealsOneDamageWithoutFullParty() {
        harness.addToBattlefield(player1, new TheDestinedBlackMage());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("A full party makes the noncreature spell trigger deal 3 damage to each opponent")
    void fullPartyMakesNoncreatureSpellDealThreeDamage() {
        harness.addToBattlefield(player1, new TheDestinedBlackMage());
        harness.addToBattlefield(player1, new SoulWarden());
        harness.addToBattlefield(player1, new BoggartBrute());
        harness.addToBattlefield(player1, new FaerieMiscreant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger the damage ability")
    void creatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new TheDestinedBlackMage());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
