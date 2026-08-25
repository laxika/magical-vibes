package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.PyreOfTheWorldTree;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Forest.class, InvasionOfKaldheim.class, Island.class, Mountain.class,
        PyreOfTheWorldTree.class, Shock.class})
class InvasionOfKaldheimTest extends BaseCardTest {

    @Test
    void etbExilesHandDrawsTheSameNumberAndAllowsPlayingThoseCards() {
        Card exiledLand = new Forest();
        Card exiledSpell = new Shock();
        Card drawnLand = new Island();
        Card drawnSpell = new Mountain();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(drawnLand, drawnSpell));
        harness.setHand(player1, List.of(new InvasionOfKaldheim(), exiledLand, exiledSpell));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnLand, drawnSpell);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(exiledLand.getId(), exiledSpell.getId());
        assertThat(gd.exilePlayPermissions)
                .containsEntry(exiledLand.getId(), player1.getId())
                .containsEntry(exiledSpell.getId(), player1.getId());

        gs.playCardFromExile(gd, player1, exiledLand.getId(), null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == exiledLand);
    }

    @Test
    void defeatCastsPyreOfTheWorldTreeTransformed() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfKaldheim());
        battle.setCounterCount(CounterType.DEFENSE, 0);

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent pyre = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof PyreOfTheWorldTree)
                .findFirst()
                .orElseThrow();
        assertThat(pyre.isTransformed()).isTrue();
    }

    @Test
    void discardingLandWithPyreDealsDamageAndExilesTopCardForPlay() {
        Permanent pyre = harness.addToBattlefieldAndReturn(player1, new InvasionOfKaldheim());
        pyre.setCard(new PyreOfTheWorldTree());
        pyre.setTransformed(true);
        Card discardedLand = new Forest();
        Card topCard = new Shock();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        harness.setHand(player1, List.of(discardedLand));
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
    }
}
