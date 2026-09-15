package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.m.ManaCylix;
import com.github.laxika.magicalvibes.cards.m.MeteorCrater;
import com.github.laxika.magicalvibes.cards.m.MoggJailer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RushingRiver.class, MoggJailer.class, MeteorCrater.class, ManaCylix.class})
class RushingRiverTest extends BaseCardTest {

    @Test
    void returnsOneTargetNonlandPermanentWithoutKicker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoggJailer());
        harness.setHand(player1, List.of(new RushingRiver()));
        addBaseMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mogg Jailer");
        harness.assertInHand(player2, "Mogg Jailer");
    }

    @Test
    void returnsTwoTargetNonlandPermanentsWhenKickedAndSacrificesLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MeteorCrater());
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new MoggJailer());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new MoggJailer());
        harness.setHand(player1, List.of(new RushingRiver()));
        addBaseMana();

        harness.getGameService().playCard(
                harness.getGameData(), player1, 0, 0, null, null,
                List.of(firstTarget.getId(), secondTarget.getId()), List.of(), false, land.getId(), null,
                null, null, null, true
        );
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Meteor Crater");
        harness.assertInGraveyard(player1, "Meteor Crater");
        harness.assertNotOnBattlefield(player2, "Mogg Jailer");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getId)
                .contains(firstTarget.getCard().getId(), secondTarget.getCard().getId());
    }

    @Test
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MeteorCrater());
        harness.setHand(player1, List.of(new RushingRiver()));
        addBaseMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    void returnsNonlandPermanentThatIsNotACreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ManaCylix());
        harness.setHand(player1, List.of(new RushingRiver()));
        addBaseMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mana Cylix");
        harness.assertInHand(player2, "Mana Cylix");
    }

    @Test
    void kickedCastRequiresAnotherTargetNonlandPermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MeteorCrater());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoggJailer());
        harness.setHand(player1, List.of(new RushingRiver()));
        addBaseMana();

        assertThatThrownBy(() -> harness.getGameService().playCard(
                harness.getGameData(), player1, 0, 0, null, null,
                List.of(target.getId(), target.getId()), List.of(), false, land.getId(), null,
                null, null, null, true))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotKickWithoutSacrificingLand() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new MoggJailer());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new MoggJailer());
        Permanent invalidSacrifice = harness.addToBattlefieldAndReturn(player1, new MoggJailer());
        harness.setHand(player1, List.of(new RushingRiver()));
        addBaseMana();

        assertThatThrownBy(() -> harness.getGameService().playCard(
                harness.getGameData(), player1, 0, 0, null, null,
                List.of(firstTarget.getId(), secondTarget.getId()), List.of(), false,
                invalidSacrifice.getId(), null, null, null, null, true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("a land");
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
