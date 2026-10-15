package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Treasure;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VihaanGoldwaker.class, GrizzlyBears.class, Treasure.class})
class VihaanGoldwakerTest extends BaseCardTest {

    @Test
    @DisplayName("Other outlaws you control have vigilance and haste")
    void grantsKeywordsToOtherOutlawsYouControl() {
        Permanent vihaan = addCreatureReady(player1, new VihaanGoldwaker());
        Permanent outlaw = addOutlaw(player1);
        Permanent nonOutlaw = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingOutlaw = addOutlaw(player2);

        assertThat(gqs.hasKeyword(gd, vihaan, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, vihaan, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, outlaw, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, outlaw, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonOutlaw, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, nonOutlaw, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingOutlaw, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingOutlaw, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Accepting the beginning-of-combat trigger animates your Treasures")
    void animatesOwnTreasures() {
        addCreatureReady(player1, new VihaanGoldwaker());
        Permanent ownTreasure = harness.addToBattlefieldAndReturn(player1, new Treasure());
        Permanent anotherOwnTreasure = harness.addToBattlefieldAndReturn(player1, new Treasure());
        Permanent opposingTreasure = harness.addToBattlefieldAndReturn(player2, new Treasure());

        advanceToCombat();
        harness.handleMayAbilityChosen(player1, true);

        assertAnimatedTreasure(ownTreasure);
        assertAnimatedTreasure(anotherOwnTreasure);
        assertThat(gqs.isCreature(gd, opposingTreasure)).isFalse();
    }

    @Test
    @DisplayName("Declining the beginning-of-combat trigger leaves Treasures unchanged")
    void mayDeclineTreasureAnimation() {
        addCreatureReady(player1, new VihaanGoldwaker());
        Permanent treasure = harness.addToBattlefieldAndReturn(player1, new Treasure());

        advanceToCombat();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.isCreature(gd, treasure)).isFalse();
        assertThat(gqs.getEffectivePower(gd, treasure)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, treasure)).isZero();
    }

    @Test
    @DisplayName("Animated Treasures retain Treasure and gain the outlaw keywords")
    void animatedTreasuresRetainSubtypeAndGainKeywords() {
        addCreatureReady(player1, new VihaanGoldwaker());
        Permanent treasure = harness.addToBattlefieldAndReturn(player1, new Treasure());

        advanceToCombat();
        harness.handleMayAbilityChosen(player1, true);

        assertAnimatedTreasure(treasure);
        assertThat(gqs.hasEffectiveSubtype(gd, treasure, CardSubtype.TREASURE)).isTrue();
        assertThat(gqs.hasKeyword(gd, treasure, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, treasure, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Treasures entering after resolution are not animated")
    void laterTreasuresAreNotAnimated() {
        addCreatureReady(player1, new VihaanGoldwaker());
        Permanent original = harness.addToBattlefieldAndReturn(player1, new Treasure());

        advanceToCombat();
        harness.handleMayAbilityChosen(player1, true);
        Permanent later = harness.addToBattlefieldAndReturn(player1, new Treasure());

        assertAnimatedTreasure(original);
        assertThat(gqs.isCreature(gd, later)).isFalse();
        assertThat(gqs.hasKeyword(gd, later, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, later, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Treasure animation and outlaw keywords expire at end of turn")
    void animationExpiresAtEndOfTurn() {
        addCreatureReady(player1, new VihaanGoldwaker());
        Permanent treasure = harness.addToBattlefieldAndReturn(player1, new Treasure());

        advanceToCombat();
        harness.handleMayAbilityChosen(player1, true);
        assertAnimatedTreasure(treasure);
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.isCreature(gd, treasure)).isFalse();
        assertThat(gqs.isArtifact(gd, treasure)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, treasure, CardSubtype.TREASURE)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, treasure, CardSubtype.CONSTRUCT)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, treasure, CardSubtype.ASSASSIN)).isFalse();
        assertThat(gqs.hasKeyword(gd, treasure, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, treasure, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Vihaan does not trigger during an opponent's combat")
    void doesNotTriggerOnOpponentsTurn() {
        addCreatureReady(player1, new VihaanGoldwaker());
        Permanent treasure = harness.addToBattlefieldAndReturn(player1, new Treasure());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.isCreature(gd, treasure)).isFalse();
    }

    private Permanent addOutlaw(Player player) {
        Permanent outlaw = addCreatureReady(player, new GrizzlyBears());
        TestCards.mutableCard(outlaw).setSubtypes(java.util.List.of(CardSubtype.ASSASSIN));
        return outlaw;
    }

    private void assertAnimatedTreasure(Permanent treasure) {
        assertThat(gqs.isCreature(gd, treasure)).isTrue();
        assertThat(gqs.isArtifact(gd, treasure)).isTrue();
        assertThat(gqs.getEffectivePower(gd, treasure)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, treasure)).isEqualTo(3);
        assertThat(gqs.hasEffectiveSubtype(gd, treasure, CardSubtype.CONSTRUCT)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, treasure, CardSubtype.ASSASSIN)).isTrue();
    }

    private void advanceToCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
    }
}
