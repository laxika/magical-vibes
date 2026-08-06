package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InvasionOfInnistrad;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoulBurnTest extends BaseCardTest {

    @Test
    @DisplayName("X=3 all black on X: 3 damage and gain 3 life")
    void dealsDamageAndGainsLifeWithBlackOnX() {
        harness.setHand(player1, List.of(new SoulBurn()));
        // {X}{2}{B} with X=3 → 3B for X + 1B for {B} + 2 any = 6
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Life gain capped by black spent on X when red pays part of X")
    void lifeGainCappedByBlackSpentOnX() {
        harness.setHand(player1, List.of(new SoulBurn()));
        // Prefer BLACK for X: pay {B} first (1B), then X=4 prefers remaining 1B then 3R, generic from leftover.
        // 2B + 5R: after {B}, 1B+5R left → X=4 takes 1B+3R → blackOnX=1; generic 2 from R.
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 4, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16); // 4 damage
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21); // +1 life (only 1B on X)
    }

    @Test
    @DisplayName("Overkill on creature: life gain capped by toughness")
    void lifeGainCappedByToughness() {
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2
        harness.setHand(player1, List.of(new SoulBurn()));
        harness.addMana(player1, ManaColor.BLACK, 7); // X=4
        harness.setLife(player1, 20);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, 4, bearsId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        // 4 damage dealt, 4B on X, but toughness was 2 → gain 2
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Overkill on low-life player: life gain capped by life before damage")
    void lifeGainCappedByPlayerLife() {
        harness.setHand(player1, List.of(new SoulBurn()));
        harness.addMana(player1, ManaColor.BLACK, 8); // X=5
        harness.setLife(player1, 20);
        harness.setLife(player2, 3);

        harness.castSorcery(player1, 0, 5, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(-2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23); // +3, not +5
    }

    @Test
    @DisplayName("Cannot pay X with blue mana")
    void cannotPayXWithBlue() {
        harness.setHand(player1, List.of(new SoulBurn()));
        // {B} + {2} from blue, but X=2 needs B/R
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can pay X with red mana (no life if no black on X)")
    void canPayXWithOnlyRed() {
        harness.setHand(player1, List.of(new SoulBurn()));
        // {B} from black, X=3 and {2} from red
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20); // 0B on X → 0 life
    }

    /**
     * Damage to a battle removes defense counters (CR 120.3h) and marks nothing, so the damage
     * actually dealt has to be measured as the drop in defense — reading marked damage would report
     * zero and gain no life.
     */
    @Test
    @DisplayName("Damage to a battle gains life equal to the defense counters removed")
    void gainsLifeForDamageDealtToABattle() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfInnistrad());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        harness.setHand(player1, List.of(new SoulBurn()));
        harness.addMana(player1, ManaColor.BLACK, 6); // X=3
        harness.setLife(player1, 20);

        harness.castSorcery(player1, 0, 3, battle.getId());
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Cast at a land is rejected")
    void castAtLandIsRejected() {
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new SoulBurn()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        UUID plainsId = harness.getPermanentId(player2, "Plains");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, plainsId))
                .isInstanceOf(IllegalStateException.class);
    }
}
