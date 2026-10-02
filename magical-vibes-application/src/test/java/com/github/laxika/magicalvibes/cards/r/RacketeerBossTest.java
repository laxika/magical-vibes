package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RacketeerBoss.class, GrizzlyBears.class, Shock.class})
class RacketeerBossTest extends BaseCardTest {

    @Test
    void choosesUpToTwoCreatureCardsAndEachCreatesOnlyOneTreasure() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        Shock nonCreature = new Shock();
        harness.setHand(player1, List.of(new RacketeerBoss(), first, second, nonCreature));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        PendingInteraction.PerpetualTriggeredAbilityCardsChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PerpetualTriggeredAbilityCardsChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(first.getId(), second.getId());

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        assertThat(gd.perpetualTriggeredAbilityGrants).containsKeys(first.getId(), second.getId());

        castBears(first);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.perpetualTriggeredAbilityGrants).containsKey(second.getId());

        castBears(second);
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        assertThat(gd.perpetualTriggeredAbilityGrants).doesNotContainKeys(first.getId(), second.getId());
    }

    private void castBears(GrizzlyBears bears) {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, gd.playerHands.get(player1.getId()).indexOf(bears));
        resolveAllTriggers();
    }
}
