package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Treasure;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwashbucklerExtraordinaire.class, Treasure.class, GrizzlyBears.class})
class SwashbucklerExtraordinaireTest extends BaseCardTest {

    @Test
    void entersWithATreasure() {
        harness.enterBattlefieldAndReturn(player1, new SwashbucklerExtraordinaire());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void sacrificesTreasuresAndGrantsDoubleStrikeToThatManyCreatures() {
        addReadySwashbuckler();
        Permanent firstTarget = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondTarget = addCreatureReady(player1, new GrizzlyBears());
        Permanent firstTreasure = harness.addToBattlefieldAndReturn(player1, new Treasure());
        Permanent secondTreasure = harness.addToBattlefieldAndReturn(player1, new Treasure());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstTreasure.getId(), secondTreasure.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, firstTarget.getId());
        harness.handlePermanentChosen(player1, secondTarget.getId());
        resolveAllTriggers();

        assertThat(firstTarget.hasKeyword(com.github.laxika.magicalvibes.model.Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(secondTarget.hasKeyword(com.github.laxika.magicalvibes.model.Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void decliningOrSacrificingNoTreasuresDoesNothing() {
        addReadySwashbuckler();
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent treasure = harness.addToBattlefieldAndReturn(player1, new Treasure());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.hasKeyword(com.github.laxika.magicalvibes.model.Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(treasure);
    }

    private Permanent addReadySwashbuckler() {
        return addCreatureReady(player1, new SwashbucklerExtraordinaire());
    }
}
