package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AkromasWill.class, EdgarMarkov.class, GrizzlyBears.class})
class AkromasWillTest extends BaseCardTest {

    @Test
    void firstModeGrantsCombatKeywordsToOwnCreatures() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        castMode(new int[]{0});

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    void secondModeGrantsLifelinkIndestructibleAndAllColorProtection() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        castMode(new int[]{1});

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.INDESTRUCTIBLE)).isTrue();
        for (CardColor color : CardColor.values()) {
            assertThat(gqs.hasProtectionFrom(gd, ownCreature, color)).isTrue();
            assertThat(gqs.hasProtectionFrom(gd, opposingCreature, color)).isFalse();
        }
    }

    @Test
    void commanderAllowsBothModes() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.makeCommander(player1.getId(), commander);
        addCreatureReady(player1, commander);
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        castMode(new int[]{0, 1});

        for (Keyword keyword : List.of(Keyword.FLYING, Keyword.VIGILANCE, Keyword.DOUBLE_STRIKE,
                Keyword.LIFELINK, Keyword.INDESTRUCTIBLE)) {
            assertThat(gqs.hasKeyword(gd, ownCreature, keyword)).isTrue();
        }
        for (CardColor color : CardColor.values()) {
            assertThat(gqs.hasProtectionFrom(gd, ownCreature, color)).isTrue();
        }
    }

    @Test
    void bothModesRequireControllingTheRegisteredCommander() {
        addToCommandZone(player1, new EdgarMarkov());
        harness.setHand(player1, List.of(new AkromasWill()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void controllingAnOpponentsCommanderAllowsBothModes() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.makeCommander(player2.getId(), commander);
        Permanent controlledCommander = addCreatureReady(player1, commander);

        castMode(new int[]{0, 1});

        assertThat(gqs.hasKeyword(gd, controlledCommander, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, controlledCommander, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, controlledCommander, CardColor.BLUE)).isTrue();
    }

    @Test
    void aNoncommanderWithTheSameNameDoesNotAllowBothModes() {
        addToCommandZone(player1, new EdgarMarkov());
        addCreatureReady(player1, new EdgarMarkov());
        harness.setHand(player1, List.of(new AkromasWill()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void secondModeDoesNotAffectCreaturesEnteringAfterResolution() {
        Permanent original = addCreatureReady(player1, new GrizzlyBears());
        castMode(new int[]{1});
        Permanent laterCreature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, original, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.INDESTRUCTIBLE)).isFalse();
        for (CardColor color : CardColor.values()) {
            assertThat(gqs.hasProtectionFrom(gd, original, color)).isTrue();
            assertThat(gqs.hasProtectionFrom(gd, laterCreature, color)).isFalse();
        }
    }

    @Test
    void firstModeDoesNotAffectCreaturesEnteringAfterResolution() {
        castMode(new int[]{0});
        Permanent laterCreature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        for (Keyword keyword : List.of(Keyword.FLYING, Keyword.VIGILANCE, Keyword.DOUBLE_STRIKE)) {
            assertThat(gqs.hasKeyword(gd, laterCreature, keyword)).isFalse();
        }
    }

    @Test
    void secondModeExpiresAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castMode(new int[]{1});

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isFalse();
        for (CardColor color : CardColor.values()) {
            assertThat(gqs.hasProtectionFrom(gd, creature, color)).isFalse();
        }
    }

    @Test
    void firstModeExpiresAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castMode(new int[]{0});

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        for (Keyword keyword : List.of(Keyword.FLYING, Keyword.VIGILANCE, Keyword.DOUBLE_STRIKE)) {
            assertThat(gqs.hasKeyword(gd, creature, keyword)).isFalse();
        }
    }

    private void castMode(int[] modes) {
        harness.setHand(player1, List.of(new AkromasWill()));
        addMana();
        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, List.of());
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    private void addToCommandZone(Player player, Card card) {
        gd.makeCommander(player.getId(), card);
        gd.playerCommandZones.get(player.getId()).add(card);
    }
}
