package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.cards.f.FatedRetribution;
import com.github.laxika.magicalvibes.cards.g.GreatHart;
import com.github.laxika.magicalvibes.cards.k.KarametraGodOfHarvests;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EpharaGodOfThePolis.class, GrizzlyBears.class, GlorySeeker.class, CloudSprite.class,
        GreatHart.class, KarametraGodOfHarvests.class, FatedRetribution.class})
class EpharaGodOfThePolisTest extends BaseCardTest {

    @Test
    @DisplayName("Ephara is not a creature below seven combined white and blue devotion")
    void isNotCreatureBelowDevotionThreshold() {
        Permanent ephara = addEphara();
        addWhiteAndBluePermanents(4);

        assertThat(gqs.isCreature(gd, ephara)).isFalse();
        assertThat(gqs.isEnchantment(gd, ephara)).isTrue();
    }

    @Test
    @DisplayName("Ephara becomes a creature at seven combined white and blue devotion")
    void becomesCreatureAtDevotionThreshold() {
        Permanent ephara = addEphara();
        addWhiteAndBluePermanents(5);

        assertThat(gqs.isCreature(gd, ephara)).isTrue();
    }

    @Test
    @DisplayName("Draws a card at upkeep after another creature entered under your control last turn")
    void drawsAfterCreatureEnteredLastTurn() {
        addEphara();
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        gd.permanentsEnteredBattlefieldLastTurn
                .computeIfAbsent(player1.getId(), ignored -> new ArrayList<>())
                .add(new GrizzlyBears());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Does not trigger for a creature that entered this turn")
    void doesNotUseCurrentTurnEntries() {
        addEphara();
        gd.permanentsEnteredBattlefieldThisTurn
                .computeIfAbsent(player1.getId(), ignored -> new ArrayList<>())
                .add(new GrizzlyBears());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when only Ephara entered last turn")
    void doesNotTriggerForItsOwnEntry() {
        Permanent ephara = addEphara();
        gd.permanentsEnteredBattlefieldLastTurn
                .computeIfAbsent(player1.getId(), ignored -> new ArrayList<>())
                .add(ephara.getCard());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotCountGodThatEnteredAsNoncreature() {
        addEphara();
        Permanent karametra = harness.enterBattlefieldAndReturn(player1, new KarametraGodOfHarvests());
        assertThat(gqs.isCreature(gd, karametra)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void drawsOnlyOnceForMultipleCreaturesThatHaveLeft() {
        addEphara();
        Card drawn = new GreatHart();
        harness.setLibrary(player1, List.of(drawn, new GreatHart()));
        Permanent first = harness.enterBattlefieldAndReturn(player1, new GreatHart());
        Permanent second = harness.enterBattlefieldAndReturn(player1, new GreatHart());
        gd.playerBattlefields.get(player1.getId()).removeAll(List.of(first, second));
        gd.playerGraveyards.get(player1.getId()).addAll(List.of(first.getCard(), second.getCard()));

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotCountOpponentsCreatureEntry() {
        addEphara();
        harness.enterBattlefieldAndReturn(player2, new GreatHart());

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void drawsOnOwnUpkeepAfterCreatureEnteredOnOpponentsTurn() {
        addEphara();
        Card drawn = new GreatHart();
        harness.setLibrary(player1, List.of(drawn));
        harness.forceActivePlayer(player2);
        harness.enterBattlefieldAndReturn(player1, new GreatHart());

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player1, TurnStep.UPKEEP);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void losesCreatureTypeWhenDevotionDrops() {
        Permanent ephara = addEphara();
        addWhiteAndBluePermanents(5);
        assertThat(gqs.isCreature(gd, ephara)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(1);

        assertThat(gqs.isCreature(gd, ephara)).isFalse();
        assertThat(gqs.isEnchantment(gd, ephara)).isTrue();
    }

    @Test
    void survivesDestructionWhileCreatureAndThenLosesCreatureType() {
        Permanent ephara = addEphara();
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new GreatHart());
        }
        assertThat(gqs.isCreature(gd, ephara)).isTrue();

        harness.castFromHand(player2, new FatedRetribution(), "{4}{W}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(ephara);
        assertThat(gqs.isCreature(gd, ephara)).isFalse();
    }

    private Permanent addEphara() {
        return harness.addToBattlefieldAndReturn(player1, new EpharaGodOfThePolis());
    }

    private void addWhiteAndBluePermanents(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, i % 2 == 0 ? new GlorySeeker() : new CloudSprite());
        }
    }
}
