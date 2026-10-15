package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.ClericOfTheForwardOrder;
import com.github.laxika.magicalvibes.cards.g.GatherTheTownsfolk;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NecromanticSummons;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.cards.t.ThassaGodOfTheSea;
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

@CardUsed({HallowedMoonlight.class, GatherTheTownsfolk.class, GrizzlyBears.class, SoulWarden.class,
        ClericOfTheForwardOrder.class, NecromanticSummons.class, ThassaGodOfTheSea.class})
class HallowedMoonlightTest extends BaseCardTest {

    private void castHallowedMoonlight(Player caster) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(caster, List.of(new HallowedMoonlight()));
        harness.addMana(caster, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(caster, 0);
        assertThat(gd.stack).isEmpty();
    }

    private String nameOf(Permanent p) {
        return p.getCard().getName();
    }

    private void castGatherTheTownsfolk(Player caster) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(caster, List.of(new GatherTheTownsfolk()));
        harness.addMana(caster, ManaColor.WHITE, 2);
        harness.castAndResolveSorcery(caster, 0, 0);
    }

    private long humanTokenCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken() && "Human".equals(nameOf(p)))
                .count();
    }

    @Test
    @DisplayName("Creature tokens that would enter this turn never appear")
    void tokensDoNotEnter() {
        castHallowedMoonlight(player1);

        castGatherTheTownsfolk(player2);

        assertThat(humanTokenCount(player2)).isZero();
        assertThat(humanTokenCount(player1)).isZero();
    }

    @Test
    @DisplayName("A creature that was cast still enters normally")
    void castCreatureStillEnters() {
        castHallowedMoonlight(player1);

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
    @DisplayName("It is symmetrical — the caster's own tokens are exiled too")
    void ownTokensAlsoReplaced() {
        castHallowedMoonlight(player1);

        castGatherTheTownsfolk(player1);

        assertThat(humanTokenCount(player1)).isZero();
    }

    @Test
    @DisplayName("Prevented tokens do not trigger abilities on an occupied battlefield")
    void preventedTokensDoNotTriggerEtbAbilities() {
        harness.addToBattlefield(player1, new SoulWarden());
        harness.setLife(player1, 20);
        castHallowedMoonlight(player1);

        castGatherTheTownsfolk(player1);

        assertThat(humanTokenCount(player1)).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Resolving Hallowed Moonlight draws a card")
    void drawsACard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new HallowedMoonlight()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The replacement wears off at end of turn")
    void wearsOffNextTurn() {
        castHallowedMoonlight(player1);
        advanceToNextTurn(player1);

        castGatherTheTownsfolk(player2);

        assertThat(humanTokenCount(player2)).isEqualTo(2);
    }

    @Test
    @DisplayName("Without Hallowed Moonlight, tokens enter as normal")
    void baselineTokensEnter() {
        castGatherTheTownsfolk(player2);

        assertThat(humanTokenCount(player2)).isEqualTo(2);
    }

    @Test
    @DisplayName("Reanimated creatures are exiled without entering or triggering their own abilities")
    void reanimatedCreatureIsExiled() {
        castHallowedMoonlight(player1);
        ClericOfTheForwardOrder cleric = new ClericOfTheForwardOrder();
        cleric.setOwnerId(player2.getId());
        harness.setGraveyard(player2, List.of(cleric));
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new NecromanticSummons()));
        harness.addMana(player2, ManaColor.BLACK, 5);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player2, 0, cleric.getId());

        harness.assertNotOnBattlefield(player2, "Cleric of the Forward Order");
        harness.assertNotInGraveyard(player2, "Cleric of the Forward Order");
        assertThat(gd.exiledCards).anySatisfy(entry -> {
            assertThat(entry.card().getId()).isEqualTo(cleric.getId());
            assertThat(entry.ownerId()).isEqualTo(player2.getId());
        });
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A creature reanimated under another player's control is exiled for its owner")
    void reanimatedOpponentsCreatureIsExiledForOwner() {
        castHallowedMoonlight(player1);
        ClericOfTheForwardOrder cleric = new ClericOfTheForwardOrder();
        cleric.setOwnerId(player2.getId());
        harness.setGraveyard(player2, List.of(cleric));
        harness.setHand(player1, List.of(new NecromanticSummons()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, cleric.getId());

        harness.assertNotOnBattlefield(player1, "Cleric of the Forward Order");
        harness.assertNotOnBattlefield(player2, "Cleric of the Forward Order");
        harness.assertNotInGraveyard(player2, "Cleric of the Forward Order");
        assertThat(gd.exiledCards).anySatisfy(entry -> {
            assertThat(entry.card().getId()).isEqualTo(cleric.getId());
            assertThat(entry.ownerId()).isEqualTo(player2.getId());
        });
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A reanimated God enters as a noncreature when devotion is insufficient")
    void noncreatureGodIsNotExiled() {
        castHallowedMoonlight(player1);
        ThassaGodOfTheSea thassa = new ThassaGodOfTheSea();
        thassa.setOwnerId(player1.getId());
        harness.setGraveyard(player1, List.of(thassa));
        harness.setHand(player1, List.of(new NecromanticSummons()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, thassa.getId());

        harness.assertOnBattlefield(player1, "Thassa, God of the Sea");
        harness.assertNotInGraveyard(player1, "Thassa, God of the Sea");
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(thassa.getId()));
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
    }
}
