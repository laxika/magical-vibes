package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AetherSpellbomb;
import com.github.laxika.magicalvibes.cards.m.MyrRetriever;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Tangleroot.class, MyrRetriever.class, AetherSpellbomb.class})
class TanglerootTest extends BaseCardTest {

    @Test
    @DisplayName("A creature spell makes its caster add green mana")
    void creatureSpellMakesCasterAddGreenMana() {
        harness.addToBattlefield(player1, new Tangleroot());
        harness.castFromHand(player1, new MyrRetriever(), "{2}");
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature spell makes an opponent caster add green mana")
    void opponentCreatureSpellMakesOpponentAddGreenMana() {
        harness.addToBattlefield(player1, new Tangleroot());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new MyrRetriever(), "{2}");
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Tangleroot triggers for the same creature spell")
    void eachTanglerootTriggers() {
        harness.addToBattlefield(player1, new Tangleroot());
        harness.addToBattlefield(player1, new Tangleroot());

        harness.castFromHand(player1, new MyrRetriever(), "{2}");
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("A noncreature spell does not trigger Tangleroot")
    void noncreatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new Tangleroot());
        harness.castFromHand(player1, new AetherSpellbomb(), "{1}");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).hasSize(1);
    }
}
