package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AuraBarbs;
import com.github.laxika.magicalvibes.cards.f.Frostling;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OgreRecluse.class, Frostling.class, AuraBarbs.class})
class OgreRecluseTest extends BaseCardTest {

    @Test
    @DisplayName("Controller casting a spell taps Ogre Recluse")
    void controllerCastingSpellTapsOgreRecluse() {
        Permanent recluse = addReadyRecluse(player1);
        castFrostling(player1);

        assertThat(recluse.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting a noncreature spell taps Ogre Recluse")
    void castingNoncreatureSpellTapsOgreRecluse() {
        Permanent recluse = addReadyRecluse(player1);

        harness.castFromHand(player1, new AuraBarbs(), "{2}{R}");
        harness.passBothPriorities();

        assertThat(recluse.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opponent casting a spell taps Ogre Recluse")
    void opponentCastingSpellTapsOgreRecluse() {
        Permanent recluse = addReadyRecluse(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        castFrostling(player2);

        assertThat(recluse.isTapped()).isTrue();
    }

    private Permanent addReadyRecluse(Player player) {
        return addCreatureReady(player, new OgreRecluse());
    }

    private void castFrostling(Player player) {
        harness.castFromHand(player, new Frostling(), "{R}");
        harness.passBothPriorities();
    }
}
