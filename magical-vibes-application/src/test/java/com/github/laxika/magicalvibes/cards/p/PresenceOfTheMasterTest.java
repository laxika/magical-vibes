package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.b.BrilliantHalo;
import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PresenceOfTheMaster.class, AngelicChorus.class, BrilliantHalo.class, DarkRitual.class,
        Disenchant.class, GorillaWarrior.class})
class PresenceOfTheMasterTest extends BaseCardTest {

    @Test
    @DisplayName("Does not counter itself when no Presence is on the battlefield")
    void doesNotCounterItself() {
        harness.castFromHand(player1, new PresenceOfTheMaster(), "{3}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Presence of the Master");
        harness.assertNotInGraveyard(player1, "Presence of the Master");
    }

    @Test
    @DisplayName("An existing Presence counters another Presence spell")
    void countersAnotherPresence() {
        harness.addToBattlefield(player1, new PresenceOfTheMaster());
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new PresenceOfTheMaster(), "{3}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Presence of the Master");
        harness.assertNotOnBattlefield(player2, "Presence of the Master");
        harness.assertInGraveyard(player2, "Presence of the Master");
    }

    @Test
    @DisplayName("Still counters the enchantment after its source is destroyed in response")
    void triggerSurvivesSourceRemoval() {
        Permanent presence = harness.addToBattlefieldAndReturn(player1, new PresenceOfTheMaster());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new AngelicChorus(), "{3}{W}{W}");

        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, presence.getId());

        harness.assertInGraveyard(player1, "Presence of the Master");
        harness.assertNotOnBattlefield(player1, "Presence of the Master");
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Angelic Chorus");
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Counters an enchantment spell cast by an opponent")
    void countersOpponentsEnchantmentSpell() {
        harness.addToBattlefield(player1, new PresenceOfTheMaster());

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new AngelicChorus(), "{3}{W}{W}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Angelic Chorus");
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Multiple Presences safely resolve their triggers for the same spell")
    void multiplePresencesCounterSameSpell() {
        harness.addToBattlefield(player1, new PresenceOfTheMaster());
        harness.addToBattlefield(player2, new PresenceOfTheMaster());

        harness.castFromHand(player1, new AngelicChorus(), "{3}{W}{W}");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Angelic Chorus");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Angelic Chorus"))
                .hasSize(1);
        harness.assertOnBattlefield(player1, "Presence of the Master");
        harness.assertOnBattlefield(player2, "Presence of the Master");
    }

    @Test
    @DisplayName("Allows a non-enchantment permanent spell to resolve")
    void allowsCreatureSpell() {
        harness.addToBattlefield(player1, new PresenceOfTheMaster());

        harness.castFromHand(player1, new GorillaWarrior(), "{2}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gorilla Warrior");
        harness.assertNotInGraveyard(player1, "Gorilla Warrior");
    }

    @Test
    @DisplayName("Counters an enchantment spell cast by its controller")
    void countersControllersEnchantmentSpell() {
        harness.addToBattlefield(player1, new PresenceOfTheMaster());

        harness.castFromHand(player1, new AngelicChorus(), "{3}{W}{W}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Angelic Chorus");
        harness.assertNotOnBattlefield(player1, "Angelic Chorus");
    }

    @Test
    @DisplayName("Counters a targeted Aura spell")
    void countersTargetedAuraSpell() {
        harness.addToBattlefield(player1, new PresenceOfTheMaster());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GorillaWarrior());
        harness.setHand(player2, List.of(new BrilliantHalo()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.castEnchantment(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Brilliant Halo");
        harness.assertNotOnBattlefield(player2, "Brilliant Halo");
    }

    @Test
    @DisplayName("Does not trigger for a non-enchantment spell")
    void doesNotTriggerForNonEnchantmentSpell() {
        harness.addToBattlefield(player1, new PresenceOfTheMaster());

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new DarkRitual(), "{B}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Dark Ritual");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(3);
    }
}
