package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TuraKennerDSkyknight.class, Shock.class, Divination.class, GrizzlyBears.class})
class TuraKennerDSkyknightTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant creates a 1/1 white Soldier token")
    void instantCreatesSoldier() {
        harness.addToBattlefield(player1, new TuraKennerDSkyknight());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(1);
        Permanent token = findPermanent(player1, "Soldier");
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SOLDIER);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a sorcery creates a Soldier token")
    void sorceryCreatesSoldier() {
        harness.addToBattlefield(player1, new TuraKennerDSkyknight());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature spell does not create a Soldier token")
    void creatureSpellCreatesNoSoldier() {
        harness.addToBattlefield(player1, new TuraKennerDSkyknight());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(countPermanents(player1, "Soldier")).isZero();
    }

    @Test
    void opponentInstantCreatesNoSoldier() {
        harness.addToBattlefield(player1, new TuraKennerDSkyknight());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(countPermanents(player1, "Soldier")).isZero();
        assertThat(countPermanents(player2, "Soldier")).isZero();
    }

    @Test
    void eachInstantCreatesSoldierBeforeSpellResolves() {
        harness.addToBattlefield(player1, new TuraKennerDSkyknight());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.passBothPriorities();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    void illegalSpellTargetDoesNotPreventSoldierCreation() {
        harness.addToBattlefield(player1, new TuraKennerDSkyknight());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new Shock()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.castAndResolveInstant(player2, 0, bears.getId());
        assertThat(countPermanents(player2, "Grizzly Bears")).isZero();
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Soldier")).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Soldier")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
