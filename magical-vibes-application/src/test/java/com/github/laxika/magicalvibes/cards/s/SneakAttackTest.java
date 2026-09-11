package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.Guma;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SneakAttack.class, Guma.class, Mountain.class})
class SneakAttackTest extends BaseCardTest {

    @Test
    @DisplayName("Offers only creature cards in hand")
    void offersOnlyCreatures() {
        harness.addToBattlefield(player1, new SneakAttack());
        harness.setHand(player1, List.of(new Mountain(), new Guma()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(1);
    }

    @Test
    @DisplayName("Chosen creature enters with haste and is sacrificed at the next end step")
    void chosenCreatureEntersWithHasteAndIsSacrificedAtEndStep() {
        harness.addToBattlefield(player1, new SneakAttack());
        harness.setHand(player1, List.of(new Guma()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        Permanent creature = findPermanent(player1, "Guma");
        assertThat(creature.hasKeyword(Keyword.HASTE)).isTrue();
        harness.assertOnBattlefield(player1, "Guma");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Guma");
        harness.assertInGraveyard(player1, "Guma");
    }

    @Test
    @DisplayName("Sacrifices only the creature chosen by the ability")
    void sacrificesOnlyTheChosenCreature() {
        harness.addToBattlefield(player1, new SneakAttack());
        harness.setHand(player1, List.of(new Guma(), new Guma()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.addToBattlefield(player1, new Guma());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Guma")).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining leaves the creature in hand")
    void decliningLeavesCreatureInHand() {
        harness.addToBattlefield(player1, new SneakAttack());
        harness.setHand(player1, List.of(new Guma()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Guma");
        harness.assertNotOnBattlefield(player1, "Guma");
    }
}
