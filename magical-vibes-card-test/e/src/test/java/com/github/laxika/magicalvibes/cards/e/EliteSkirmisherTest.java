package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EliteSkirmisher.class, GiantGrowth.class, GrizzlyBears.class, Shock.class})
class EliteSkirmisherTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell that targets Elite Skirmisher lets you tap a creature")
    void heroicTapsTargetCreature() {
        Permanent skirmisher = harness.addToBattlefieldAndReturn(player1, new EliteSkirmisher());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, skirmisher.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining the heroic ability leaves the target untapped")
    void decliningHeroicLeavesTargetUntapped() {
        Permanent skirmisher = harness.addToBattlefieldAndReturn(player1, new EliteSkirmisher());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, skirmisher.getId());
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

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("An opponent's spell that targets Elite Skirmisher does not trigger heroic")
    void opponentsSpellDoesNotTriggerHeroic() {
        Permanent skirmisher = harness.addToBattlefieldAndReturn(player1, new EliteSkirmisher());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, skirmisher.getId());
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("A spell targeting another creature does not trigger heroic")
    void targetingAnotherCreatureDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new EliteSkirmisher());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, bears.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A creature spell with no targets does not trigger heroic")
    void untargetedSpellDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new EliteSkirmisher());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Heroic can tap Elite Skirmisher itself before the spell resolves")
    void heroicCanTapItself() {
        Permanent skirmisher = harness.addToBattlefieldAndReturn(player1, new EliteSkirmisher());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, skirmisher.getId());
        harness.handlePermanentChosen(player1, skirmisher.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(skirmisher.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Elite Skirmisher");
    }
}
