package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.setHand(player1, List.of(new RampageOfTheClans()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
