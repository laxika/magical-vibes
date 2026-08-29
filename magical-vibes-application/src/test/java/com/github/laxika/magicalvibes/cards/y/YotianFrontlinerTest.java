package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class YotianFrontlinerTest extends BaseCardTest {

    @Test
    @DisplayName("Attack trigger targets another creature I control")
    void attackTriggerRestrictsTargets() {
        Permanent frontliner = addCreatureReady(player1, new YotianFrontliner());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ownCreature.getId())
                .doesNotContain(frontliner.getId(), opponentCreature.getId());
    }

    @Test
    @DisplayName("Attack trigger gives another creature +1/+1 until end of turn")
    void attackTriggerBoostsTarget() {
        addCreatureReady(player1, new YotianFrontliner());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getPowerModifier()).isEqualTo(1);
        assertThat(ownCreature.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Unearth returns Yotian Frontliner with haste and exiles it at the next end step")
    void unearthReturnsWithHasteAndExilesAtEndStep() {
        harness.setGraveyard(player1, List.of(new YotianFrontliner()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent frontliner = findPermanent(player1, "Yotian Frontliner");
        assertThat(frontliner.getGrantedKeywords()).contains(Keyword.HASTE);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Yotian Frontliner");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(cardInExile -> cardInExile.getName().equals("Yotian Frontliner"));
    }
}
