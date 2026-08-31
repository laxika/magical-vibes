package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BogImp;
import com.github.laxika.magicalvibes.cards.t.TormodsCrypt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FrankensteinsMonster.class, BogImp.class, TormodsCrypt.class})
class FrankensteinsMonsterTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles exactly X creature cards and puts the chosen counter type on the creature")
    void exilesExactlyXCreatureCardsAndChoosesCounterType() {
        BogImp firstImp = new BogImp();
        BogImp secondImp = new BogImp();
        TormodsCrypt crypt = new TormodsCrypt();
        harness.setGraveyard(player1, List.of(firstImp, secondImp, crypt));
        FrankensteinsMonster monsterCard = new FrankensteinsMonster();
        harness.setHand(player1, List.of(monsterCard));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playCard(gd, player1, 0, 2, null, null);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice graveyardChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(graveyardChoice).isNotNull();
        assertThat(graveyardChoice.minCount()).isEqualTo(2);
        assertThat(graveyardChoice.maxCount()).isEqualTo(2);
        assertThat(graveyardChoice.validCardIds()).containsExactlyInAnyOrder(firstImp.getId(), secondImp.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstImp.getId(), secondImp.getId()));

        PendingInteraction.ColorChoice counterChoice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(counterChoice.options()).containsExactly("+2/+0", "+1/+1", "+0/+2");
        harness.handleListChoice(player1, "+2/+0");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "+0/+2");

        Permanent monster = findPermanent(player1, "Frankenstein's Monster");
        assertThat(monster.getCounterCount(CounterType.PLUS_TWO_PLUS_ZERO)).isEqualTo(1);
        assertThat(monster.getCounterCount(CounterType.PLUS_ZERO_PLUS_TWO)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, monster)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, monster)).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(crypt);
        assertThat(gd.getCardsExiledByPermanent(monster.getId()))
                .extracting(card -> card.getId())
                .containsExactlyInAnyOrder(firstImp.getId(), secondImp.getId());
    }

    @Test
    @DisplayName("Can choose a +1/+1 counter for an exiled creature card")
    void choosesPlusOnePlusOneCounter() {
        BogImp imp = new BogImp();
        harness.setGraveyard(player1, List.of(imp));
        harness.setHand(player1, List.of(new FrankensteinsMonster()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        gs.playCard(gd, player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(imp.getId()));

        PendingInteraction.ColorChoice counterChoice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(counterChoice.options()).containsExactly("+2/+0", "+1/+1", "+0/+2");
        harness.handleListChoice(player1, "+1/+1");

        Permanent monster = findPermanent(player1, "Frankenstein's Monster");
        assertThat(monster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, monster)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, monster)).isEqualTo(2);
        assertThat(gd.getCardsExiledByPermanent(monster.getId())).extracting(card -> card.getId())
                .containsExactly(imp.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Puts the creature into its owner's graveyard when X creature cards are unavailable")
    void goesToGraveyardWhenXCreatureCardsAreUnavailable() {
        BogImp imp = new BogImp();
        TormodsCrypt crypt = new TormodsCrypt();
        harness.setGraveyard(player1, List.of(imp, crypt));
        FrankensteinsMonster monster = new FrankensteinsMonster();
        harness.setHand(player1, List.of(monster));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playCard(gd, player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getCard().getId().equals(monster.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(imp, crypt, monster);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("X=0 enters without exiling cards or choosing a counter type")
    void entersWithoutExileAtXZero() {
        BogImp imp = new BogImp();
        harness.setGraveyard(player1, List.of(imp));
        harness.setHand(player1, List.of(new FrankensteinsMonster()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent monster = findPermanent(player1, "Frankenstein's Monster");
        assertThat(monster.getCounters()).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(imp);
    }
}
