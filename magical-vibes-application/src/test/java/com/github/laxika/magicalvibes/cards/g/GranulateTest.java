package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AetherSpellbomb;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MyrEnforcer;
import com.github.laxika.magicalvibes.cards.s.SeatOfTheSynod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class GranulateTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys each nonland artifact with mana value 4 or less")
    void destroysMatchingArtifactsAcrossBothBattlefields() {
        harness.addToBattlefield(player1, new AetherSpellbomb());
        harness.addToBattlefield(player1, new MyrEnforcer());
        harness.addToBattlefield(player1, new SeatOfTheSynod());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new AetherSpellbomb());

        harness.setHand(player1, List.of(new Granulate()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Aether Spellbomb");
        harness.assertNotOnBattlefield(player2, "Aether Spellbomb");
        harness.assertOnBattlefield(player1, "Myr Enforcer");
        harness.assertOnBattlefield(player1, "Seat of the Synod");
        harness.assertOnBattlefield(player1, "Forest");
    }
}
