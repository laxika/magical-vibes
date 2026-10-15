package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CrawlingChorus;
import com.github.laxika.magicalvibes.cards.c.ContagiousVorrac;
import com.github.laxika.magicalvibes.cards.t.TyvarsStand;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MazesMantle.class, CrawlingChorus.class, ContagiousVorrac.class, TyvarsStand.class})
class MazesMantleTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a toxic enchanted creature +2/+2 and hexproof until end of turn")
    void toxicEnchantedCreatureGetsBoostAndHexproof() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CrawlingChorus());
        castAndResolve(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Does not grant hexproof to a non-toxic enchanted creature")
    void nonToxicEnchantedCreatureDoesNotGetHexproof() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ContagiousVorrac());
        castAndResolve(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("The temporary hexproof grant wears off at end of turn")
    void hexproofWearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CrawlingChorus());
        castAndResolve(creature);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Can be cast during an opponent's turn and grants hexproof to their toxic creature")
    void flashOnOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CrawlingChorus());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.ensurePriority(player1);

        castAndResolve(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Removing the Aura removes its boost but preserves the resolved temporary hexproof")
    void hexproofPersistsAfterAuraLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CrawlingChorus());
        castAndResolve(creature);
        Permanent aura = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Maze's Mantle"));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));

        harness.assertInGraveyard(player1, "Maze's Mantle");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("The non-targeting trigger resolves even if the opponent's enchanted creature gains hexproof")
    void hexproofInResponseDoesNotCounterTrigger() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CrawlingChorus());
        harness.setHand(player1, List.of(new MazesMantle()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new TyvarsStand()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
        gd.gameLog.clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).extracting(GameLogEntry::plainText)
                .noneMatch(message -> message.contains("fizzles"));
    }

    private void castAndResolve(Permanent target) {
        harness.setHand(player1, List.of(new MazesMantle()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
