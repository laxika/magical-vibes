package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StreetRiot.class, GrizzlyBears.class, Opalescence.class})
class StreetRiotTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts and grants trample to your creatures during your turn")
    void boostsOwnCreaturesDuringOwnTurn() {
        harness.addToBattlefield(player1, new StreetRiot());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Does not affect your creatures during an opponent's turn")
    void doesNotBoostOwnCreaturesDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new StreetRiot());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Affects only creatures controlled by the enchantment's controller")
    void affectsOnlyControllerCreatures() {
        harness.addToBattlefield(player1, new StreetRiot());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ownBears, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposingBears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opposingBears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The bonus turns off and back on as the active player changes")
    void bonusTracksActivePlayer() {
        harness.addToBattlefield(player1, new StreetRiot());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();

        harness.forceActivePlayer(player1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Multiple Street Riots stack their power bonuses only during your turn")
    void multipleCopiesStackDuringOwnTurn() {
        harness.addToBattlefield(player2, new StreetRiot());
        harness.addToBattlefield(player2, new StreetRiot());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();

        harness.forceActivePlayer(player1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering later receive the ongoing bonus")
    void creaturesEnteringLaterReceiveBonus() {
        harness.addToBattlefield(player1, new StreetRiot());
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        Permanent bears = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Grizzly Bears"));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The bonus ends immediately when Street Riot leaves the battlefield")
    void bonusEndsWhenSourceLeaves() {
        Permanent riot = harness.addToBattlefieldAndReturn(player1, new StreetRiot());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(riot);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("An animated Street Riot receives its own bonus during your turn")
    void animatedStreetRiotReceivesOwnBonus() {
        Permanent riot = harness.addToBattlefieldAndReturn(player1, new StreetRiot());
        harness.addToBattlefield(player1, new Opalescence());
        harness.forceActivePlayer(player1);

        assertThat(gqs.isCreature(gd, riot)).isTrue();
        assertThat(gqs.getEffectivePower(gd, riot)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, riot)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, riot, Keyword.TRAMPLE)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.getEffectivePower(gd, riot)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, riot)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, riot, Keyword.TRAMPLE)).isFalse();
    }
}
