package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlameJab.class, GrizzlyBears.class, Mountain.class, ChandraNalaar.class})
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
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FlameJab()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears") && p.getMarkedDamage() == 1);
    }

    @Test
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

    @Test
    @DisplayName("Retrace can reuse the same Flame Jab after it resolves")
    void retraceCanBeRepeated() {
        FlameJab jab = new FlameJab();
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(jab));
        harness.setHand(player1, List.of(new Mountain(), new Mountain()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castRetrace(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        int jabIndex = gd.playerGraveyards.get(player1.getId()).indexOf(jab);
        assertThat(jabIndex).isGreaterThanOrEqualTo(0);
        harness.castRetrace(player1, jabIndex, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(jab).hasSize(3);
    }

    @Test
    @DisplayName("Retrace still requires red mana and does not discard a land on a rejected cast")
    void retraceRequiresMana() {
        harness.setGraveyard(player1, List.of(new FlameJab()));
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Mountain");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Flame Jab");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Retrace does not allow Flame Jab to be cast outside a main phase")
    void retraceRequiresSorceryTiming() {
        harness.setGraveyard(player1, List.of(new FlameJab()));
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.RED, 1);
        gd.currentStep = TurnStep.UPKEEP;

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Mountain");
        harness.assertInGraveyard(player1, "Flame Jab");
        assertThat(gd.stack).isEmpty();
    }
}
