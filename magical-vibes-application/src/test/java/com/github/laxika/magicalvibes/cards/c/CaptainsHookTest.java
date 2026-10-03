package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaptainsHook.class, GrizzlyBears.class, Naturalize.class})
class CaptainsHookTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+0, menace, and Pirate")
    void equippedCreatureGetsGrants() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent hook = harness.addToBattlefieldAndReturn(player1, new CaptainsHook());
        hook.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
        assertThat(gqs.computeStaticBonus(gd, creature).grantedSubtypes()).contains(CardSubtype.PIRATE);
    }

    @Test
    @DisplayName("Re-equipping destroys the previously equipped permanent")
    void reEquipDestroysPreviousPermanent() {
        Permanent hook = harness.addToBattlefieldAndReturn(player1, new CaptainsHook());
        Permanent firstCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCreature = addCreatureReady(player1, new GrizzlyBears());
        hook.setAttachedTo(firstCreature.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, secondCreature.getId());
        resolveAllTriggers();

        assertThat(hook.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(firstCreature.getId()));
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Equipping from unattached state does not destroy the equipped creature")
    void equippingFromUnattachedDoesNotDestroyCreature() {
        Permanent hook = harness.addToBattlefieldAndReturn(player1, new CaptainsHook());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(hook.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Equipping adds Pirate without replacing the creature's original type")
    void equippingPreservesExistingCreatureTypes() {
        harness.addToBattlefield(player1, new CaptainsHook());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.BEAR)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.PIRATE)).isTrue();
    }

    @Test
    @DisplayName("Equipping the same creature again does not unattach or destroy it")
    void equippingSameCreatureDoesNotDestroyIt() {
        Permanent hook = harness.addToBattlefieldAndReturn(player1, new CaptainsHook());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        hook.setAttachedTo(creature.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, creature.getId());
        resolveAllTriggers();

        assertThat(hook.getAttachedTo()).isEqualTo(creature.getId());
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Re-equipping leaves the old creature alive until the destroy trigger resolves")
    void destructionUsesASeparateTrigger() {
        Permanent hook = harness.addToBattlefieldAndReturn(player1, new CaptainsHook());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        hook.setAttachedTo(first.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(hook.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first, second);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, first, CardSubtype.PIRATE)).isFalse();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first).contains(second);
    }

    @Test
    @DisplayName("Destroying the attached Hook still triggers destruction of its former creature")
    void destroyingHookDestroysEquippedCreature() {
        Permanent hook = harness.addToBattlefieldAndReturn(player1, new CaptainsHook());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        hook.setAttachedTo(creature.getId());
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, hook.getId());

        harness.assertInGraveyard(player1, "Captain's Hook");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }
}
