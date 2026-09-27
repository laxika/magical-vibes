package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.ReachThroughMists;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({TheFantasticar.class, ReachThroughMists.class, GrizzlyBears.class})
class TheFantasticarTest extends BaseCardTest {

    @Test
    @DisplayName("A noncreature spell may animate The Fantasticar until end of turn")
    void noncreatureSpellAnimatesFantasticar() {
        Permanent fantasticar = addFantasticar();
        castNoncreatureSpell(true);

        assertThat(gqs.isCreature(gd, fantasticar)).isTrue();
        assertThat(gqs.isArtifact(fantasticar)).isTrue();
        assertThat(gqs.getEffectivePower(gd, fantasticar)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, fantasticar)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, fantasticar)).isFalse();
    }

    @Test
    @DisplayName("The fourth noncreature spell can sacrifice The Fantasticar for four Constructs")
    void fourthNoncreatureSpellCreatesConstructsAfterSacrifice() {
        addFantasticar();

        for (int i = 0; i < 4; i++) {
            castNoncreatureSpell(i == 3);
        }

        assertThat(findPermanents(player1, "The Fantasticar")).isEmpty();
        List<Permanent> constructs = findPermanents(player1, "Construct");
        assertThat(constructs).hasSize(4);
        assertThat(constructs).allSatisfy(construct -> {
            assertThat(construct.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(4);
            assertThat(gqs.hasKeyword(gd, construct, Keyword.FLYING)).isTrue();
            assertThat(gqs.hasKeyword(gd, construct, Keyword.HASTE)).isTrue();
        });
    }

    private Permanent addFantasticar() {
        return harness.addToBattlefieldAndReturn(player1, new TheFantasticar());
    }

    private void castNoncreatureSpell(boolean acceptAnimation) {
        harness.setHand(player1, List.of(new ReachThroughMists()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        while (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, acceptAnimation);
            harness.passBothPriorities();
        }
    }
}
