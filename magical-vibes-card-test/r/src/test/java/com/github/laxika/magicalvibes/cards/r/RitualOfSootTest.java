package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrayOgre;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GolgariLocket;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.j.JayemdaeTome;
import com.github.laxika.magicalvibes.cards.j.JoinShields;
import com.github.laxika.magicalvibes.cards.s.SwornCompanions;
import com.github.laxika.magicalvibes.cards.v.VernadiShieldmate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({RitualOfSoot.class, GrayOgre.class, GrizzlyBears.class, HillGiant.class,
        JayemdaeTome.class, GolgariLocket.class, JoinShields.class, SwornCompanions.class,
        VernadiShieldmate.class})
class RitualOfSootTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys creatures with mana value 3 or less and spares larger creatures and noncreatures")
    void destroysSmallCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrayOgre());
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player1, new JayemdaeTome());
        harness.setHand(player1, List.of(new RitualOfSoot()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Gray Ogre");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player1, "Jayemdae Tome");
    }

    @Test
    @DisplayName("Destroys a hybrid-cost creature but spares an artifact within the mana-value limit")
    void destroysHybridCreatureAndSparesSmallArtifact() {
        harness.addToBattlefield(player2, new VernadiShieldmate());
        harness.addToBattlefield(player2, new GolgariLocket());
        harness.setHand(player1, List.of(new RitualOfSoot()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player2, "Vernadi Shieldmate");
        harness.assertInGraveyard(player2, "Vernadi Shieldmate");
        harness.assertOnBattlefield(player2, "Golgari Locket");
    }

    @Test
    @DisplayName("Destroys creature tokens with no mana cost")
    void destroysCreatureTokens() {
        harness.setHand(player1, List.of(new SwornCompanions(), new RitualOfSoot()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.assertOnBattlefield(player1, "Soldier");
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Soldier");
    }

    @Test
    @DisplayName("Indestructible creatures survive while unprotected creatures are destroyed")
    void respectsIndestructible() {
        harness.addToBattlefield(player1, new VernadiShieldmate());
        harness.addToBattlefield(player2, new VernadiShieldmate());
        harness.setHand(player1, List.of(new RitualOfSoot()));
        harness.setHand(player2, List.of(new JoinShields()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player2, ManaColor.GREEN, 4);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.castAndResolveInstant(player2, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Vernadi Shieldmate");
        harness.assertInGraveyard(player1, "Vernadi Shieldmate");
        harness.assertOnBattlefield(player2, "Vernadi Shieldmate");
        harness.assertNotInGraveyard(player2, "Vernadi Shieldmate");
    }
}
