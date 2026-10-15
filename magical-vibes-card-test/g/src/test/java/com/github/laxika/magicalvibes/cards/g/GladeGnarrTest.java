package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GaeasSkyfolk;
import com.github.laxika.magicalvibes.cards.m.MournfulZombie;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GladeGnarr.class, MournfulZombie.class, GaeasSkyfolk.class})
class GladeGnarrTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+2 when any player casts a blue spell")
    void getsBoostWhenAnyPlayerCastsBlueSpell() {
        Permanent gnarr = addGnarr();

        castBlueSpell(player2);
        harness.passBothPriorities();

        assertThat(gnarr.getPowerModifier()).isEqualTo(2);
        assertThat(gnarr.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger for a nonblue spell")
    void doesNotTriggerForNonblueSpell() {
        Permanent gnarr = addGnarr();

        castSpellAsOpponent(new MournfulZombie(), "{2}{B}");

        assertThat(gnarr.getPowerModifier()).isZero();
        assertThat(gnarr.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Each blue spell cast gives another +2/+2")
    void blueSpellTriggersStack() {
        Permanent gnarr = addGnarr();

        castBlueSpell(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        castBlueSpell(player1);
        harness.passBothPriorities();

        assertThat(gnarr.getPowerModifier()).isEqualTo(4);
        assertThat(gnarr.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent gnarr = addGnarr();

        castBlueSpell(player2);
        harness.passBothPriorities();
        assertThat(gnarr.getPowerModifier()).isEqualTo(2);

        harness.passUntil(player1, TurnStep.UPKEEP);

        assertThat(gnarr.getPowerModifier()).isZero();
        assertThat(gnarr.getToughnessModifier()).isZero();
    }

    private Permanent addGnarr() {
        return harness.addToBattlefieldAndReturn(player1, new GladeGnarr());
    }

    @Test
    @DisplayName("Casting a blue spell queues the boost before the spell resolves")
    void boostResolvesBeforeBlueSpell() {
        Permanent gnarr = addGnarr();

        castBlueSpell(player1);

        assertThat(gnarr.getPowerModifier()).isZero();
        assertThat(gnarr.getToughnessModifier()).isZero();
        harness.assertNotOnBattlefield(player1, "Gaea's Skyfolk");

        harness.passBothPriorities();

        assertThat(gnarr.getPowerModifier()).isEqualTo(2);
        assertThat(gnarr.getToughnessModifier()).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Gaea's Skyfolk");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gaea's Skyfolk");
        assertThat(gnarr.getPowerModifier()).isEqualTo(2);
        assertThat(gnarr.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("A blue creature entering without being cast does not trigger the boost")
    void enteringBlueCreatureDoesNotTrigger() {
        Permanent gnarr = addGnarr();

        harness.addToBattlefield(player2, new GaeasSkyfolk());
        harness.passBothPriorities();

        assertThat(gnarr.getPowerModifier()).isZero();
        assertThat(gnarr.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Each Glade Gnarr boosts itself regardless of which player controls it")
    void bothPlayersGnarrsTriggerIndependently() {
        Permanent first = addGnarr();
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GladeGnarr());

        castBlueSpell(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getPowerModifier()).isEqualTo(2);
        assertThat(first.getToughnessModifier()).isEqualTo(2);
        assertThat(second.getPowerModifier()).isEqualTo(2);
        assertThat(second.getToughnessModifier()).isEqualTo(2);
    }

    private void castBlueSpell(Player caster) {
        if (caster == player2) {
            prepareOpponentMainPhase();
        }
        harness.castFromHand(caster, new GaeasSkyfolk(), "{G}{U}");
    }

    private void castSpellAsOpponent(Card spell, String manaCost) {
        prepareOpponentMainPhase();
        harness.castFromHand(player2, spell, manaCost);
    }

    private void prepareOpponentMainPhase() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
