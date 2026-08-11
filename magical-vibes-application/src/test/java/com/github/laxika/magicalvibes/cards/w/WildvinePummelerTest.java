package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WildvinePummelerTest extends BaseCardTest {

    @Test
    @DisplayName("Costs one less for one distinct color among permanents you control")
    void costsOneLessForOneColor() {
        addPermanent(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WildvinePummeler()));
        addManaForGenericCost(player1, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Counts each controlled color only once")
    void countsEachColorOnlyOnce() {
        addPermanent(player1, new GrizzlyBears());
        addPermanent(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WildvinePummeler()));
        addManaForGenericCost(player1, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Does not count colors among permanents controlled by an opponent")
    void ignoresOpponentsColors() {
        addPermanent(player2, new RagingGoblin());
        harness.setHand(player1, List.of(new WildvinePummeler()));
        addManaForGenericCost(player1, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private Permanent addPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addManaForGenericCost(Player player, int amount) {
        harness.addMana(player, ManaColor.COLORLESS, amount);
    }
}
