package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SelflessSpirit.class, GrizzlyBears.class, Murder.class})
class SelflessSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Selfless Spirit grants indestructible to your creatures")
    void sacrificeGrantsIndestructibleToOwnCreatures() {
        addReadySelflessSpirit(player1);
        Permanent bears = addReadyCreature(player1);
        Permanent opponentBears = addReadyCreature(player2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.assertNotOnBattlefield(player1, "Selfless Spirit");
        harness.assertInGraveyard(player1, "Selfless Spirit");
    }

    @Test
    @DisplayName("Granted indestructible wears off at end of turn")
    void indestructibleResetsAtEndOfTurn() {
        addReadySelflessSpirit(player1);
        Permanent bears = addReadyCreature(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Sacrifice is paid before indestructible is granted")
    void sacrificeIsPaidBeforeResolution() {
        addReadySelflessSpirit(player1);
        Permanent survivor = addReadySelflessSpirit(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(countPermanents(player1, "Selfless Spirit")).isEqualTo(1);
        harness.assertInGraveyard(player1, "Selfless Spirit");
        assertThat(gqs.hasKeyword(gd, survivor, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, survivor, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Selfless Spirit can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new SelflessSpirit());
        source.setSummoningSick(true);
        source.tap();
        Permanent survivor = addReadySelflessSpirit(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Selfless Spirit");
        assertThat(countPermanents(player1, "Selfless Spirit")).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, survivor, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Creatures entering before resolution receive indestructible, later creatures do not")
    void affectedCreaturesAreDeterminedAtResolution() {
        addReadySelflessSpirit(player1);

        harness.activateAbility(player1, 0, null, null);
        Permanent beforeResolution = harness.enterBattlefieldAndReturn(player1, new SelflessSpirit());
        harness.passBothPriorities();
        Permanent afterResolution = harness.enterBattlefieldAndReturn(player1, new SelflessSpirit());

        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Selfless Spirit can be sacrificed when you control no other creatures")
    void canActivateWithoutOtherCreatures() {
        addReadySelflessSpirit(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Selfless Spirit");
        harness.assertInGraveyard(player1, "Selfless Spirit");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing Selfless Spirit in response to Murder saves another creature")
    void protectsCreatureFromDestructionInResponse() {
        addReadySelflessSpirit(player1);
        Permanent survivor = addReadySelflessSpirit(player1);
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castInstant(player2, 0, survivor.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Selfless Spirit")).containsExactly(survivor);
        assertThat(gqs.hasKeyword(gd, survivor, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertInGraveyard(player1, "Selfless Spirit");
        harness.assertInGraveyard(player2, "Murder");
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadySelflessSpirit(Player player) {
        return addCreatureReady(player, new SelflessSpirit());
    }

    private Permanent addReadyCreature(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }
}
