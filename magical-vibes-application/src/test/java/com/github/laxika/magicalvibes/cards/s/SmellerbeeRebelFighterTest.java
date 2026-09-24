package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SmellerbeeRebelFighter.class, GrizzlyBears.class, Forest.class})
class SmellerbeeRebelFighterTest extends BaseCardTest {

    @Test
    @DisplayName("Gives haste to other creatures you control")
    void givesHasteToOtherCreatures() {
        Permanent smellerbee = addCreatureReady(player1, new SmellerbeeRebelFighter());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, smellerbee, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("May discard the hand and draw for each attacking creature")
    void mayDiscardHandAndDrawForEachAttacker() {
        addCreatureReady(player1, new SmellerbeeRebelFighter());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        GrizzlyBears discardedCreature = new GrizzlyBears();
        Forest discardedLand = new Forest();
        harness.setHand(player1, List.of(discardedCreature, discardedLand));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        declareAttackers(List.of(0, 1, 2));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discardedCreature, discardedLand);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the attack trigger leaves the hand unchanged")
    void decliningAttackTriggerLeavesHandUnchanged() {
        addCreatureReady(player1, new SmellerbeeRebelFighter());
        GrizzlyBears retained = new GrizzlyBears();
        harness.setHand(player1, List.of(retained));
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
