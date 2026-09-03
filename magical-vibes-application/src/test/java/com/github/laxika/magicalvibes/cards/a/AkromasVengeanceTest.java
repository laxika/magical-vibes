package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PithingNeedle;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({AkromasVengeance.class, Forest.class, GrizzlyBears.class, PithingNeedle.class, RuleOfLaw.class})
class AkromasVengeanceTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all artifacts, creatures, and enchantments")
    void destroysArtifactsCreaturesAndEnchantments() {
        harness.addToBattlefield(player1, new PithingNeedle());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new RuleOfLaw());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new AkromasVengeance()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pithing Needle");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Rule of Law");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Cycling {3} discards Akroma's Vengeance and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new AkromasVengeance()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Akroma's Vengeance");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
