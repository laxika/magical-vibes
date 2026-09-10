package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.k.Kindle;
import com.github.laxika.magicalvibes.cards.l.LowlandGiant;
import com.github.laxika.magicalvibes.cards.l.LotusPetal;
import com.github.laxika.magicalvibes.cards.m.MasterDecoy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpinalGraft.class, Disenchant.class, Kindle.class, LotusPetal.class,
        LowlandGiant.class, MasterDecoy.class})
class SpinalGraftTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Spinal Graft attaches it and gives the creature +3/+3")
    void resolvingAttachesAndBoosts() {
        Permanent giant = addCreatureReady(player1, new LowlandGiant());

        harness.setHand(player1, List.of(new SpinalGraft()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, giant.getId());
        harness.passBothPriorities();

        Permanent graft = findPermanent(player1, "Spinal Graft");
        assertThat(graft.isAttached()).isTrue();
        assertThat(graft.getAttachedTo()).isEqualTo(giant.getId());
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(6);
    }

    @Test
    @DisplayName("Boost wears off when Spinal Graft leaves the battlefield")
    void boostEndsWhenAuraLeaves() {
        Permanent giant = addCreatureReady(player1, new LowlandGiant());
        Permanent graft = attachGraft(player1, giant);

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(7);

        gd.playerBattlefields.get(player1.getId()).remove(graft);

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Enchanted creature is destroyed when it becomes the target of a spell")
    void destroyedWhenEnchantedCreatureTargetedBySpell() {
        Permanent giant = addCreatureReady(player2, new LowlandGiant());
        attachGraft(player1, giant);

        castKindleAt(player1, giant);

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Lowland Giant");
        harness.assertNotOnBattlefield(player2, "Lowland Giant");
    }

    @Test
    @DisplayName("The trigger still destroys the creature if Spinal Graft leaves before resolution")
    void triggerUsesLastKnownAttachmentWhenAuraLeaves() {
        Permanent giant = addCreatureReady(player2, new LowlandGiant());
        Permanent graft = attachGraft(player1, giant);

        castKindleAt(player1, giant);

        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, graft.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spinal Graft");
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Lowland Giant");
        harness.assertNotOnBattlefield(player2, "Lowland Giant");
    }

    @Test
    @DisplayName("Enchanted creature is destroyed when it becomes the target of an ability")
    void destroyedWhenEnchantedCreatureTargetedByAbility() {
        Permanent decoy = addCreatureReady(player2, new MasterDecoy());
        Permanent giant = addCreatureReady(player2, new LowlandGiant());
        attachGraft(player1, giant);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.activateAbility(player2, 0, null, giant.getId());
        resolveAllTriggers();

        assertThat(decoy.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Lowland Giant");
        harness.assertNotOnBattlefield(player2, "Lowland Giant");
    }

    @Test
    @DisplayName("A creature destroyed this way can't be regenerated")
    void destroyedCreatureCannotRegenerate() {
        Permanent giant = addCreatureReady(player2, new LowlandGiant());
        giant.setRegenerationShield(1);
        attachGraft(player1, giant);

        castKindleAt(player1, giant);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Lowland Giant");
    }

    @Test
    @DisplayName("Targeting Spinal Graft itself does not trigger the destruction")
    void doesNotTriggerWhenAuraItselfIsTargeted() {
        Permanent giant = addCreatureReady(player2, new LowlandGiant());
        Permanent graft = attachGraft(player1, giant);

        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, graft.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spinal Graft");
        harness.assertOnBattlefield(player2, "Lowland Giant");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Spinal Graft")
    void cannotTargetNonCreature() {
        // A legal creature target must exist somewhere, or the aura is unplayable before targeting
        // is ever validated (CR 601.2c) and the cast fails with the wrong message.
        harness.addToBattlefield(player2, new LowlandGiant());
        harness.addToBattlefield(player1, new LotusPetal());
        harness.setHand(player1, List.of(new SpinalGraft()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent lotusPetal = findPermanent(player1, "Lotus Petal");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, lotusPetal.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castKindleAt(Player player, Permanent target) {
        harness.setHand(player, List.of(new Kindle()));
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.castInstant(player, 0, target.getId());
    }

    private Permanent attachGraft(Player controller, Permanent creature) {
        Permanent graft = harness.addToBattlefieldAndReturn(controller, new SpinalGraft());
        graft.setAttachedTo(creature.getId());
        return graft;
    }
}
