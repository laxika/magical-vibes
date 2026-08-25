package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DefiantThundermaw;
import com.github.laxika.magicalvibes.cards.d.DragonEgg;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DefiantThundermaw.class, DragonEgg.class, Forest.class, GrizzlyBears.class, ShivanDragon.class,
        InvasionOfTarkir.class})
class InvasionOfTarkirTest extends BaseCardTest {

    @Test
    void etbRevealsDragonsBeforeChoosingAnOtherTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        DragonEgg dragon = new DragonEgg();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new InvasionOfTarkir(), dragon, forest));
        addManaToCast();

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice reveal =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(reveal.validCardIds()).containsExactly(dragon.getId());

        harness.handleMultipleCardsChosen(player1, List.of(dragon.getId()));

        PendingInteraction.PermanentChoice target =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        Permanent battle = findPermanent(player1, "Invasion of Tarkir");
        assertThat(target.validIds()).contains(bears.getId()).doesNotContain(battle.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void etbDealsTwoDamageWhenNoDragonIsRevealed() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new InvasionOfTarkir()));
        addManaToCast();

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void transformedFaceDealsDamageWhenADragonAttacks() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfTarkir());
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Defiant Thundermaw")).isNotNull();
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new ShivanDragon());
        dragon.setSummoningSick(false);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(dragon)));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(2);
    }

    private void addManaToCast() {
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 1);
    }

}
