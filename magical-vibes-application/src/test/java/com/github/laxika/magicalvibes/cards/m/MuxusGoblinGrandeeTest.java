package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MuxusGoblinGrandee.class, RagingGoblin.class, GrizzlyBears.class, Shock.class, Forest.class})
class MuxusGoblinGrandeeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters all Goblin creatures with mana value 5 or less from the top six")
    void entersEligibleGoblins() {
        Card goblin1 = new RagingGoblin();
        Card goblin2 = new RagingGoblin();
        Card tooExpensiveGoblin = new MuxusGoblinGrandee();
        Card nonGoblinCreature = new GrizzlyBears();
        Card nonCreature = new Shock();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(
                goblin1, goblin2, tooExpensiveGoblin, nonGoblinCreature, nonCreature, land));
        harness.setHand(player1, List.of(new MuxusGoblinGrandee()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(goblin1.getId(), goblin2.getId());
        assertThat(choice.minCount()).isEqualTo(2);
        assertThat(choice.maxCount()).isEqualTo(2);
        harness.handleMultipleCardsChosen(player1, List.of(goblin1.getId(), goblin2.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == goblin1)
                .anyMatch(permanent -> permanent.getCard() == goblin2)
                .noneMatch(permanent -> permanent.getCard() == tooExpensiveGoblin)
                .noneMatch(permanent -> permanent.getCard() == nonGoblinCreature);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(tooExpensiveGoblin, nonGoblinCreature, nonCreature, land);
    }

    @Test
    @DisplayName("Gets +1/+1 for each other Goblin you control when it attacks")
    void boostsForOtherControlledGoblins() {
        Permanent muxus = addCreatureReady(player1, new MuxusGoblinGrandee());
        addCreatureReady(player1, new RagingGoblin());
        addCreatureReady(player1, new RagingGoblin());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(muxus.getPowerModifier()).isEqualTo(2);
        assertThat(muxus.getToughnessModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(muxus.getPowerModifier()).isZero();
        assertThat(muxus.getToughnessModifier()).isZero();
    }
}
