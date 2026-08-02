package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShadowAlleyDenizenTest extends BaseCardTest {

    @Test
    @DisplayName("Another black creature entering lets you give target creature intimidate")
    void blackCreatureEnterGrantsIntimidate() {
        harness.addToBattlefield(player1, new ShadowAlleyDenizen());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castWalkingCorpse(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.EntersTriggerTarget.class);

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INTIMIDATE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's creature is a legal target")
    void canTargetOpponentCreature() {
        harness.addToBattlefield(player1, new ShadowAlleyDenizen());
        Permanent enemy = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castWalkingCorpse(player1);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, enemy.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, enemy, Keyword.INTIMIDATE)).isTrue();
    }

    @Test
    @DisplayName("Intimidate wears off at end of turn")
    void intimidateWearsOff() {
        harness.addToBattlefield(player1, new ShadowAlleyDenizen());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castWalkingCorpse(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INTIMIDATE)).isFalse();
    }

    @Test
    @DisplayName("A non-black creature entering does not trigger the ability")
    void nonBlackCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new ShadowAlleyDenizen());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INTIMIDATE)).isFalse();
    }

    @Test
    @DisplayName("Shadow Alley Denizen entering does not trigger its own ability")
    void ownEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShadowAlleyDenizen()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castWalkingCorpse(Player player) {
        harness.setHand(player, List.of(new WalkingCorpse()));
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.castCreature(player, 0);
    }
}
