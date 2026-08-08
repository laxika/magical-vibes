package com.github.laxika.magicalvibes.cards.m;

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

class MistcallerTest extends BaseCardTest {

    private String nameOf(Permanent permanent) {
        return permanent.getCard().getName();
    }

    private void sacrificeMistcaller(Player controller) {
        harness.forceActivePlayer(controller);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(controller, new Mistcaller());
        int index = gd.playerBattlefields.get(controller.getId()).size() - 1;
        harness.activateAbility(controller, index, null, null);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
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
                .filter(permanent -> permanent.getCard().isToken() && "Human".equals(nameOf(permanent)))
                .count();
    }

    @Test
    @DisplayName("A reanimated nontoken creature is exiled instead of entering")
    void reanimatedCreatureIsExiled() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        sacrificeMistcaller(player1);

        reanimate(player2, player2);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(this::nameOf).doesNotContain("Grizzly Bears");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName()).contains("Grizzly Bears");
    }

    @Test
    @DisplayName("Creature tokens are unaffected — the replacement is nontoken only")
    void tokensStillEnter() {
        sacrificeMistcaller(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GatherTheTownsfolk()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        assertThat(humanTokenCount(player2)).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature that was cast still enters normally")
    void castCreatureStillEnters() {
        sacrificeMistcaller(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(this::nameOf).contains("Grizzly Bears");
    }

    @Test
    @DisplayName("The replacement wears off at end of turn")
    void wearsOffNextTurn() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        sacrificeMistcaller(player1);
        advanceToNextTurn(player1);

        reanimate(player2, player2);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(this::nameOf).contains("Grizzly Bears");
    }

    @Test
    @DisplayName("Without Mistcaller's ability, a reanimated creature enters normally")
    void baselineReanimationWorks() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        reanimate(player2, player2);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(this::nameOf).contains("Grizzly Bears");
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
