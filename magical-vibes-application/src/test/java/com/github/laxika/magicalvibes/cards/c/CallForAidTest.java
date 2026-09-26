package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VolrathsStronghold;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CallForAid.class, GrizzlyBears.class, VolrathsStronghold.class})
class CallForAidTest extends BaseCardTest {

    @Test
    @DisplayName("Steals, untaps, grants haste and protects the opponent's creatures")
    void stealsUntapsHastesAndProtectsCreatures() {
        Permanent firstCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondCreature = addCreatureReady(player2, new GrizzlyBears());
        firstCreature.tap();
        secondCreature.tap();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new VolrathsStronghold());

        castCallForAid(player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(firstCreature, secondCreature)
                .doesNotContain(land);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
        assertThat(firstCreature.isTapped()).isFalse();
        assertThat(secondCreature.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.cantBeSacrificed(gd, firstCreature)).isTrue();
        assertThat(gqs.cantBeSacrificed(gd, secondCreature)).isTrue();
    }

    @Test
    @DisplayName("Temporary control and creature riders expire at end of turn")
    void temporaryEffectsExpireAtEndOfTurn() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        castCallForAid(player2.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
        assertThat(gqs.cantBeSacrificed(gd, creature)).isFalse();
    }

    @Test
    @DisplayName("The caster cannot attack the targeted player that turn")
    void casterCannotAttackTargetedPlayer() {
        addCreatureReady(player1, new GrizzlyBears());

        castCallForAid(player2.getId());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new CallForAid()));
        addMana(player1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    private void castCallForAid(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new CallForAid()));
        addMana(player1);
        harness.castSorcery(player1, 0, targetPlayerId);
        harness.passBothPriorities();
    }

    private void addMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 4);
    }
}
