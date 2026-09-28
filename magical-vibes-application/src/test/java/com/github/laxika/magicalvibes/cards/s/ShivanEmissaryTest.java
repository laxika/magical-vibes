package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RogueKavu;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShivanEmissary.class, RogueKavu.class, ShivanZombie.class, ShivanOasis.class})
class ShivanEmissaryTest extends BaseCardTest {

    @Test
    void withoutKickerDoesNotDestroy() {
        harness.addToBattlefield(player2, new RogueKavu());
        harness.setHand(player1, List.of(new ShivanEmissary()));
        addBaseMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shivan Emissary");
        harness.assertOnBattlefield(player2, "Rogue Kavu");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void kickedDestroysTargetNonblackCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RogueKavu());
        harness.setHand(player1, List.of(new ShivanEmissary()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        chooseTarget(target);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shivan Emissary");
        harness.assertNotOnBattlefield(player2, "Rogue Kavu");
        harness.assertInGraveyard(player2, "Rogue Kavu");
    }

    @Test
    void kickedDestroyCannotBeRegenerated() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RogueKavu());
        harness.setHand(player1, List.of(new ShivanEmissary()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        target.setRegenerationShield(1);
        chooseTarget(target);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Rogue Kavu");
        harness.assertInGraveyard(player2, "Rogue Kavu");
    }

    @Test
    void cannotKickTargetBlackCreature() {
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player2, new ShivanZombie());
        Permanent nonblackCreature = harness.addToBattlefieldAndReturn(player2, new RogueKavu());
        harness.setHand(player1, List.of(new ShivanEmissary()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds())
                .contains(nonblackCreature.getId())
                .doesNotContain(blackCreature.getId());

        harness.handlePermanentChosen(player1, nonblackCreature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Shivan Zombie");
        harness.assertInGraveyard(player2, "Rogue Kavu");
    }

    @Test
    void cannotKickTargetNoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new ShivanOasis());
        Permanent nonblackCreature = harness.addToBattlefieldAndReturn(player2, new RogueKavu());
        harness.setHand(player1, List.of(new ShivanEmissary()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds())
                .contains(nonblackCreature.getId())
                .doesNotContain(land.getId());

        harness.handlePermanentChosen(player1, nonblackCreature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Shivan Oasis");
        harness.assertInGraveyard(player2, "Rogue Kavu");
    }

    private void chooseTarget(Permanent target) {
        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void addKickedMana() {
        addBaseMana();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
