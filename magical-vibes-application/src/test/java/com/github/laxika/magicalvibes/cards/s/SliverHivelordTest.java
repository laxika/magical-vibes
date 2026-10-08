package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.c.CripplingBlight;
import com.github.laxika.magicalvibes.cards.f.FleshToDust;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IntoTheVoid;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.v.VenomSliver;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SliverHivelord.class, BonescytheSliver.class, GrizzlyBears.class, Murder.class,
        VenomSliver.class, FleshToDust.class, LightningStrike.class, IntoTheVoid.class, CripplingBlight.class})
class SliverHivelordTest extends BaseCardTest {

    @Test
    @DisplayName("Sliver Hivelord grants itself indestructible (it is a Sliver)")
    void grantsSelfIndestructible() {
        Permanent hivelord = addCreatureReady(player1, new SliverHivelord());

        assertThat(gqs.hasKeyword(gd, hivelord, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Grants indestructible to another Sliver you control")
    void grantsIndestructibleToOtherSliver() {
        addCreatureReady(player1, new SliverHivelord());
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant indestructible to a non-Sliver creature")
    void doesNotGrantToNonSliver() {
        addCreatureReady(player1, new SliverHivelord());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant indestructible to an opponent's Sliver")
    void doesNotGrantToOpponentSliver() {
        addCreatureReady(player1, new SliverHivelord());
        Permanent opponentSliver = addCreatureReady(player2, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("A granted Sliver survives a destroy spell")
    void grantedSliverSurvivesDestruction() {
        addCreatureReady(player1, new SliverHivelord());
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player2);

        harness.castAndResolveInstant(player2, 0, otherSliver.getId());

        harness.assertOnBattlefield(player1, "Bonescythe Sliver");
    }

    @Test
    @DisplayName("Hivelord survives destruction even when regeneration is forbidden")
    void selfSurvivesDestructionWithoutRegeneration() {
        Permanent hivelord = addCreatureReady(player1, new SliverHivelord());
        harness.setHand(player2, List.of(new FleshToDust()));
        harness.addMana(player2, ManaColor.BLACK, 5);

        harness.castAndResolveInstant(player2, 0, hivelord.getId());

        harness.assertOnBattlefield(player1, "Sliver Hivelord");
        harness.assertNotInGraveyard(player1, "Sliver Hivelord");
    }

    @Test
    @DisplayName("A Sliver survives lethal damage, then dies when Hivelord leaves")
    void lethalDamageKillsSliverAfterSourceLeaves() {
        Permanent hivelord = addCreatureReady(player1, new SliverHivelord());
        Permanent sliver = addCreatureReady(player1, new VenomSliver());
        harness.setHand(player2, List.of(new LightningStrike(), new IntoTheVoid()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, sliver.getId());

        harness.assertOnBattlefield(player1, "Venom Sliver");
        harness.assertNotInGraveyard(player1, "Venom Sliver");
        assertThat(sliver.getMarkedDamage()).isEqualTo(3);

        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castAndResolveSorcery(player2, 0, List.of(hivelord.getId()));

        harness.assertInHand(player1, "Sliver Hivelord");
        harness.assertNotOnBattlefield(player1, "Sliver Hivelord");
        harness.assertNotOnBattlefield(player1, "Venom Sliver");
        harness.assertInGraveyard(player1, "Venom Sliver");
    }

    @Test
    @DisplayName("An undamaged Sliver loses indestructible when Hivelord leaves")
    void otherSliverLosesIndestructibleAfterSourceLeaves() {
        Permanent hivelord = addCreatureReady(player1, new SliverHivelord());
        Permanent sliver = addCreatureReady(player1, new VenomSliver());
        harness.setHand(player1, List.of(new IntoTheVoid()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, List.of(hivelord.getId()));

        harness.assertInHand(player1, "Sliver Hivelord");
        assertThat(gqs.hasKeyword(gd, sliver, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.setHand(player2, List.of(new FleshToDust()));
        harness.addMana(player2, ManaColor.BLACK, 5);

        harness.castAndResolveInstant(player2, 0, sliver.getId());

        harness.assertNotOnBattlefield(player1, "Venom Sliver");
        harness.assertInGraveyard(player1, "Venom Sliver");
    }

    @Test
    @DisplayName("Indestructible does not save a Sliver with zero toughness")
    void zeroToughnessSliverDiesDespiteIndestructible() {
        addCreatureReady(player1, new SliverHivelord());
        Permanent sliver = addCreatureReady(player1, new VenomSliver());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new CripplingBlight()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castEnchantment(player2, 0, sliver.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Venom Sliver");
        harness.assertInGraveyard(player1, "Venom Sliver");
        harness.assertOnBattlefield(player1, "Sliver Hivelord");
    }
}
