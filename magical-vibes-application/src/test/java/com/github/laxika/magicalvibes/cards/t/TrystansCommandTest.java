package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrystansCommandTest extends BaseCardTest {

    @Test
    void copyAndDestroyModesResolveInCardTextOrder() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new TrystansCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 2},
                List.of(elf.getId(), elf.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken());
        harness.assertInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    void graveyardReturnModePreservesOtherModeTarget() {
        Card creatureCard = new GrizzlyBears();
        Card artifactCard = new Spellbook();
        harness.setGraveyard(player1, List.of(creatureCard, artifactCard));
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TrystansCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{1, 2},
                List.of(creature.getId()));

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, new ArrayList<>(choice.validCardIds()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Spellbook");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void boostAndUntapModeAffectsOnlyTargetPlayersCreatures() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent targetCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        targetCreature.tap();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ownCreature.tap();
        harness.setHand(player1, List.of(new TrystansCommand()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 3},
                List.of(elf.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, targetCreature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, targetCreature)).isEqualTo(5);
        assertThat(targetCreature.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(ownCreature.isTapped()).isTrue();
    }

    @Test
    void copyModeRejectsElfControlledByOpponent() {
        Permanent opponentElf = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new TrystansCommand()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 2},
                List.of(opponentElf.getId(), opponentElf.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
