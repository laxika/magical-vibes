package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CelestineCaveWitch;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HistoriansBoon.class, GloriousAnthem.class, HistoryOfBenalia.class, CelestineCaveWitch.class})
class HistoriansBoonTest extends BaseCardTest {

    @Test
    void createsSoldiersForItsOwnAndAnotherNontokenEnchantmentEntry() {
        harness.setHand(player1, List.of(new HistoriansBoon()));
        addManaForHistoriansBoon();
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Soldier")).hasSize(1);

        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Soldier")).hasSize(2);
    }

    @Test
    void doesNotTriggerForAnEnchantmentToken() {
        harness.addToBattlefield(player1, new HistoriansBoon());
        harness.setHand(player1, List.of(new CelestineCaveWitch()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent witch = findPermanent(player1, "Celestine Cave Witch");
        witch.setSummoningSick(false);
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(witch)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        Permanent insect = findPermanents(player1, "Insect").get(0);
        harness.handlePermanentChosen(player1, insect.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Curse")).hasSize(1);
        assertThat(findPermanents(player1, "Soldier")).isEmpty();
    }

    @Test
    void createsAnAngelWhenAControlledSagaFinalChapterResolves() {
        harness.addToBattlefield(player1, new HistoriansBoon());
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new HistoryOfBenalia());
        saga.setCounterCount(CounterType.LORE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent angel = findPermanent(player1, "Angel");
        assertThat(angel.getCard().getSubtypes()).containsExactly(CardSubtype.ANGEL);
        assertThat(angel.getCard().getPower()).isEqualTo(4);
        assertThat(angel.getCard().getToughness()).isEqualTo(4);
        assertThat(angel.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.VIGILANCE)).isTrue();
    }

    private void addManaForHistoriansBoon() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
