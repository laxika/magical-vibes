package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FeastOfTheUnicorn.class, GrizzlyBears.class, HowlingMine.class})
class FeastOfTheUnicornTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +4/+0")
    void enchantedCreatureGetsBuff() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FeastOfTheUnicorn());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting Feast of the Unicorn attaches it to an opponent's creature")
    void castingAttachesToOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new FeastOfTheUnicorn()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Feast of the Unicorn");
        assertThat(aura.isAttached()).isTrue();
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creature returns to base stats when Feast of the Unicorn is removed")
    void effectsStopWhenRemoved() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FeastOfTheUnicorn());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Feast of the Unicorn")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new HowlingMine());
        harness.setHand(player1, List.of(new FeastOfTheUnicorn()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Multiple copies give cumulative power bonuses")
    void multipleCopiesStack() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FeastOfTheUnicorn(), new FeastOfTheUnicorn()));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Feast of the Unicorn")).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Aura goes to its owner's graveyard when the enchanted opposing creature dies")
    void auraGoesToGraveyardWhenEnchantedCreatureDies() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FeastOfTheUnicorn()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, creature));
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Feast of the Unicorn");
        harness.assertInGraveyard(player1, "Feast of the Unicorn");
        harness.assertNotInGraveyard(player2, "Feast of the Unicorn");
    }

    @Test
    @DisplayName("Feast of the Unicorn fizzles if its target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FeastOfTheUnicorn()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0, creature.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, creature));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertInGraveyard(player1, "Feast of the Unicorn");
    }
}
