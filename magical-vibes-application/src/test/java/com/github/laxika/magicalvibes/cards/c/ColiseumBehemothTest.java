package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ColiseumBehemoth.class, Bonesplitter.class, Forest.class, GrizzlyBears.class})
class ColiseumBehemothTest extends BaseCardTest {

    @Test
    void destroysTargetArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Bonesplitter());
        castBehemoth();

        harness.handleListChoice(player1, "Destroy target artifact or enchantment.");
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Bonesplitter");
    }

    @Test
    void drawsACard() {
        harness.setLibrary(player1, List.of(new Forest()));
        castBehemoth();

        harness.handleListChoice(player1, "Draw a card.");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
    }

    @Test
    void destroyModeRejectsCreatureTarget() {
        harness.addToBattlefield(player2, new Bonesplitter());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castBehemoth();

        harness.handleListChoice(player1, "Destroy target artifact or enchantment.");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castBehemoth() {
        harness.setHand(player1, List.of(new ColiseumBehemoth()));
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
