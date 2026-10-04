package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.t.TimberpackWolf;
import com.github.laxika.magicalvibes.cards.t.TitanicGrowth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FungalSprouting.class, GrizzlyBears.class, HillGiant.class, Ornithopter.class,
        TimberpackWolf.class, TitanicGrowth.class})
class FungalSproutingTest extends BaseCardTest {

    private long saprolings(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Saproling"))
                .count();
    }

    @Test
    @DisplayName("Creates tokens equal to the greatest power among creatures you control")
    void createsTokensForGreatestPower() {
        harness.setHand(player1, List.of(new FungalSprouting()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(saprolings(player1)).isEqualTo(3);
    }

    @Test
    @DisplayName("Creates no tokens when you control no creatures")
    void createsNoTokensWithoutCreatures() {
        harness.setHand(player1, List.of(new FungalSprouting()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(saprolings(player1)).isZero();
    }

    @Test
    @DisplayName("Opponent's creatures do not count")
    void ignoresOpponentCreatures() {
        harness.setHand(player1, List.of(new FungalSprouting()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new HillGiant());

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(saprolings(player1)).isZero();
    }

    @Test
    @DisplayName("Saproling tokens are 1/1")
    void saprolingTokensArePowerOneToughnessOne() {
        harness.setHand(player1, List.of(new FungalSprouting()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Saproling"))
                .findFirst().orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Uses effective power at resolution after a pump spell resolves")
    void usesPowerAtResolution() {
        harness.setHand(player1, List.of(new FungalSprouting(), new TitanicGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.addToBattlefield(player1, new TimberpackWolf());

        harness.castSorcery(player1, 0, 0);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Timberpack Wolf"));
        harness.passBothPriorities();

        assertThat(saprolings(player1)).isEqualTo(6);
    }

    @Test
    @DisplayName("Tied greatest powers are not added together and include static bonuses")
    void usesGreatestPowerWithTiedCreatures() {
        harness.setHand(player1, List.of(new FungalSprouting()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addToBattlefield(player1, new TimberpackWolf());
        harness.addToBattlefield(player1, new TimberpackWolf());

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(saprolings(player1)).isEqualTo(3);
    }
}
