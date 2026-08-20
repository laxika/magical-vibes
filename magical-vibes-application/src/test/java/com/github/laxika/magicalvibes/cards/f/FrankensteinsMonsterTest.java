package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FrankensteinsMonster.class, GrizzlyBears.class, Pacifism.class})
class FrankensteinsMonsterTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles exactly X creature cards and puts the chosen counter type on the creature")
    void exilesExactlyXCreatureCardsAndChoosesCounterType() {
        GrizzlyBears firstBear = new GrizzlyBears();
        GrizzlyBears secondBear = new GrizzlyBears();
        Pacifism pacifism = new Pacifism();
        harness.setGraveyard(player1, new ArrayList<>(List.of(firstBear, secondBear, pacifism)));
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
        assertThat(graveyardChoice.validCardIds()).containsExactlyInAnyOrder(firstBear.getId(), secondBear.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstBear.getId(), secondBear.getId()));

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
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(pacifism);
        assertThat(gd.getCardsExiledByPermanent(monster.getId()))
                .extracting(card -> card.getId())
                .containsExactlyInAnyOrder(firstBear.getId(), secondBear.getId());
    }

    @Test
    @DisplayName("Puts the creature into its owner's graveyard when X creature cards are unavailable")
    void goesToGraveyardWhenXCreatureCardsAreUnavailable() {
        GrizzlyBears bear = new GrizzlyBears();
        Pacifism pacifism = new Pacifism();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bear, pacifism)));
        FrankensteinsMonster monster = new FrankensteinsMonster();
        harness.setHand(player1, List.of(monster));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playCard(gd, player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getCard().getId().equals(monster.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bear, pacifism, monster);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("X=0 enters without exiling cards or choosing a counter type")
    void entersWithoutExileAtXZero() {
        GrizzlyBears bear = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bear)));
        harness.setHand(player1, List.of(new FrankensteinsMonster()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent monster = findPermanent(player1, "Frankenstein's Monster");
        assertThat(monster.getCounters()).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bear);
    }
}
