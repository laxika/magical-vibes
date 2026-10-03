package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.Sunforger;
import com.github.laxika.magicalvibes.cards.s.SupremeVerdict;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodFunnel.class, BorosRecruit.class, Sunforger.class, SupremeVerdict.class})
class BloodFunnelTest extends BaseCardTest {

    @Test
    @DisplayName("Noncreature spells cost {2} less to cast")
    void noncreatureSpellsCostTwoLess() {
        harness.addToBattlefield(player1, new BloodFunnel());
        harness.castFromHand(player1, new Sunforger(), "{1}");

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Sunforger"));
    }

    @Test
    @DisplayName("Sacrificing a creature lets a noncreature spell resolve")
    void sacrificingCreatureLetsSpellResolve() {
        harness.addToBattlefield(player1, new BloodFunnel());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());

        harness.castFromHand(player1, new Sunforger(), "{1}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sunforger");
        harness.assertNotOnBattlefield(player1, "Boros Recruit");
    }

    @Test
    @DisplayName("A noncreature spell is countered when its controller declines to sacrifice a creature")
    void decliningSacrificeCountersSpell() {
        harness.addToBattlefield(player1, new BloodFunnel());
        harness.addToBattlefield(player1, new BorosRecruit());

        harness.castFromHand(player1, new Sunforger(), "{1}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Sunforger");
    }

    @Test
    @DisplayName("A noncreature spell is countered immediately when its controller controls no creature")
    void noncreatureSpellIsCounteredWithoutCreatureToSacrifice() {
        harness.addToBattlefield(player1, new BloodFunnel());
        harness.castFromHand(player1, new Sunforger(), "{1}");

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sunforger");
    }

    @Test
    @DisplayName("Creature spells are neither reduced nor countered")
    void creatureSpellsAreUnaffected() {
        harness.addToBattlefield(player1, new BloodFunnel());
        harness.setHand(player1, List.of(new BorosRecruit()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Boros Recruit");
    }

    @Test
    @DisplayName("An opponent's noncreature spell is not reduced")
    void opponentsNoncreatureSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new BloodFunnel());
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castFromHand(player2, new Sunforger(), "{2}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger Blood Funnel")
    void opponentsNoncreatureSpellsAreNotCountered() {
        harness.addToBattlefield(player1, new BloodFunnel());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new Sunforger(), "{3}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Sunforger");
    }

    @Test
    @DisplayName("An uncounterable spell still allows a creature sacrifice")
    void uncounterableSpellStillAllowsSacrifice() {
        harness.addToBattlefield(player1, new BloodFunnel());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());

        harness.castFromHand(player1, new SupremeVerdict(), "{W}{W}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creature.getId());

        harness.assertInGraveyard(player1, "Boros Recruit");
        harness.assertNotInGraveyard(player1, "Supreme Verdict");
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Supreme Verdict"));

        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Supreme Verdict");
    }

    @Test
    @DisplayName("Multiple Funnels reduce costs cumulatively but each requires a separate sacrifice")
    void multipleFunnelsRequireSeparateSacrifices() {
        harness.addToBattlefield(player1, new BloodFunnel());
        harness.addToBattlefield(player1, new BloodFunnel());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());

        harness.castFromHand(player1, new Sunforger(), "");
        assertThat(gd.stack).hasSize(3);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Boros Recruit");
        harness.assertInGraveyard(player1, "Sunforger");
        harness.assertNotOnBattlefield(player1, "Sunforger");
    }

    @Test
    @DisplayName("The reduction does not pay colored mana or let a Funnel counter itself")
    void coloredManaIsStillRequiredAndOnlyExistingFunnelTriggers() {
        harness.addToBattlefield(player1, new BloodFunnel());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());

        assertThatThrownBy(() -> harness.castFromHand(player1, new BloodFunnel(), ""))
                .isInstanceOf(IllegalStateException.class);
        harness.castFromHand(player1, new BloodFunnel(), "{B}");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Blood Funnel"))
                .hasSize(2);
        harness.assertInGraveyard(player1, "Boros Recruit");
    }

    @Test
    @DisplayName("An opponent's creature cannot satisfy the sacrifice")
    void opponentsCreatureCannotSatisfySacrifice() {
        harness.addToBattlefield(player1, new BloodFunnel());
        harness.addToBattlefield(player2, new BorosRecruit());

        harness.castFromHand(player1, new Sunforger(), "{1}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sunforger");
        harness.assertOnBattlefield(player2, "Boros Recruit");
    }
}
