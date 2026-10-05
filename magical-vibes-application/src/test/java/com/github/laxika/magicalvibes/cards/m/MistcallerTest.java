package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DwarvenPriest;
import com.github.laxika.magicalvibes.cards.g.GatherTheTownsfolk;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.Zombify;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Mistcaller.class, GrizzlyBears.class, Zombify.class, GatherTheTownsfolk.class, DwarvenPriest.class})
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
        harness.castAndResolveSorcery(caster, 0, target.getId());
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
        harness.castAndResolveSorcery(player2, 0, 0);

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

    @Test
    @DisplayName("Mistcaller is sacrificed immediately, even while tapped and newly entered")
    void sacrificeIsPaidBeforeResolution() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent mistcaller = harness.addToBattlefieldAndReturn(player1, new Mistcaller());
        mistcaller.tap();

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Mistcaller");
        harness.assertInGraveyard(player1, "Mistcaller");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The replacement also exiles the controller's own reanimated creatures")
    void ownCreatureIsExiled() {
        sacrificeMistcaller(player1);
        Card target = gd.playerGraveyards.get(player1.getId()).getFirst();

        reanimate(player1, player1);

        harness.assertNotOnBattlefield(player1, "Mistcaller");
        harness.assertNotInGraveyard(player1, "Mistcaller");
        assertThat(gd.exiledCards).anySatisfy(entry ->
                assertThat(entry.card().getId()).isEqualTo(target.getId()));
    }

    @Test
    @DisplayName("The replacement applies to every uncast creature throughout the turn")
    void replacementIsNotConsumedByFirstCreature() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(first, second));
        sacrificeMistcaller(player1);

        reanimate(player2, player2);
        reanimate(player2, player2);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(first, second);
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId())
                .contains(first.getId(), second.getId());
    }

    @Test
    @DisplayName("A creature exiled instead of entering does not trigger its enters ability")
    void exiledCreatureDoesNotTriggerEnterAbility() {
        Card priest = new DwarvenPriest();
        harness.setGraveyard(player2, List.of(priest));
        harness.addToBattlefield(player2, new GrizzlyBears());
        sacrificeMistcaller(player1);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        reanimate(player2, player2);

        harness.assertNotOnBattlefield(player2, "Dwarven Priest");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId()).contains(priest.getId());
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, lifeBefore);
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(currentActivePlayer == player1 ? player2 : player1, TurnStep.PRECOMBAT_MAIN);
    }
}
