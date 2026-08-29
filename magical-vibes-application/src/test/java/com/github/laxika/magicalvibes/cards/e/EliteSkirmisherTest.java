package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EliteSkirmisherTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell that targets Elite Skirmisher lets you tap a creature")
    void heroicTapsTargetCreature() {
        harness.addToBattlefield(player1, new EliteSkirmisher());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID skirmisherId = harness.getPermanentId(player1, "Elite Skirmisher");
        harness.castInstant(player1, 0, skirmisherId);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining the heroic ability leaves the target untapped")
    void decliningHeroicLeavesTargetUntapped() {
        harness.addToBattlefield(player1, new EliteSkirmisher());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID skirmisherId = harness.getPermanentId(player1, "Elite Skirmisher");
        harness.castInstant(player1, 0, skirmisherId);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A spell that targets a player does not trigger heroic")
    void targetingPlayerDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new EliteSkirmisher());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("An opponent's spell that targets Elite Skirmisher does not trigger heroic")
    void opponentsSpellDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new EliteSkirmisher());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        UUID skirmisherId = harness.getPermanentId(player1, "Elite Skirmisher");
        harness.castInstant(player2, 0, skirmisherId);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}
