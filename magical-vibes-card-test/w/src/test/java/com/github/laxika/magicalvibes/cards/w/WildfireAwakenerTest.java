package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WildfireAwakener.class})
class WildfireAwakenerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with X Elemental tokens")
    void createsXElementals() {
        harness.setHand(player1, List.of(new WildfireAwakener()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, 2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Elemental")).hasSize(2);
    }

    @Test
    @DisplayName("An Elemental deals 1 damage to a target player when it becomes tapped")
    void elementalDealsDamageWhenTapped() {
        harness.setHand(player1, List.of(new WildfireAwakener()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent elemental = findPermanents(player1, "Elemental").getFirst();
        elemental.tap();
        harness.inMutationScope(() -> {
            harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, elemental);
            harness.getTriggerCollectionService().processNextEntersTriggerTarget(gd);
        });

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(player1.getId(), player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("X zero creates no tokens")
    void zeroCreatesNoTokens() {
        harness.setHand(player1, List.of(new WildfireAwakener()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wildfire Awakener");
        assertThat(findPermanents(player1, "Elemental")).isEmpty();
    }

    @Test
    @DisplayName("Only the tapped token triggers and it may damage its controller")
    void onlyTappedTokenTriggersAndCanTargetController() {
        harness.setHand(player1, List.of(new WildfireAwakener()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, 2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent elemental = findPermanents(player1, "Elemental").getFirst();
        elemental.tap();
        harness.inMutationScope(() -> {
            harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, elemental);
            harness.getTriggerCollectionService().processNextEntersTriggerTarget(gd);
        });
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Convoke reduces mana payment without reducing X")
    void convokePreservesX() {
        harness.addToBattlefield(player1, new WildfireAwakener());
        Permanent helper = findPermanents(player1, "Wildfire Awakener").getFirst();
        harness.setHand(player1, List.of(new WildfireAwakener()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.ensurePriority(player1);
        gs.playCard(gd, player1, 0, 1, null, null, List.of(), List.of(helper.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(helper.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Elemental")).hasSize(1);
        assertThat(findPermanents(player1, "Wildfire Awakener")).hasSize(2);
    }
}
