package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.j.JustTheWind;
import com.github.laxika.magicalvibes.cards.m.MoorlandDrifter;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({HanweirMilitiaCaptain.class, MoorlandDrifter.class, JustTheWind.class})
class HanweirMilitiaCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms at upkeep with four creatures")
    void transformsAtUpkeepWithFourCreatures() {
        Permanent captain = addCaptainReady(player1);
        harness.addToBattlefield(player1, new MoorlandDrifter());
        harness.addToBattlefield(player1, new MoorlandDrifter());
        harness.addToBattlefield(player1, new MoorlandDrifter());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(captain.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Does not transform at upkeep with fewer than four creatures")
    void doesNotTransformWithFewerThanFourCreatures() {
        Permanent captain = addCaptainReady(player1);
        harness.addToBattlefield(player1, new MoorlandDrifter());
        harness.addToBattlefield(player1, new MoorlandDrifter());

        advanceToUpkeep(player1);

        assertThat(captain.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cult Leader has power and toughness equal to controlled creatures")
    void cultLeaderScalesWithControlledCreatures() {
        Permanent leader = addTransformedCaptain(player1);
        harness.addToBattlefield(player1, new MoorlandDrifter());
        harness.addToBattlefield(player1, new MoorlandDrifter());
        harness.addToBattlefield(player2, new MoorlandDrifter());

        assertThat(gqs.getEffectivePower(gd, leader)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, leader)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cult Leader creates a multicolored Human Cleric token at its controller's end step")
    void cultLeaderCreatesHumanClericTokenAtEndStep() {
        Permanent leader = addTransformedCaptain(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();

        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.CLERIC);
        assertThat(gqs.getEffectivePower(gd, leader)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, leader)).isEqualTo(2);
    }

    @Test
    void opponentsCreaturesDoNotMeetTransformCondition() {
        Permanent captain = addCaptainReady(player1);
        harness.addToBattlefield(player1, new MoorlandDrifter());
        harness.addToBattlefield(player1, new MoorlandDrifter());
        harness.addToBattlefield(player2, new MoorlandDrifter());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(captain.isTransformed()).isFalse();
    }

    @Test
    void doesNotTransformDuringOpponentsUpkeep() {
        Permanent captain = addCaptainReady(player1);
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new MoorlandDrifter());
        }

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(captain.isTransformed()).isFalse();
    }

    @Test
    void doesNotTransformIfCreatureCountDropsBeforeResolution() {
        Permanent captain = addCaptainReady(player1);
        Permanent drifter = harness.addToBattlefieldAndReturn(player1, new MoorlandDrifter());
        harness.addToBattlefield(player1, new MoorlandDrifter());
        harness.addToBattlefield(player1, new MoorlandDrifter());
        harness.setHand(player1, List.of(new JustTheWind()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        bounceCreature(drifter);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        resolveAllTriggers();

        assertThat(captain.isTransformed()).isFalse();
    }

    @Test
    void cultLeaderShrinksWithoutTransformingBack() {
        Permanent leader = addTransformedCaptain(player1);
        Permanent drifter = harness.addToBattlefieldAndReturn(player1, new MoorlandDrifter());
        harness.setHand(player1, List.of(new JustTheWind()));
        assertThat(gqs.getEffectivePower(gd, leader)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, leader)).isEqualTo(2);

        bounceCreature(drifter);
        advanceToUpkeep(player1);

        assertThat(leader.isTransformed()).isTrue();
        assertThat(gqs.getEffectivePower(gd, leader)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, leader)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cultLeaderDoesNotCreateTokenDuringOpponentsEndStep() {
        Permanent leader = addTransformedCaptain(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(leader);
    }

    @Test
    void frontFaceDoesNotCreateTokenAtEndStep() {
        Permanent captain = addCaptainReady(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(captain);
    }

    @Test
    void cultLeaderCreatesExactlyOneUntappedOneOneToken() {
        addTransformedCaptain(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(token.isTapped()).isFalse();
    }

    private void bounceCreature(Permanent creature) {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
    }

    private Permanent addCaptainReady(Player player) {
        return addCreatureReady(player, new HanweirMilitiaCaptain());
    }

    private Permanent addTransformedCaptain(Player player) {
        HanweirMilitiaCaptain card = new HanweirMilitiaCaptain();
        Permanent permanent = addCreatureReady(player, card);
        permanent.setCard(card.getBackFaceCard());
        permanent.setTransformed(true);
        return permanent;
    }

}
