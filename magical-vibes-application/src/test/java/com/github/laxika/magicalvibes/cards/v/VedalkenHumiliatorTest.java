package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CounterType;
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
        harness.passBothPriorities();

        assertThat(angel.getEffectivePower()).isEqualTo(4);
        assertThat(angel.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Gaining metalcraft after attacking does not create a trigger")
    void gainingMetalcraftAfterAttackDoesNotTrigger() {
        addCreatureReady(player1, new VedalkenHumiliator());
        addArtifacts(player1, 2);
        Permanent angel = addCreatureReady(player2, new SerraAngel());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).isEmpty();
        addArtifacts(player1, 1);
        resolveAllTriggers();

        assertThat(angel.getEffectivePower()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Losing metalcraft before resolution prevents humiliation")
    void losingMetalcraftBeforeResolutionPreventsEffect() {
        addCreatureReady(player1, new VedalkenHumiliator());
        addArtifacts(player1, 3);
        Permanent angel = addCreatureReady(player2, new SerraAngel());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(3);
        resolveAllTriggers();

        assertThat(angel.getEffectivePower()).isEqualTo(4);
        assertThat(angel.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Creatures entering before resolution are humiliated")
    void affectsCreaturesEnteringBeforeResolution() {
        addCreatureReady(player1, new VedalkenHumiliator());
        addArtifacts(player1, 3);
        addCreatureReady(player2, new SerraAngel());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        Permanent arrivingAngel = addCreatureReady(player2, new SerraAngel());
        resolveAllTriggers();

        assertThat(arrivingAngel.getEffectivePower()).isEqualTo(1);
        assertThat(arrivingAngel.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, arrivingAngel, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, arrivingAngel, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering after resolution retain their stats and abilities")
    void doesNotAffectCreaturesEnteringAfterResolution() {
        addCreatureReady(player1, new VedalkenHumiliator());
        addArtifacts(player1, 3);
        Permanent originalAngel = addCreatureReady(player2, new SerraAngel());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        Permanent arrivingAngel = addCreatureReady(player2, new SerraAngel());

        assertThat(originalAngel.getEffectivePower()).isEqualTo(1);
        assertThat(arrivingAngel.getEffectivePower()).isEqualTo(4);
        assertThat(arrivingAngel.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, arrivingAngel, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, arrivingAngel, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Counters still modify the humiliated creature's base stats")
    void countersModifyHumiliatedBaseStats() {
        addCreatureReady(player1, new VedalkenHumiliator());
        addArtifacts(player1, 3);
        Permanent angel = addCreatureReady(player2, new SerraAngel());
        angel.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The attack ability resolves even if Humiliator leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent humiliator = addCreatureReady(player1, new VedalkenHumiliator());
        addArtifacts(player1, 3);
        Permanent angel = addCreatureReady(player2, new SerraAngel());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(humiliator);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Humiliation does not affect the attacking player's other creatures")
    void leavesControllersOtherCreaturesUnaffected() {
        addCreatureReady(player1, new VedalkenHumiliator());
        addArtifacts(player1, 3);
        Permanent ownAngel = addCreatureReady(player1, new SerraAngel());
        Permanent opposingAngel = addCreatureReady(player2, new SerraAngel());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(opposingAngel.getEffectivePower()).isEqualTo(1);
        assertThat(ownAngel.getEffectivePower()).isEqualTo(4);
        assertThat(ownAngel.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ownAngel, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownAngel, Keyword.VIGILANCE)).isTrue();
    }

    private void addArtifacts(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new Spellbook());
        }
    }
}
