package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MothriderSamurai;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KentaroTheSmilingCatTest extends BaseCardTest {

    @Test
    @DisplayName("A Samurai spell can be cast for generic mana equal to its mana value")
    void samuraiSpellCastForGenericManaValue() {
        harness.addToBattlefield(player1, new KentaroTheSmilingCat());
        // Mothrider Samurai costs {3}{W} (mana value 4) — with Kentaro it can be cast for {4}
        harness.setHand(player1, List.of(new MothriderSamurai()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Mothrider Samurai");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("The alternative cost is not free — less generic mana than the mana value is not enough")
    void samuraiSpellNeedsFullManaValue() {
        harness.addToBattlefield(player1, new KentaroTheSmilingCat());
        harness.setHand(player1, List.of(new MothriderSamurai()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A non-Samurai spell gets no alternative cost")
    void nonSamuraiSpellUnaffected() {
        harness.addToBattlefield(player1, new KentaroTheSmilingCat());
        // Grizzly Bears costs {1}{G} — the green pip still has to be paid
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Without Kentaro, a Samurai spell cannot be cast for generic mana alone")
    void noAlternativeCostWithoutKentaro() {
        harness.setHand(player1, List.of(new MothriderSamurai()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's Samurai spells are not affected by your Kentaro")
    void opponentSamuraiSpellsUnaffected() {
        harness.addToBattlefield(player1, new KentaroTheSmilingCat());
        harness.setHand(player2, List.of(new MothriderSamurai()));
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("When Kentaro becomes blocked, it gets +1/+1 until end of turn")
    void becomesBlockedGetsBushidoBonus() {
        Permanent kentaro = addReady(player1, new KentaroTheSmilingCat());
        kentaro.setAttacking(true);
        addReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(kentaro.getPowerModifier()).isEqualTo(1);
        assertThat(kentaro.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Kentaro blocks, it gets +1/+1 until end of turn")
    void blocksGetsBushidoBonus() {
        Permanent attacker = addReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent kentaro = addReady(player2, new KentaroTheSmilingCat());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(kentaro.getPowerModifier()).isEqualTo(1);
        assertThat(kentaro.getToughnessModifier()).isEqualTo(1);
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
