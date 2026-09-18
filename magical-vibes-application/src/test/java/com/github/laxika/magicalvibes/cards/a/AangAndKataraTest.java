package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AangAndKatara.class, GrizzlyBears.class, Ornithopter.class})
class AangAndKataraTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates one Ally token for each tapped artifact or creature you control")
    void enteringCountsTappedArtifactsAndCreatures() {
        Permanent tappedBear = addCreatureReady(player1, new GrizzlyBears());
        tappedBear.tap();
        Permanent tappedOrnithopter = addCreatureReady(player1, new Ornithopter());
        tappedOrnithopter.tap();
        addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentTappedBear = addCreatureReady(player2, new GrizzlyBears());
        opponentTappedBear.tap();

        castAangAndKatara();

        assertThat(countTokens(player1)).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking creates tokens based on the tapped permanents at resolution")
    void attackingCountsTappedPermanents() {
        addCreatureReady(player1, new AangAndKatara());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(countTokens(player1)).isEqualTo(1);
    }

    private void castAangAndKatara() {
        harness.setHand(player1, List.of(new AangAndKatara()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    private long countTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count();
    }
}
