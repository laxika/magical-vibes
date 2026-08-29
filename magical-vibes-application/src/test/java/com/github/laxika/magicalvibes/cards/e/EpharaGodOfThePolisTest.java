package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

    private Permanent addEphara() {
        return harness.addToBattlefieldAndReturn(player1, new EpharaGodOfThePolis());
    }

    private void addWhiteAndBluePermanents(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, i % 2 == 0 ? new GlorySeeker() : new CloudSprite());
        }
    }
}
