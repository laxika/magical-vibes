package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BusterSword;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SummonTitan;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ColiseumBehemoth.class, BusterSword.class, Forest.class, SummonTitan.class})
class ColiseumBehemothTest extends BaseCardTest {

    @Test
    void destroysTargetArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BusterSword());
        castBehemoth();

        harness.handleListChoice(player1, "Destroy target artifact or enchantment.");
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Buster Sword");
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
        harness.addToBattlefield(player2, new BusterSword());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ColiseumBehemoth());
        castBehemoth();

        harness.handleListChoice(player1, "Destroy target artifact or enchantment.");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void destroysTargetEnchantmentCreature() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new SummonTitan());
        harness.setLibrary(player1, List.of(new Forest()));
        castBehemoth();

        harness.handleListChoice(player1, "Destroy target artifact or enchantment.");
        harness.handlePermanentChosen(player1, enchantment.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Summon: Titan");
        harness.assertNotInHand(player1, "Forest");
        harness.assertOnBattlefield(player1, "Coliseum Behemoth");
    }

    @Test
    void canDestroyOwnArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new BusterSword());
        castBehemoth();

        harness.handleListChoice(player1, "Destroy target artifact or enchantment.");
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Buster Sword");
    }

    @Test
    void drawingLeavesAvailableArtifactUntouchedAndDrawsExactlyOneCard() {
        harness.addToBattlefield(player2, new BusterSword());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        castBehemoth();

        harness.handleListChoice(player1, "Draw a card.");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Buster Sword");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void destroyModeRejectsLandTarget() {
        harness.addToBattlefield(player2, new BusterSword());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        castBehemoth();

        harness.handleListChoice(player1, "Destroy target artifact or enchantment.");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void missingDestroyTargetDoesNotSwitchToDrawing() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BusterSword());
        harness.setLibrary(player1, List.of(new Forest()));
        castBehemoth();

        harness.handleListChoice(player1, "Destroy target artifact or enchantment.");
        harness.handlePermanentChosen(player1, artifact.getId());
        gd.playerBattlefields.get(player2.getId()).remove(artifact);
        gd.playerHands.get(player2.getId()).add(artifact.getCard());
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Forest");
        harness.assertInHand(player2, "Buster Sword");
        harness.assertNotInGraveyard(player2, "Buster Sword");
        assertThat(gd.stack).isEmpty();
    }

    private void castBehemoth() {
        harness.castFromHand(player1, new ColiseumBehemoth(), "{5}{G}{G}");
        harness.passBothPriorities();
    }
}
