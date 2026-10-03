package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.m.MillennialGargoyle;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CorpseBlockade.class, MillennialGargoyle.class})
class CorpseBlockadeTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature grants deathtouch")
    void sacrificingAnotherCreatureGrantsDeathtouch() {
        Permanent blockade = addCreatureReady(player1, new CorpseBlockade());
        harness.addToBattlefield(player1, new MillennialGargoyle());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Millennial Gargoyle");
        assertThat(gqs.hasKeyword(gd, blockade, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Deathtouch wears off during cleanup")
    void deathtouchWearsOffAtEndOfTurn() {
        Permanent blockade = addCreatureReady(player1, new CorpseBlockade());
        harness.addToBattlefield(player1, new MillennialGargoyle());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, blockade, Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, blockade, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("The ability cannot be activated without another creature")
    void cannotActivateWithoutAnotherCreature() {
        addCreatureReady(player1, new CorpseBlockade());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The source cannot be sacrificed to its own ability")
    void cannotSacrificeItself() {
        Permanent blockade = addCreatureReady(player1, new CorpseBlockade());
        // Two other creatures, so the sacrifice cost actually prompts instead of auto-paying.
        harness.addToBattlefield(player1, new MillennialGargoyle());
        harness.addToBattlefield(player1, new MillennialGargoyle());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(blockade.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, blockade.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Corpse Blockade");
        harness.assertOnBattlefield(player1, "Millennial Gargoyle");
    }

    @Test
    @DisplayName("Sacrifice is paid before the deathtouch ability resolves")
    void sacrificeIsPaidBeforeResolution() {
        Permanent blockade = addCreatureReady(player1, new CorpseBlockade());
        harness.addToBattlefield(player1, new MillennialGargoyle());

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Millennial Gargoyle");
        harness.assertNotOnBattlefield(player1, "Millennial Gargoyle");
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.hasKeyword(gd, blockade, Keyword.DEATHTOUCH)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, blockade, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("An opposing creature cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        addCreatureReady(player1, new CorpseBlockade());
        harness.addToBattlefield(player2, new MillennialGargoyle());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Millennial Gargoyle");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped summoning-sick Corpse Blockade can activate repeatedly")
    void tappedSummoningSickBlockadeCanActivateRepeatedly() {
        Permanent blockade = harness.addToBattlefieldAndReturn(player1, new CorpseBlockade());
        blockade.setSummoningSick(true);
        blockade.setTapped(true);
        harness.addToBattlefield(player1, new MillennialGargoyle());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new MillennialGargoyle());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, blockade, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Millennial Gargoyle");
    }
}
