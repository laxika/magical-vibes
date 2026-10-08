package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KomodoRhino;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SmellerbeeRebelFighter.class, GrizzlyBears.class, Forest.class, KomodoRhino.class, Mountain.class})
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

    @Test
    @DisplayName("Accepting with an empty hand still draws for attacking creatures")
    void drawsWhenDiscardingEmptyHand() {
        addCreatureReady(player1, new SmellerbeeRebelFighter());
        addCreatureReady(player1, new KomodoRhino());
        addCreatureReady(player1, new KomodoRhino());
        harness.setHand(player1, List.of());
        Mountain drawn = new Mountain();
        harness.setLibrary(player1, List.of(drawn, new Mountain()));

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2).contains(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Haste applies only to your other creatures and ends when Smellerbee leaves")
    void hasteIsLimitedToControllerAndSourcePresence() {
        Permanent smellerbee = harness.addToBattlefieldAndReturn(player1, new SmellerbeeRebelFighter());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new KomodoRhino());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new KomodoRhino());

        assertThat(gqs.hasKeyword(gd, ally, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, smellerbee, Keyword.HASTE)).isFalse();

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, smellerbee);

        assertThat(gqs.hasKeyword(gd, ally, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Other creatures attacking without Smellerbee do not trigger a discard")
    void doesNotTriggerWhenSmellerbeeDoesNotAttack() {
        addCreatureReady(player1, new SmellerbeeRebelFighter());
        addCreatureReady(player1, new KomodoRhino());
        Mountain retained = new Mountain();
        harness.setHand(player1, List.of(retained));
        harness.setLibrary(player1, List.of(new Mountain()));

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The trigger survives Smellerbee leaving and counts the remaining attackers")
    void countsAttackersAtResolutionAfterSourceLeaves() {
        Permanent smellerbee = addCreatureReady(player1, new SmellerbeeRebelFighter());
        addCreatureReady(player1, new KomodoRhino());
        Mountain discarded = new Mountain();
        harness.setHand(player1, List.of(discarded));
        Mountain drawn = new Mountain();
        harness.setLibrary(player1, List.of(drawn, new Mountain()));

        declareAttackers(List.of(0, 1));
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, smellerbee);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(smellerbee.getCard(), discarded);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
