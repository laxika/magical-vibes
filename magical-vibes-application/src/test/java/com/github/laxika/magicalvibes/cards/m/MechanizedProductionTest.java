package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.p.PowerstoneShard;
import com.github.laxika.magicalvibes.cards.w.WeldingJar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MechanizedProductionTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a token copy of the enchanted artifact at your upkeep")
    void createsTokenCopyOfEnchantedArtifact() {
        Permanent artifact = addArtifact(new WeldingJar());
        castProduction(artifact);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Welding Jar"));
    }

    @Test
    @DisplayName("Wins after the token makes eight same-name artifacts")
    void winsWithEightArtifactsOfTheSameName() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new PowerstoneShard());
        }
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PowerstoneShard());
        castProduction(artifact);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Does not win before the token creates eight same-name artifacts")
    void doesNotWinBelowEightArtifacts() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new PowerstoneShard());
        }
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PowerstoneShard());
        castProduction(artifact);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.winnerPlayerId).isNull();
    }

    @Test
    @DisplayName("Cannot enchant an artifact controlled by an opponent")
    void cannotEnchantOpponentsArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new WeldingJar());
        harness.setHand(player1, List.of(new MechanizedProduction()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact you control");
    }

    private Permanent addArtifact(Card artifact) {
        harness.addToBattlefield(player1, artifact);
        return gd.playerBattlefields.get(player1.getId()).getLast();
    }

    private void castProduction(Permanent artifact) {
        harness.setHand(player1, List.of(new MechanizedProduction()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();
    }
}
