package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NaturalAffinityTest extends BaseCardTest {

    private void cast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new NaturalAffinity()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent land(GameData gd, com.github.laxika.magicalvibes.model.Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElse(null);
    }

    @Test
    @DisplayName("Animates lands of both players as 2/2 creatures, still lands")
    void animatesAllLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Mountain());

        cast();
        GameData gd = harness.getGameData();

        Permanent ownForest = land(gd, player1, "Forest");
        assertThat(ownForest).isNotNull();
        assertThat(ownForest.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(ownForest.getEffectivePower()).isEqualTo(2);
        assertThat(ownForest.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.isCreature(gd, ownForest)).isTrue();
        assertThat(ownForest.getCard().hasType(CardType.LAND)).isTrue();

        Permanent opponentMountain = land(gd, player2, "Mountain");
        assertThat(opponentMountain).isNotNull();
        assertThat(opponentMountain.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(opponentMountain.getEffectivePower()).isEqualTo(2);
        assertThat(opponentMountain.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.isCreature(gd, opponentMountain)).isTrue();
    }

    @Test
    @DisplayName("Does not animate non-land permanents")
    void doesNotAnimateNonLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());

        cast();
        GameData gd = harness.getGameData();

        Permanent bears = land(gd, player1, "Grizzly Bears");
        assertThat(bears).isNotNull();
        assertThat(bears.isAnimatedUntilEndOfTurn()).isFalse();
    }

    @Test
    @DisplayName("Animation wears off at end of turn (resetModifiers)")
    void animationWearsOff() {
        harness.addToBattlefield(player1, new Forest());

        cast();
        GameData gd = harness.getGameData();

        Permanent forest = land(gd, player1, "Forest");
        assertThat(forest).isNotNull();
        assertThat(forest.isAnimatedUntilEndOfTurn()).isTrue();

        forest.resetModifiers();

        assertThat(forest.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, forest)).isFalse();
    }
}
