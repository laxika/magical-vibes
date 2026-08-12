package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RazorGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for Plains reduces the generic mana cost")
    void affinityForPlainsReducesGenericCost() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new Plains());
        }
        harness.setHand(player1, List.of(new RazorGolem()));

        harness.castCreature(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).hasSize(1);
    }

    @Test
    @DisplayName("Affinity counts only Plains controlled by the spell's controller")
    void affinityCountsOnlyControlledPlains() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player2, new Plains());
        }
        harness.setHand(player1, List.of(new RazorGolem()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Vigilance prevents Razor Golem from tapping when it attacks")
    void vigilancePreventsTappingWhenAttacking() {
        Permanent golem = new Permanent(new RazorGolem());
        golem.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(golem);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(golem.isTapped()).isFalse();
    }
}
