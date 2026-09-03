package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CircleOfProtectionBlack;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DrainLife.class, CircleOfProtectionBlack.class, GrizzlyBears.class, Plains.class})
class DrainLifeTest extends BaseCardTest {

    @Test
    @DisplayName("X=3 at a player deals 3 damage and controller gains 3 life")
    void drainsPlayer() {
        harness.setHand(player1, List.of(new DrainLife()));
        harness.addMana(player1, ManaColor.BLACK, 5); // {X}{1}{B}, X=3
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        harness.assertLife(player2, 17);
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("X=2 kills a 2/2 and controller gains 2 life")
    void killsCreatureAndGainsLife() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DrainLife()));
        harness.addMana(player1, ManaColor.BLACK, 4); // {2}{1}{B}
        harness.setLife(player1, 20);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, 2, bearsId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Cast at a land is rejected")
    void castAtLandIsRejected() {
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new DrainLife()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID plainsId = harness.getPermanentId(player2, "Plains");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, plainsId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Drain Life");
    }

    @Test
    @DisplayName("Life gain is capped by the target player's life total before damage")
    void lifeGainIsCappedByTargetPlayerLifeTotal() {
        harness.setHand(player1, List.of(new DrainLife()));
        harness.addMana(player1, ManaColor.BLACK, 7); // {X}{1}{B}, X=5
        harness.setLife(player1, 20);
        harness.setLife(player2, 3);

        harness.castAndResolveSorcery(player1, 0, 5, player2.getId());

        harness.assertLife(player2, -2);
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Life gain is capped by a creature's toughness")
    void lifeGainIsCappedByCreatureToughness() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DrainLife()));
        harness.addMana(player1, ManaColor.BLACK, 6); // {X}{1}{B}, X=4
        harness.setLife(player1, 20);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, 4, bearsId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Prevented damage does not produce life gain")
    void preventedDamageDoesNotProduceLifeGain() {
        Permanent circle = harness.addToBattlefieldAndReturn(player2, new CircleOfProtectionBlack());
        DrainLife drainLife = new DrainLife();
        harness.setHand(player1, List.of(drainLife));
        harness.addMana(player1, ManaColor.BLACK, 4); // {X}{1}{B}, X=2
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 2, player2.getId());
        int circleIndex = gd.playerBattlefields.get(player2.getId()).indexOf(circle);
        harness.activateAbility(player2, circleIndex, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, drainLife.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("X must be paid with black mana")
    void cannotPayXWithNonBlackMana() {
        harness.setHand(player1, List.of(new DrainLife()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
