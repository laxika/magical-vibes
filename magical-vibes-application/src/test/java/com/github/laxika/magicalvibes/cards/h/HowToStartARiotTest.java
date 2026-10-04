package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HowToStartARiot.class, GiantSpider.class, GrizzlyBears.class, Mountain.class})
class HowToStartARiotTest extends BaseCardTest {

    @Test
    @DisplayName("Gives the creature menace and boosts the targeted player's creatures")
    void givesMenaceAndBoostsTargetPlayersCreatures() {
        Permanent targetCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent firstOpponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondOpponentCreature = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        cast(targetCreature, player2.getId());

        assertThat(targetCreature.hasKeyword(Keyword.MENACE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, targetCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, firstOpponentCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, secondOpponentCreature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Both effects expire at end of turn")
    void effectsExpireAtEndOfTurn() {
        Permanent targetCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(targetCreature, player2.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(targetCreature.hasKeyword(Keyword.MENACE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, targetCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Rejects a non-player in the player target group")
    void rejectsNonPlayerTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new HowToStartARiot()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a player in the creature target group")
    void rejectsNonCreatureTarget() {
        harness.setHand(player1, List.of(new HowToStartARiot()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(player2.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetOwnPlayerAndOpponentsCreatureIndependently() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        cast(opponentCreature, player1.getId());

        assertThat(opponentCreature.hasKeyword(Keyword.MENACE)).isTrue();
        assertThat(ownCreature.hasKeyword(Keyword.MENACE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(4);
    }

    @Test
    void targetedCreatureAlsoReceivesBoostWhenItsControllerIsTargeted() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(creature, player1.getId());

        assertThat(creature.hasKeyword(Keyword.MENACE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void boostsCreaturesPresentAtResolutionButNotCreaturesEnteringLater() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HowToStartARiot()));
        addMana();
        harness.castInstant(player1, 0, List.of(creature.getId(), player2.getId()));
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, beforeResolution)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, afterResolution)).isEqualTo(2);
    }

    @Test
    void stillBoostsCreaturesWhenCreatureTargetLeavesBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new HowToStartARiot()));
        addMana();
        harness.castInstant(player1, 0, List.of(creature.getId(), player2.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(4);
        assertThat(creature.hasKeyword(Keyword.MENACE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void cast(Permanent creature, UUID playerTarget) {
        harness.setHand(player1, List.of(new HowToStartARiot()));
        addMana();
        harness.castInstant(player1, 0, List.of(creature.getId(), playerTarget));
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
