package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VedalkenHumiliator.class, SerraAngel.class, Spellbook.class})
class VedalkenHumiliatorTest extends BaseCardTest {

    @Test
    @DisplayName("With metalcraft, attacking makes opponents' creatures 1/1 without abilities")
    void humiliatesOpponentsCreaturesWithMetalcraft() {
        Permanent humiliator = addCreatureReady(player1, new VedalkenHumiliator());
        addArtifacts(player1, 3);
        Permanent angel = addCreatureReady(player2, new SerraAngel());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(angel.getEffectivePower()).isEqualTo(1);
        assertThat(angel.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isFalse();
        assertThat(humiliator.getEffectivePower()).isEqualTo(3);
    }

    @Test
    @DisplayName("Without metalcraft, attacking does not affect opponents' creatures")
    void doesNotHumiliateWithoutMetalcraft() {
        addCreatureReady(player1, new VedalkenHumiliator());
        Permanent angel = addCreatureReady(player2, new SerraAngel());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(angel.getEffectivePower()).isEqualTo(4);
        assertThat(angel.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The humiliation wears off at end of turn")
    void humiliationWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new VedalkenHumiliator());
        addArtifacts(player1, 3);
        Permanent angel = addCreatureReady(player2, new SerraAngel());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(angel.getEffectivePower()).isEqualTo(1);

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(angel.getEffectivePower()).isEqualTo(4);
        assertThat(angel.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
    }

    private void addArtifacts(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new Spellbook());
        }
    }
}
