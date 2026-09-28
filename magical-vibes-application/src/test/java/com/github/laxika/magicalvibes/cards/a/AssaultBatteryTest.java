package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.InvasionOfInnistrad;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AssaultBattery.class, AngelOfMercy.class, Forest.class, InvasionOfInnistrad.class})
class AssaultBatteryTest extends BaseCardTest {

    @Test
    @DisplayName("Assault deals 2 damage to a creature")
    void assaultDealsDamageToCreature() {
        harness.addToBattlefield(player2, new AngelOfMercy());
        harness.setHand(player1, List.of(new AssaultBattery()));
        harness.addMana(player1, ManaColor.RED, 1);

        castAssault(harness.getPermanentId(player2, "Angel of Mercy"));
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Angel of Mercy").getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Assault deals 2 damage to a player")
    void assaultDealsDamageToPlayer() {
        harness.setHand(player1, List.of(new AssaultBattery()));
        harness.addMana(player1, ManaColor.RED, 1);

        castAssault(player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Assault can target a battle")
    void assaultDealsDamageToBattle() {
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfInnistrad());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        harness.setHand(player1, List.of(new AssaultBattery()));
        harness.addMana(player1, ManaColor.RED, 1);

        castAssault(battle.getId());
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Battery creates a 3/3 green Elephant token for its own cost")
    void batteryCreatesElephantToken() {
        harness.setHand(player1, List.of(new AssaultBattery()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        List<Permanent> elephants = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Elephant"))
                .toList();
        assertThat(elephants).hasSize(1);
        assertThat(elephants.getFirst().getCard().getPower()).isEqualTo(3);
        assertThat(elephants.getFirst().getCard().getToughness()).isEqualTo(3);
        assertThat(elephants.getFirst().getCard().getColors()).contains(CardColor.GREEN);
        assertThat(elephants.getFirst().getCard().getSubtypes()).contains(CardSubtype.ELEPHANT);
    }

    @Test
    @DisplayName("Assault cannot target a land")
    void assaultCannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new AssaultBattery()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> castAssault(harness.getPermanentId(player2, "Forest")))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castAssault(java.util.UUID targetId) {
        harness.ensurePriority(player1);
        gs.playCard(gd, player1, 0, 0, targetId, null);
    }
}
