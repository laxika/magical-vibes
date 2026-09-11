package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({TribalForcemage.class, LlanowarElves.class, GrizzlyBears.class})
class TribalForcemageTest extends BaseCardTest {

    @Test
    @DisplayName("Turning Tribal Forcemage face up boosts and gives trample to the chosen type")
    void turningFaceUpBoostsChosenTypeAcrossBattlefields() {
        Permanent ownElf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent opposingElf = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent forcemage = castFaceDown();

        turnFaceUp(forcemage);
        harness.handleListChoice(player1, CardSubtype.ELF.name());

        assertThat(gqs.getEffectivePower(gd, ownElf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownElf)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ownElf, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposingElf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposingElf)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, opposingElf, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Tribal Forcemage's face-up effect wears off at end of turn")
    void faceUpEffectWearsOffAtEndOfTurn() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent forcemage = castFaceDown();

        turnFaceUp(forcemage);
        harness.handleListChoice(player1, CardSubtype.ELF.name());
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, elf, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, elf, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new TribalForcemage()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return findPermanent(player1, "Tribal Forcemage");
    }

    private void turnFaceUp(Permanent forcemage) {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(forcemage));
        harness.passBothPriorities();
    }
}
