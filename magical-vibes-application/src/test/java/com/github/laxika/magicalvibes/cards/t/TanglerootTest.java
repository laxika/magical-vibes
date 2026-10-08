package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AetherSpellbomb;
import com.github.laxika.magicalvibes.cards.a.Annul;
import com.github.laxika.magicalvibes.cards.m.MyrRetriever;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Tangleroot.class, MyrRetriever.class, AetherSpellbomb.class, Annul.class, Shatter.class})
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

    @Test
    @DisplayName("Mana is added only when the trigger resolves, before the creature spell resolves")
    void manaIsAddedWhenTriggerResolves() {
        harness.addToBattlefield(player1, new Tangleroot());
        harness.castFromHand(player1, new MyrRetriever(), "{2}");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Myr Retriever");

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Myr Retriever");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Myr Retriever");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Countering the creature spell does not stop the mana trigger")
    void counteredCreatureStillGrantsMana() {
        harness.addToBattlefield(player1, new Tangleroot());
        MyrRetriever creature = new MyrRetriever();
        harness.castFromHand(player1, creature, "{2}");
        harness.setHand(player2, List.of(new Annul()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        harness.assertInGraveyard(player1, "Myr Retriever");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();

        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Myr Retriever");
    }

    @Test
    @DisplayName("Removing Tangleroot does not stop its pending mana trigger")
    void triggerSurvivesSourceRemoval() {
        var tangleroot = harness.addToBattlefieldAndReturn(player1, new Tangleroot());
        harness.castFromHand(player1, new MyrRetriever(), "{2}");
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, tangleroot.getId());

        harness.assertInGraveyard(player1, "Tangleroot");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();

        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Myr Retriever");
    }

    @Test
    @DisplayName("A creature entering without being cast does not trigger Tangleroot")
    void enteringWithoutCastingDoesNotTrigger() {
        harness.addToBattlefield(player1, new Tangleroot());
        harness.enterBattlefieldAndReturn(player1, new MyrRetriever());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        harness.assertOnBattlefield(player1, "Myr Retriever");
    }

    @Test
    @DisplayName("Tangleroots controlled by different players both award mana to the caster")
    void tanglerootsWithDifferentControllersAwardManaToCaster() {
        harness.addToBattlefield(player1, new Tangleroot());
        harness.addToBattlefield(player2, new Tangleroot());
        harness.castFromHand(player1, new MyrRetriever(), "{2}");

        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
        harness.assertOnBattlefield(player1, "Myr Retriever");
    }

    @Test
    @DisplayName("Tangleroot triggers for every creature spell cast in the same turn")
    void triggersForEveryCreatureSpellInSameTurn() {
        harness.addToBattlefield(player1, new Tangleroot());
        harness.castFromHand(player1, new MyrRetriever(), "{2}");
        resolveAllTriggers();

        harness.castFromHand(player1, new MyrRetriever(), "{2}");
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(countPermanents(player1, "Myr Retriever")).isEqualTo(2);
    }
}
