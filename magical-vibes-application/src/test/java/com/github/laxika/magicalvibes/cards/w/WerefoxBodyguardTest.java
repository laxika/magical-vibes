package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({WerefoxBodyguard.class, GrizzlyBears.class})
class WerefoxBodyguardTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles up to one non-Fox creature until Werefox Bodyguard leaves")
    void etbExilesNonFoxCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndResolve(bears.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("The exiled creature returns when Werefox Bodyguard leaves")
    void exiledCreatureReturnsWhenBodyguardLeaves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castAndResolve(bears.getId());
        Permanent bodyguard = findPermanent(player1, "Werefox Bodyguard");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bodyguard));

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("The ETB ability cannot target a Fox creature")
    void cannotTargetFoxCreature() {
        Permanent fox = harness.addToBattlefieldAndReturn(player2, new WerefoxBodyguard());

        harness.setHand(player1, List.of(new WerefoxBodyguard()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, fox.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrificing Werefox Bodyguard gains 2 life")
    void sacrificeAbilityGainsTwoLife() {
        harness.addToBattlefield(player1, new WerefoxBodyguard());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertInGraveyard(player1, "Werefox Bodyguard");
    }

    private void castAndResolve(UUID targetId) {
        harness.setHand(player1, List.of(new WerefoxBodyguard()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
