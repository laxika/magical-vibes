package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SivvisRuse.class, GrizzlyBears.class, Mountain.class, Plains.class})
class SivvisRuseTest extends BaseCardTest {

    private static Card createCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    @Test
    @DisplayName("Can be cast for free when an opponent controls a Mountain and you control a Plains")
    void castsForFreeWithRequiredLands() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new SivvisRuse()));

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sivvi's Ruse");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot be cast for free when only the controller controls a Mountain")
    void cannotCastForFreeWithoutRequiredLands() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new SivvisRuse()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, (java.util.UUID) null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be cast for free without a Plains you control")
    void cannotCastForFreeWithoutControllerPlains() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new SivvisRuse()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, (java.util.UUID) null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be cast normally when the free-cast condition is not met")
    void castsNormallyWithoutRequiredLands() {
        harness.setHand(player1, List.of(new SivvisRuse()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sivvi's Ruse");
    }

    @Test
    @DisplayName("Prevents damage to creatures you control")
    void preventsDamageToControlledCreatures() {
        Permanent blocker = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SivvisRuse()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        Permanent attacker = addCreatureReady(player2, createCreature("Large Bear", 5, 5));
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not prevent damage to the controller")
    void doesNotPreventDamageToController() {
        harness.setHand(player1, List.of(new SivvisRuse()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        Permanent attacker = addCreatureReady(player2, createCreature("Large Bear", 5, 5));
        attacker.setAttacking(true);

        harness.setLife(player1, 20);
        resolveCombat(player2);

        harness.assertLife(player1, 15);
    }
}
