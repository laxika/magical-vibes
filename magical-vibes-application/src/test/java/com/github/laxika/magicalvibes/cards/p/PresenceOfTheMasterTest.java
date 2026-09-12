package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.b.BrilliantHalo;
import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({PresenceOfTheMaster.class, AngelicChorus.class, BrilliantHalo.class, DarkRitual.class,
        GorillaWarrior.class})
class PresenceOfTheMasterTest extends BaseCardTest {

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
    }
}
