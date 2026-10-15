package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BloodFountain;
import com.github.laxika.magicalvibes.cards.b.BolassCitadel;
import com.github.laxika.magicalvibes.cards.s.StitchedAssistant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;


import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PathOfPeril.class, GrizzlyBears.class, HillGiant.class, BloodFountain.class, BolassCitadel.class, PersistentSpecimen.class, StitchedAssistant.class})
class PathOfPerilTest extends BaseCardTest {

    @Test
    @DisplayName("Normal cast destroys creatures with mana value 2 or less")
    void normalCastDestroysOnlyLowManaValueCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent ownGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent enemyGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new PathOfPeril()));
        addNormalMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .containsExactly(ownGiant.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .containsExactly(enemyGiant.getId());
    }

    @Test
    @DisplayName("Cleave cast destroys all creatures")
    void cleaveCastDestroysAllCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new PathOfPeril()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @CardUsed({PathOfPeril.class, PersistentSpecimen.class, StitchedAssistant.class, BloodFountain.class})
    void normalCastSparesThreeManaCreatureAndNoncreatureArtifact() {
        harness.addToBattlefield(player1, new PersistentSpecimen());
        harness.addToBattlefield(player2, new StitchedAssistant());
        harness.addToBattlefield(player2, new BloodFountain());
        harness.setHand(player1, List.of(new PathOfPeril()));
        addNormalMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Persistent Specimen");
        harness.assertInGraveyard(player1, "Persistent Specimen");
        harness.assertOnBattlefield(player2, "Stitched Assistant");
        harness.assertOnBattlefield(player2, "Blood Fountain");
        harness.assertInGraveyard(player1, "Path of Peril");
    }

    @Test
    @CardUsed({PathOfPeril.class, StitchedAssistant.class, BloodFountain.class})
    void cleaveUsesExactCostAndSparesNoncreatureArtifact() {
        harness.addToBattlefield(player2, new StitchedAssistant());
        harness.addToBattlefield(player2, new BloodFountain());
        harness.setHand(player1, List.of(new PathOfPeril()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Stitched Assistant");
        harness.assertInGraveyard(player2, "Stitched Assistant");
        harness.assertOnBattlefield(player2, "Blood Fountain");
        harness.assertInGraveyard(player1, "Path of Peril");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @CardUsed({PathOfPeril.class, BolassCitadel.class, PersistentSpecimen.class, StitchedAssistant.class})
    void payingLifeThroughCitadelDoesNotPayCleaveCost() {
        harness.addToBattlefield(player1, new BolassCitadel());
        harness.addToBattlefield(player1, new PersistentSpecimen());
        harness.addToBattlefield(player2, new StitchedAssistant());
        harness.setLibrary(player1, List.of(new PathOfPeril()));

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertLife(player1, 17);
        harness.assertInGraveyard(player1, "Persistent Specimen");
        harness.assertOnBattlefield(player2, "Stitched Assistant");
        harness.assertOnBattlefield(player1, "Bolas's Citadel");
        harness.assertInGraveyard(player1, "Path of Peril");
    }

    private void addNormalMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
