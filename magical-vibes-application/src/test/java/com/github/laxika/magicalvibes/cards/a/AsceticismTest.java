package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GalvanicBlast;
import com.github.laxika.magicalvibes.cards.m.MoriokReaver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Asceticism.class, MoriokReaver.class, GalvanicBlast.class})
class AsceticismTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Asceticism puts it on the stack as an enchantment spell")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new Asceticism(), "{3}{G}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("Creatures you control gain can't-be-targeted effect")
    void ownCreaturesGainCantBeTargeted() {
        Permanent bears = addCreatureReady(player1, new MoriokReaver());
        harness.addToBattlefield(player1, new Asceticism());

        assertThat(gqs.cantBeTargetedBySpellsOrAbilities(gd, bears)).isTrue();
    }

    @Test
    @DisplayName("Opponent creatures do not gain can't-be-targeted effect")
    void opponentCreaturesDoNotGainEffect() {
        Permanent opponentBears = addCreatureReady(player2, new MoriokReaver());
        harness.addToBattlefield(player1, new Asceticism());

        assertThat(gqs.cantBeTargetedBySpellsOrAbilities(gd, opponentBears)).isFalse();
    }

    @Test
    @DisplayName("Can't-be-targeted effect is removed when Asceticism leaves the battlefield")
    void effectRemovedWhenSourceLeaves() {
        Permanent bears = addCreatureReady(player1, new MoriokReaver());
        harness.addToBattlefield(player1, new Asceticism());
        assertThat(gqs.cantBeTargetedBySpellsOrAbilities(gd, bears)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Asceticism"));

        assertThat(gqs.cantBeTargetedBySpellsOrAbilities(gd, bears)).isFalse();
    }

    @Test
    @DisplayName("Opponent cannot target your creature with spells while Asceticism is on battlefield")
    void opponentCannotTargetYourCreature() {
        Permanent bears = addCreatureReady(player1, new MoriokReaver());
        harness.addToBattlefield(player1, new Asceticism());

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new GalvanicBlast()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Controller can target own creatures despite Asceticism's effect")
    void controllerCanTargetOwnCreatures() {
        Permanent bears = addCreatureReady(player1, new MoriokReaver());
        harness.addToBattlefield(player1, new Asceticism());

        harness.setHand(player1, List.of(new GalvanicBlast()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Activating regeneration targets a creature and puts ability on stack")
    void activatingRegenTargetsCreature() {
        harness.addToBattlefield(player1, new Asceticism());
        Permanent bears = addCreatureReady(player1, new MoriokReaver());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, bears.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Resolving regeneration grants a regeneration shield to target creature")
    void resolvingRegenGrantsShield() {
        harness.addToBattlefield(player1, new Asceticism());
        Permanent bears = addCreatureReady(player1, new MoriokReaver());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can regenerate opponent's creature")
    void canRegenerateOpponentCreature() {
        harness.addToBattlefield(player1, new Asceticism());
        Permanent opponentBears = addCreatureReady(player2, new MoriokReaver());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, opponentBears.getId());
        harness.passBothPriorities();

        assertThat(opponentBears.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate regeneration without enough mana")
    void cannotActivateRegenWithoutMana() {
        harness.addToBattlefield(player1, new Asceticism());
        Permanent bears = addCreatureReady(player1, new MoriokReaver());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot target non-creature with regeneration ability")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new Asceticism());
        // Target Asceticism itself (an enchantment, not a creature)
        UUID asceticismId = harness.getPermanentId(player1, "Asceticism");
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, asceticismId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Mana is consumed when activating regeneration ability")
    void manaConsumedOnRegenActivation() {
        harness.addToBattlefield(player1, new Asceticism());
        Permanent bears = addCreatureReady(player1, new MoriokReaver());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, bears.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration fizzles if target creature is removed before resolution")
    void regenFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new Asceticism());
        Permanent bears = addCreatureReady(player1, new MoriokReaver());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, bears.getId());

        // Remove target before resolution
        gd.playerBattlefields.get(player1.getId()).remove(bears);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can activate regeneration multiple times on the same creature")
    void canStackMultipleRegenerationShields() {
        harness.addToBattlefield(player1, new Asceticism());
        Permanent bears = addCreatureReady(player1, new MoriokReaver());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getRegenerationShield()).isEqualTo(2);
    }

    @Test
    @DisplayName("Regeneration replaces lethal damage, taps the creature, and clears damage")
    void regenerationPreventsLethalDamage() {
        harness.addToBattlefield(player1, new Asceticism());
        Permanent creature = addCreatureReady(player1, new MoriokReaver());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        harness.setHand(player1, List.of(new GalvanicBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(creature.getRegenerationShield()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(MoriokReaver.class::isInstance);
    }

    @Test
    @DisplayName("An opponent's spell loses its target when Asceticism enters before resolution")
    void hexproofGainedBeforeResolutionInvalidatesTarget() {
        Permanent creature = addCreatureReady(player1, new MoriokReaver());
        harness.setHand(player2, List.of(new GalvanicBlast()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, creature.getId());
        harness.addToBattlefield(player1, new Asceticism());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Opponent's Asceticism prevents targeting their creature with regeneration")
    void cannotRegenerateOpponentsHexproofCreature() {
        harness.addToBattlefield(player1, new Asceticism());
        harness.addToBattlefield(player2, new Asceticism());
        Permanent creature = addCreatureReady(player2, new MoriokReaver());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Regeneration ability resolves after Asceticism leaves the battlefield")
    void regenerationResolvesWithoutSource() {
        harness.addToBattlefield(player1, new Asceticism());
        Permanent source = findPermanent(player1, "Asceticism");
        Permanent creature = addCreatureReady(player1, new MoriokReaver());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(source);

        harness.passBothPriorities();

        assertThat(creature.getRegenerationShield()).isEqualTo(1);
    }
}
