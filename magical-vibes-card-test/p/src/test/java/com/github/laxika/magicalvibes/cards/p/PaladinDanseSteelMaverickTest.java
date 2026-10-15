package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PaladinDanseSteelMaverick.class, Ornithopter.class, EliteVanguard.class, GrizzlyBears.class})
class PaladinDanseSteelMaverickTest extends BaseCardTest {

    @Test
    @DisplayName("Exile is paid immediately, while protection waits for resolution")
    void exileCostIsPaidBeforeProtectionResolves() {
        Permanent paladin = addCreatureReady(player1, new PaladinDanseSteelMaverick());
        paladin.setSummoningSick(true);
        paladin.tap();
        Permanent human = addCreatureReady(player1, new EliteVanguard());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(paladin);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(paladin.getCard());
        assertThat(gqs.hasKeyword(gd, human, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, human, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Protection includes creatures entering before resolution but excludes later arrivals")
    void protectionUsesCreaturesPresentAtResolution() {
        addCreatureReady(player1, new PaladinDanseSteelMaverick());

        harness.activateAbility(player1, 0, null, null);
        Permanent beforeResolution = addCreatureReady(player1, new Ornithopter());
        harness.passBothPriorities();
        Permanent afterResolution = addCreatureReady(player1, new Ornithopter());

        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Danse can be exiled when no other creatures are present")
    void canActivateWithNoRecipients() {
        Permanent paladin = addCreatureReady(player1, new PaladinDanseSteelMaverick());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(paladin.getCard());
    }

    @Test
    @DisplayName("Exiling Paladin Danse protects your artifact and Human creatures")
    void exilingProtectsArtifactAndHumanCreatures() {
        Permanent paladin = addCreatureReady(player1, new PaladinDanseSteelMaverick());
        Permanent artifact = addCreatureReady(player1, new Ornithopter());
        Permanent human = addCreatureReady(player1, new EliteVanguard());
        Permanent nonMatching = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentArtifact = addCreatureReady(player2, new Ornithopter());
        Permanent opponentHuman = addCreatureReady(player2, new EliteVanguard());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, artifact, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, human, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonMatching, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentArtifact, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentHuman, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(paladin);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(paladin.getCard());
    }

    @Test
    @DisplayName("The granted indestructible wears off at end of turn")
    void indestructibleWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new PaladinDanseSteelMaverick());
        Permanent artifact = addCreatureReady(player1, new Ornithopter());
        Permanent human = addCreatureReady(player1, new EliteVanguard());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, human, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, artifact, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, human, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
