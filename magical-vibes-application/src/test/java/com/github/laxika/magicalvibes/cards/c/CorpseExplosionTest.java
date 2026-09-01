package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({CorpseExplosion.class, ChandraNalaar.class, GrizzlyBears.class, HillGiant.class})
class CorpseExplosionTest extends BaseCardTest {

    private void castCorpseExplosion(int graveyardCardIndex) {
        harness.setHand(player1, List.of(new CorpseExplosion()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstantWithGraveyardExile(player1, 0, null, graveyardCardIndex);
    }

    @Test
    @DisplayName("Exiles a creature from the graveyard and deals its power to every creature and planeswalker")
    void exilesCreatureAndDealsPowerDamageToCreaturesAndPlaneswalkers() {
        Card exiledCreature = new HillGiant();
        harness.setGraveyard(player1, List.of(exiledCreature));
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);

        castCorpseExplosion(0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(exiledCreature);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(planeswalker);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not deal Corpse Explosion damage to players")
    void doesNotDamagePlayers() {
        harness.setGraveyard(player1, List.of(new HillGiant()));
        castCorpseExplosion(0);

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot cast without a creature card in the graveyard")
    void cannotCastWithoutCreatureInGraveyard() {
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new CorpseExplosion()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithGraveyardExile(player1, 0, null, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
