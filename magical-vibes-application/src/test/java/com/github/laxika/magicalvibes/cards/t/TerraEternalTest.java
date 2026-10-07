package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.Armageddon;
import com.github.laxika.magicalvibes.cards.a.AshayaSoulOfTheWild;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NaturesClaim;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TerraEternal.class, Plains.class, Forest.class, GrizzlyBears.class, Armageddon.class, NaturesClaim.class, Opalescence.class, AshayaSoulOfTheWild.class})
class TerraEternalTest extends BaseCardTest {

    @Test
    @DisplayName("All lands have indestructible regardless of who controls them")
    void grantsIndestructibleToAllLands() {
        harness.addToBattlefield(player1, new TerraEternal());
        Permanent ownPlains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        assertThat(gqs.hasKeyword(gd, ownPlains, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentForest, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Terra Eternal does not grant indestructible to nonlands")
    void doesNotAffectNonlands() {
        harness.addToBattlefield(player1, new TerraEternal());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Lands survive Armageddon while Terra Eternal is on the battlefield")
    void landsSurviveArmageddon() {
        harness.addToBattlefield(player1, new TerraEternal());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Forest());
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new Armageddon(), "{3}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Plains");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player1, "Terra Eternal");
    }

    @Test
    @DisplayName("Resolving Terra Eternal protects lands already on the battlefield")
    void protectsExistingLandsWhenItResolves() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.castFromHand(player1, new TerraEternal(), "{2}{W}");
        assertThat(gqs.hasKeyword(gd, land, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, land, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Destroying Terra Eternal removes protection and allows lands to be destroyed")
    void landsCanBeDestroyedAfterTerraEternalLeaves() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new TerraEternal());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new NaturesClaim()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, source.getId());

        harness.assertInGraveyard(player1, "Terra Eternal");
        assertThat(gqs.hasKeyword(gd, land, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.castFromHand(player1, new Armageddon(), "{3}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("A second Terra Eternal continues protecting lands after the first is destroyed")
    void anotherCopyKeepsLandsIndestructible() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new TerraEternal());
        harness.addToBattlefield(player2, new TerraEternal());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.setHand(player1, List.of(new NaturesClaim()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, source.getId());

        harness.assertInGraveyard(player1, "Terra Eternal");
        harness.assertOnBattlefield(player2, "Terra Eternal");
        assertThat(gqs.hasKeyword(gd, land, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Terra Eternal protects itself when it becomes a land without losing its ability")
    void protectsItselfWhenItBecomesALand() {
        harness.addToBattlefield(player1, new Opalescence());
        harness.addToBattlefield(player1, new AshayaSoulOfTheWild());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new TerraEternal());

        assertThat(gqs.hasKeyword(gd, source, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.setHand(player1, List.of(new NaturesClaim()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, source.getId());

        harness.assertOnBattlefield(player1, "Terra Eternal");
        harness.assertNotInGraveyard(player1, "Terra Eternal");
    }
}
