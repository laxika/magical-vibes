package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvenCloudchaser.class, GloriousAnthem.class, AngelicChorus.class, GrizzlyBears.class})
class AvenCloudchaserTest extends BaseCardTest {

    @Test
    @DisplayName("Aven Cloudchaser has flying")
    void hasFlying() {
        Permanent cloudchaser = harness.addToBattlefieldAndReturn(player1, new AvenCloudchaser());

        assertThat(gqs.hasKeyword(gd, cloudchaser, Keyword.FLYING)).isTrue();
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Aven Cloudchaser does not choose its ETB target")
    void castingDoesNotChooseEtbTarget() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.castFromHand(player1, new AvenCloudchaser(), "{3}{W}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Aven Cloudchaser");
        assertThat(entry.getTargetId()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Resolving Aven Cloudchaser enters battlefield and triggers ETB destroy")
    void resolvingEntersBattlefieldAndTriggersEtb() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        UUID targetId = harness.getPermanentId(player2, "Glorious Anthem");
        harness.castFromHand(player1, new AvenCloudchaser(), "{3}{W}");

        // Resolve creature spell → enters battlefield, ETB triggers
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Aven Cloudchaser");
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, targetId);

        // ETB triggered ability should be on stack
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Aven Cloudchaser");
        assertThat(trigger.getTargetId()).isEqualTo(targetId);

        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB resolves and destroys target enchantment")
    void etbDestroysTargetEnchantment() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        UUID targetId = harness.getPermanentId(player2, "Glorious Anthem");
        harness.castFromHand(player1, new AvenCloudchaser(), "{3}{W}");

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Cannot target a creature with Aven Cloudchaser's ETB")
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.castFromHand(player1, new AvenCloudchaser(), "{3}{W}");

        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        harness.handlePermanentChosen(player1, enchantment.getId());
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Chooses an enchantment that enters before the ETB is put on the stack")
    void choosesTargetWhenEtbIsPutOnStack() {
        harness.castFromHand(player1, new AvenCloudchaser(), "{3}{W}");
        harness.addToBattlefield(player2, new GloriousAnthem());
        UUID targetId = harness.getPermanentId(player2, "Glorious Anthem");

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Can destroy own enchantment with ETB")
    void canDestroyOwnEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        harness.castFromHand(player1, new AvenCloudchaser(), "{3}{W}");

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Glorious Anthem");
        harness.assertInGraveyard(player1, "Glorious Anthem");
    }

    @Test
    @DisplayName("ETB fizzles if target enchantment is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        UUID targetId = target.getId();
        harness.castFromHand(player1, new AvenCloudchaser(), "{3}{W}");

        // Resolve creature spell → choose the ETB target
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);

        // Remove target before ETB resolves
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));

        // Resolve ETB → fizzles
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    // ===== No target scenarios =====

    @Test
    @DisplayName("Can choose an enchantment when the ETB trigger is put on the stack")
    void canChooseEnchantmentAtTriggerTime() {
        harness.addToBattlefield(player2, new AngelicChorus());
        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.castFromHand(player1, new AvenCloudchaser(), "{3}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Can cast without a target when no enchantments on battlefield")
    void canCastWithoutTargetWhenNoEnchantments() {
        harness.castFromHand(player1, new AvenCloudchaser(), "{3}{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Aven Cloudchaser");
    }

    @Test
    @DisplayName("ETB does not trigger when cast without a target")
    void etbDoesNotTriggerWithoutTarget() {
        harness.castFromHand(player1, new AvenCloudchaser(), "{3}{W}");

        // Resolve creature spell
        harness.passBothPriorities();

        // Creature should be on battlefield
        harness.assertOnBattlefield(player1, "Aven Cloudchaser");
        // No triggered ability on stack
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new AvenCloudchaser()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

}
