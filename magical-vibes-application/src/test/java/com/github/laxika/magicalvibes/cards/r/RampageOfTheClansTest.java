package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GlassOfTheGuildpact;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.SphinxOfTheGuildpact;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RampageOfTheClans.class, Millstone.class, IcyManipulator.class,
        GloriousAnthem.class, GrizzlyBears.class, Forest.class,
        GlassOfTheGuildpact.class, SphinxOfTheGuildpact.class})
class RampageOfTheClansTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys artifacts and enchantments and creates a Centaur for each controller")
    void destroysArtifactsAndEnchantmentsAndCreatesTokens() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new IcyManipulator());
        Permanent opponentEnchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        castRampageOfTheClans();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(ownArtifact.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(opponentArtifact.getId())
                        || permanent.getId().equals(opponentEnchantment.getId()))
                .anyMatch(permanent -> permanent.getId().equals(opponentCreature.getId()))
                .anyMatch(permanent -> permanent.getId().equals(opponentLand.getId()));

        assertThat(centaurs(player1)).hasSize(1).allSatisfy(this::assertCentaur);
        assertThat(centaurs(player2)).hasSize(2).allSatisfy(this::assertCentaur);
    }

    @Test
    @DisplayName("Does not create a token for an indestructible artifact")
    void indestructibleArtifactSurvivesWithoutCreatingToken() {
        Permanent indestructibleArtifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        indestructibleArtifact.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        castRampageOfTheClans();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(indestructibleArtifact.getId()));
        assertThat(centaurs(player2)).isEmpty();
    }

    @Test
    @DisplayName("Resolves without creating tokens when no artifacts or enchantments exist")
    void emptyBattlefieldCreatesNoTokens() {
        castRampageOfTheClans();

        assertThat(centaurs(player1)).isEmpty();
        assertThat(centaurs(player2)).isEmpty();
        harness.assertInGraveyard(player1, "Rampage of the Clans");
    }

    @Test
    @DisplayName("Destroys an artifact creature despite hexproof from monocolored")
    void destroysArtifactCreatureWithoutTargeting() {
        harness.addToBattlefield(player2, new SphinxOfTheGuildpact());

        castRampageOfTheClans();

        harness.assertNotOnBattlefield(player2, "Sphinx of the Guildpact");
        harness.assertInGraveyard(player2, "Sphinx of the Guildpact");
        assertThat(centaurs(player2)).hasSize(1).allSatisfy(this::assertCentaur);
    }

    @Test
    @DisplayName("A regenerated artifact creature survives and does not earn a Centaur")
    void regeneratedArtifactCreatureCreatesNoToken() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player2, new SphinxOfTheGuildpact());
        sphinx.setRegenerationShield(1);
        harness.addToBattlefield(player2, new GlassOfTheGuildpact());

        castRampageOfTheClans();

        harness.assertOnBattlefield(player2, "Sphinx of the Guildpact");
        assertThat(sphinx.isTapped()).isTrue();
        assertThat(sphinx.getRegenerationShield()).isZero();
        harness.assertInGraveyard(player2, "Glass of the Guildpact");
        assertThat(centaurs(player2)).hasSize(1).allSatisfy(this::assertCentaur);
    }

    @Test
    @DisplayName("Creates a Centaur for a destroyed artifact token")
    void destroyedArtifactTokenCreatesCentaur() {
        GlassOfTheGuildpact tokenCopy = new GlassOfTheGuildpact();
        tokenCopy.setToken(true);
        harness.addToBattlefield(player2, tokenCopy);

        castRampageOfTheClans();

        harness.assertNotOnBattlefield(player2, "Glass of the Guildpact");
        assertThat(centaurs(player2)).hasSize(1).allSatisfy(this::assertCentaur);
    }

    @Test
    @DisplayName("The controller of a stolen artifact gets the Centaur rather than its owner")
    void destroyedArtifactRewardsControllerRatherThanOwner() {
        GlassOfTheGuildpact stolenArtifact = new GlassOfTheGuildpact();
        stolenArtifact.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, stolenArtifact);

        castRampageOfTheClans();

        harness.assertInGraveyard(player1, "Glass of the Guildpact");
        assertThat(centaurs(player1)).isEmpty();
        assertThat(centaurs(player2)).hasSize(1).allSatisfy(this::assertCentaur);
    }

    private List<Permanent> centaurs(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.CENTAUR))
                .toList();
    }

    private void assertCentaur(Permanent centaur) {
        assertThat(centaur.getCard().getPower()).isEqualTo(3);
        assertThat(centaur.getCard().getToughness()).isEqualTo(3);
        assertThat(centaur.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(centaur.getCard().getSubtypes()).containsExactly(CardSubtype.CENTAUR);
    }

    private void castRampageOfTheClans() {
        harness.castFromHand(player1, new RampageOfTheClans(), "{3}{G}");
        harness.passBothPriorities();
    }
}
