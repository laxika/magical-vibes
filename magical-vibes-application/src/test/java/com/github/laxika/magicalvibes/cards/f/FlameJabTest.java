package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlameJab.class, GrizzlyBears.class, Mountain.class})
class FlameJabTest extends BaseCardTest {

    @Test
    @DisplayName("Flame Jab deals 1 damage to target player")
    void deals1DamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new FlameJab()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Flame Jab deals 1 damage to target creature")
    void deals1DamageToCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FlameJab()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, 0, targetId);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears") && p.getMarkedDamage() == 1);
    }

    @Test
    @CardUsed(ChandraNalaar.class)
    @DisplayName("Flame Jab deals 1 damage to target planeswalker")
    void deals1DamageToPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 2);
        harness.setHand(player1, List.of(new FlameJab()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Retrace lets Flame Jab be recast from the graveyard by discarding a land, returning to graveyard")
    void retraceDiscardsLandAndDeals() {
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(new FlameJab()));
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castRetrace(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Mountain");
        harness.assertInGraveyard(player1, "Flame Jab");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Flame Jab"));
    }

    @Test
    @DisplayName("Retrace requires discarding a land card")
    void retraceRequiresLandDiscard() {
        harness.setGraveyard(player1, List.of(new FlameJab()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
