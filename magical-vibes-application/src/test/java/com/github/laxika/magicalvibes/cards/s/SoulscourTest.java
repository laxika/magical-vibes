package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BlinkmothNexus;
import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({Soulscour.class, CrazedGoblin.class, BlinkmothNexus.class, SpireGolem.class, Skullclamp.class,
        DarksteelCitadel.class})
class SoulscourTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all nonartifact permanents and leaves artifacts on the battlefield")
    void destroysAllNonartifactPermanents() {
        harness.addToBattlefield(player1, new CrazedGoblin());
        harness.addToBattlefield(player2, new BlinkmothNexus());
        harness.addToBattlefield(player1, new SpireGolem());
        harness.addToBattlefield(player2, new Skullclamp());

        harness.setHand(player1, List.of(new Soulscour()));
        harness.addMana(player1, ManaColor.WHITE, 10);

        harness.castAndResolveSorcery(player1, 0, List.of());

        harness.assertNotOnBattlefield(player1, "Crazed Goblin");
        harness.assertNotOnBattlefield(player2, "Blinkmoth Nexus");
        harness.assertOnBattlefield(player1, "Spire Golem");
        harness.assertOnBattlefield(player2, "Skullclamp");
        harness.assertInGraveyard(player1, "Soulscour");
    }

    @Test
    @DisplayName("An animated Blinkmoth Nexus survives while an unanimated Nexus is destroyed")
    void usesArtifactTypesAtResolution() {
        harness.addToBattlefield(player1, new BlinkmothNexus());
        harness.addToBattlefield(player2, new BlinkmothNexus());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Soulscour()));
        harness.addMana(player1, ManaColor.WHITE, 10);
        harness.castAndResolveSorcery(player1, 0, List.of());

        harness.assertOnBattlefield(player1, "Blinkmoth Nexus");
        harness.assertNotOnBattlefield(player2, "Blinkmoth Nexus");
        harness.assertInGraveyard(player2, "Blinkmoth Nexus");
        harness.assertInGraveyard(player1, "Soulscour");
    }

    @Test
    @DisplayName("Artifact lands survive Soulscour")
    void preservesArtifactLands() {
        harness.addToBattlefield(player1, new DarksteelCitadel());
        harness.addToBattlefield(player2, new DarksteelCitadel());
        harness.addToBattlefield(player2, new CrazedGoblin());
        harness.setHand(player1, List.of(new Soulscour()));
        harness.addMana(player1, ManaColor.WHITE, 10);

        harness.castAndResolveSorcery(player1, 0, List.of());

        harness.assertOnBattlefield(player1, "Darksteel Citadel");
        harness.assertOnBattlefield(player2, "Darksteel Citadel");
        harness.assertInGraveyard(player2, "Crazed Goblin");
        harness.assertInGraveyard(player1, "Soulscour");
    }

    @Test
    @DisplayName("Soulscour resolves on an empty battlefield without targets")
    void resolvesOnEmptyBattlefield() {
        harness.setHand(player1, List.of(new Soulscour()));
        harness.addMana(player1, ManaColor.WHITE, 10);

        harness.castAndResolveSorcery(player1, 0, List.of());

        harness.assertInGraveyard(player1, "Soulscour");
    }
}
