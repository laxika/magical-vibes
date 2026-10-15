package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CollectiveRestraint;
import com.github.laxika.magicalvibes.cards.n.NomadicElf;
import com.github.laxika.magicalvibes.cards.r.Repulse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TolarianEmissary.class, CollectiveRestraint.class, NomadicElf.class, Repulse.class})
class TolarianEmissaryTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, the ETB does not destroy an enchantment")
    void withoutKickerDoesNotDestroyEnchantment() {
        harness.addToBattlefield(player2, new CollectiveRestraint());
        harness.setHand(player1, List.of(new TolarianEmissary()));
        addBaseMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Collective Restraint");
        harness.assertOnBattlefield(player1, "Tolarian Emissary");
        org.assertj.core.api.Assertions.assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("When kicked, the ETB destroys the target enchantment")
    void kickedDestroysTargetEnchantment() {
        harness.addToBattlefield(player2, new CollectiveRestraint());
        harness.setHand(player1, List.of(new TolarianEmissary()));
        addKickedMana();
        UUID enchantmentId = harness.getPermanentId(player2, "Collective Restraint");

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, enchantmentId);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Collective Restraint");
        harness.assertInGraveyard(player2, "Collective Restraint");
    }

    @Test
    @DisplayName("When kicked, only an enchantment is a legal target")
    void kickedOnlyTargetsEnchantments() {
        harness.addToBattlefield(player2, new NomadicElf());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new CollectiveRestraint());
        harness.setHand(player1, List.of(new TolarianEmissary()));
        addKickedMana();
        UUID creatureId = harness.getPermanentId(player2, "Nomadic Elf");

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds()).containsExactly(enchantment.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creatureId))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, enchantment.getId());
        resolveAllTriggers();
        harness.assertOnBattlefield(player2, "Nomadic Elf");
    }

    @Test
    @DisplayName("When kicked, chooses the enchantment as the ETB ability is put on the stack")
    void kickedChoosesTargetAtEtbTime() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new CollectiveRestraint());
        harness.setHand(player1, List.of(new TolarianEmissary()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds()).containsExactly(enchantment.getId());

        harness.handlePermanentChosen(player1, enchantment.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Collective Restraint");
    }

    @Test
    @DisplayName("When kicked without a legal enchantment target, no ETB ability remains on the stack")
    void kickedWithoutEnchantmentLeavesStackEmpty() {
        harness.setHand(player1, List.of(new TolarianEmissary()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Tolarian Emissary");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The kicked ETB must destroy your own enchantment when it is the only target")
    void kickedDestroysOwnEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new CollectiveRestraint());
        harness.setHand(player1, List.of(new TolarianEmissary()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, enchantment.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Collective Restraint");
        harness.assertOnBattlefield(player1, "Tolarian Emissary");
    }

    @Test
    @DisplayName("The kicked ETB resolves after the Emissary is returned to hand")
    void kickedTriggerResolvesWithoutSource() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new CollectiveRestraint());
        harness.setHand(player1, List.of(new TolarianEmissary()));
        harness.setHand(player2, List.of(new Repulse()));
        harness.setLibrary(player2, List.of(new NomadicElf()));
        addKickedMana();
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, enchantment.getId());
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Tolarian Emissary"));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Tolarian Emissary");
        harness.assertOnBattlefield(player2, "Collective Restraint");
        resolveAllTriggers();
        harness.assertInGraveyard(player2, "Collective Restraint");
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void addKickedMana() {
        addBaseMana();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
