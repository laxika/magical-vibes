package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MeteorGolemTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys target nonland permanent an opponent controls")
    void etbDestroysTargetNonlandPermanentOpponentControls() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.setHand(player1, List.of(new MeteorGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        UUID targetId = harness.getPermanentId(player2, "Leonin Scimitar");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
        harness.assertOnBattlefield(player1, "Meteor Golem");
        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Cannot target a permanent controlled by its caster")
    void cannotTargetOwnNonlandPermanent() {
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.setHand(player1, List.of(new MeteorGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        UUID targetId = harness.getPermanentId(player1, "Leonin Scimitar");

        assertThatThrownBy(() -> harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Cannot target an opponent's land")
    void cannotTargetOpponentLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new MeteorGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        UUID targetId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland");
    }

    @Test
    @DisplayName("ETB does not trigger when there is no legal target")
    void etbDoesNotTriggerWithoutLegalTarget() {
        harness.setHand(player1, List.of(new MeteorGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Meteor Golem");
    }
}
