package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.Griptide;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PharikasCure.class, AirElemental.class, GrizzlyBears.class, Griptide.class})
class PharikasCureTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to a creature and controller gains 2 life")
    void dealsDamageAndGainsLife() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setLife(player1, 15);
        harness.setHand(player1, List.of(new PharikasCure()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, elemental.getId());
        harness.passBothPriorities();

        assertThat(elemental.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("2 damage destroys a 2-toughness creature and controller still gains 2 life")
    void destroysSmallCreatureAndGainsLife() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 15);
        harness.setHand(player1, List.of(new PharikasCure()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new PharikasCure()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Gains no life when the only target leaves the battlefield before resolution")
    void doesNotGainLifeWhenTargetLeavesBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 15);
        harness.setHand(player1, List.of(new PharikasCure()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, creature.getId());

        harness.setHand(player2, List.of(new Griptide()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(creature.getCard());
        harness.assertLife(player1, 15);
        harness.assertInGraveyard(player1, "Pharika's Cure");
    }

    @Test
    @DisplayName("Can target the controller's own creature and gains life above the starting total")
    void canTargetOwnCreatureAndGainLifeAboveStartingTotal() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new PharikasCure()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }
}
