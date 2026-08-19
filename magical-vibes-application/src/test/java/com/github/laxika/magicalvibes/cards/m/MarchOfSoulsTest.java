package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MarchOfSoulsTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all creatures and gives each creature's controller a Spirit per creature destroyed")
    void destroysCreaturesAndCreatesSpiritsForTheirControllers() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());

        castMarchOfSouls();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(plains);
        assertThat(spiritTokens(player1)).hasSize(1);
        assertThat(spiritTokens(player2)).hasSize(2);
        assertThat(spiritTokens(player1)).allSatisfy(this::assertSpirit);
        assertThat(spiritTokens(player2)).allSatisfy(this::assertSpirit);
    }

    @Test
    @DisplayName("Does not create a Spirit for a creature that cannot be destroyed")
    void indestructibleCreaturesSurviveWithoutCreatingSpirits() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent indestructible = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        indestructible.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        castMarchOfSouls();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(indestructible);
        assertThat(spiritTokens(player1)).hasSize(1);
        assertThat(spiritTokens(player2)).isEmpty();
    }

    private void castMarchOfSouls() {
        harness.setHand(player1, List.of(new MarchOfSouls()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private List<Permanent> spiritTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Spirit"))
                .toList();
    }

    private void assertSpirit(Permanent spirit) {
        assertThat(spirit.getCard().getPower()).isEqualTo(1);
        assertThat(spirit.getCard().getToughness()).isEqualTo(1);
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(spirit.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(spirit.getCard().isToken()).isTrue();
    }
}
