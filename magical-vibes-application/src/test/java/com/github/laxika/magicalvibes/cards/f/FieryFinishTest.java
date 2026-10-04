package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.Gigantosaurus;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FieryFinish.class, GrizzlyBears.class, LeoninScimitar.class, Gigantosaurus.class})
class FieryFinishTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 7 damage to target creature, destroying it")
    void dealsSevenDamageToTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FieryFinish()));
        harness.addMana(player1, ManaColor.RED, 6);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Fiery Finish");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.setHand(player1, List.of(new FieryFinish()));
        harness.addMana(player1, ManaColor.RED, 6);

        UUID targetId = harness.getPermanentId(player2, "Leonin Scimitar");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Deals exactly 7 damage to a creature that survives")
    void dealsExactlySevenDamage() {
        harness.addToBattlefield(player2, new Gigantosaurus());
        harness.setHand(player1, List.of(new FieryFinish()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Gigantosaurus"));

        harness.assertOnBattlefield(player2, "Gigantosaurus");
        assertThat(findPermanent(player2, "Gigantosaurus").getMarkedDamage()).isEqualTo(7);
        harness.assertInGraveyard(player1, "Fiery Finish");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can target a creature its caster controls")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new Gigantosaurus());
        harness.setHand(player1, List.of(new FieryFinish()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player1, "Gigantosaurus"));

        harness.assertOnBattlefield(player1, "Gigantosaurus");
        assertThat(findPermanent(player1, "Gigantosaurus").getMarkedDamage()).isEqualTo(7);
        harness.assertInGraveyard(player1, "Fiery Finish");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new FieryFinish()));
        harness.addMana(player1, ManaColor.RED, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player2, 20);
        harness.assertInHand(player1, "Fiery Finish");
    }
}
