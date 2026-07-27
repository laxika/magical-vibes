package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class TranquilityTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys enchantments controlled by both players")
    void destroysEnchantmentsFromBothPlayers() {
        harness.addToBattlefield(player1, new RuleOfLaw());
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new Tranquility()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Rule of Law");
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player1, "Rule of Law");
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Destroys auras attached to creatures but not the creatures")
    void destroysAurasButNotCreatures() {
        GrizzlyBears bearsCard = new GrizzlyBears();
        Permanent bears = new Permanent(bearsCard);
        bears.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(bears);

        HolyStrength aura = new HolyStrength();
        Permanent auraPerm = new Permanent(aura);
        auraPerm.setAttachedTo(bears.getId());
        harness.getGameData().playerBattlefields.get(player1.getId()).add(auraPerm);

        harness.setHand(player1, List.of(new Tranquility()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Holy Strength");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Holy Strength");
    }

    @Test
    @DisplayName("Does not destroy creatures")
    void doesNotDestroyCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Tranquility()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
