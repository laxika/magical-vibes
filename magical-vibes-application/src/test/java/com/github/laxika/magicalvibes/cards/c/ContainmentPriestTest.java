package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GatherTheTownsfolk;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.Zombify;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ContainmentPriestTest extends BaseCardTest {

    private void castContainmentPriest(Player controller) {
        harness.forceActivePlayer(controller);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(controller, List.of(new ContainmentPriest()));
        harness.addMana(controller, ManaColor.WHITE, 2);
        harness.castCreature(controller, 0);
        harness.passBothPriorities();
    }

    private void reanimate(Player caster, Player graveyardOwner) {
        Card target = gd.playerGraveyards.get(graveyardOwner.getId()).getFirst();
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(caster, List.of(new Zombify()));
        harness.addMana(caster, ManaColor.BLACK, 4);
        harness.castSorcery(caster, 0, target.getId());
        harness.passBothPriorities();
    }

    private long humanTokenCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && "Human".equals(permanent.getCard().getName()))
                .count();
    }

    @Test
    @DisplayName("A nontoken creature entering without being cast is exiled")
    void reanimatedCreatureIsExiled() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        castContainmentPriest(player1);

        reanimate(player2, player2);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .doesNotContain("Grizzly Bears");
        assertThat(gd.exiledCards)
                .extracting(entry -> entry.card().getName())
                .contains("Grizzly Bears");
    }

    @Test
    @DisplayName("Creature tokens enter normally")
    void tokensStillEnter() {
        castContainmentPriest(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GatherTheTownsfolk()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        assertThat(humanTokenCount(player2)).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature spell enters normally")
    void castCreatureStillEnters() {
        castContainmentPriest(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .contains("Grizzly Bears");
    }
}
