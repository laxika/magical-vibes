package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IntoTheVoid;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BattlefieldThaumaturgeTest extends BaseCardTest {

    @Test
    @DisplayName("Reduces an instant or sorcery by one for each creature it targets")
    void reducesCostForEachCreatureTarget() {
        harness.addToBattlefield(player1, new BattlefieldThaumaturge());
        Permanent firstBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IntoTheVoid()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, List.of(firstBear.getId(), secondBear.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Grizzly Bears"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Does not reduce a spell that targets only a player")
    void doesNotReducePlayerTargetingSpell() {
        harness.addToBattlefield(player1, new BattlefieldThaumaturge());
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Heroic grants hexproof when you cast a spell targeting Battlefield Thaumaturge")
    void heroicGrantsHexproofWhenTargeted() {
        Permanent thaumaturge = addCreatureReady(player1, new BattlefieldThaumaturge());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, thaumaturge.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, thaumaturge, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Heroic hexproof wears off at end of turn")
    void heroicHexproofWearsOffAtEndOfTurn() {
        Permanent thaumaturge = addCreatureReady(player1, new BattlefieldThaumaturge());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, thaumaturge.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, thaumaturge, Keyword.HEXPROOF)).isTrue();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, thaumaturge, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Heroic does not trigger for a spell targeting a player")
    void heroicDoesNotTriggerForPlayerTarget() {
        Permanent thaumaturge = addCreatureReady(player1, new BattlefieldThaumaturge());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, thaumaturge, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Heroic does not trigger for an opponent's spell")
    void heroicDoesNotTriggerForOpponentsSpell() {
        Permanent thaumaturge = addCreatureReady(player1, new BattlefieldThaumaturge());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        UUID thaumaturgeId = thaumaturge.getId();
        harness.castInstant(player2, 0, thaumaturgeId);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, thaumaturge, Keyword.HEXPROOF)).isFalse();
    }
}
