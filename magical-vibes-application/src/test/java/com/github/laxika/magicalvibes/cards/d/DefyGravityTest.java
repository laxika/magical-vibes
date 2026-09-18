package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CabalTrainee;
import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
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

@CardUsed({DefyGravity.class, CabalTrainee.class, KrosanVerge.class})
class DefyGravityTest extends BaseCardTest {
    @Test
    @DisplayName("Target creature gains flying until end of turn")
    void grantsFlyingUntilEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CabalTrainee());
        harness.setHand(player1, List.of(new DefyGravity()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        assertThat(creature.hasKeyword(Keyword.FLYING)).isTrue();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(creature.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Flashback grants flying and exiles Defy Gravity")
    void flashbackGrantsFlyingAndExilesSpell() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CabalTrainee());
        harness.setGraveyard(player1, List.of(new DefyGravity()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveFlashback(player1, 0, creature.getId());
        assertThat(creature.hasKeyword(Keyword.FLYING)).isTrue();
        harness.assertNotInGraveyard(player1, "Defy Gravity");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Defy Gravity"));
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void canTargetOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CabalTrainee());
        harness.setHand(player1, List.of(new DefyGravity()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        assertThat(creature.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new KrosanVerge());
        harness.setHand(player1, List.of(new DefyGravity()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
