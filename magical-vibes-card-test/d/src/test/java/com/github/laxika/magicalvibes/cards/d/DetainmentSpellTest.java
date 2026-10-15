package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.j.JhoirasTimebug;
import com.github.laxika.magicalvibes.cards.g.GemhideSliver;
import com.github.laxika.magicalvibes.cards.p.PrismaticLens;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DetainmentSpell.class, JhoirasTimebug.class, PrismaticLens.class, GemhideSliver.class})
class DetainmentSpellTest extends BaseCardTest {

    @Test
    void resolvingAttachesAndLocksTheEnchantedCreature() {
        Permanent timebug = addCreatureReady(player2, new JhoirasTimebug());
        harness.setHand(player1, List.of(new DetainmentSpell()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, timebug.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Detainment Spell").getAttachedTo()).isEqualTo(timebug.getId());
        assertThatThrownBy(() -> harness.activateAbility(player2, indexOf(player2, timebug), null, timebug.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    void activatedAbilityMovesAuraToTargetCreature() {
        Permanent first = addCreatureReady(player2, new JhoirasTimebug());
        Permanent second = addCreatureReady(player2, new JhoirasTimebug());
        Permanent aura = attachAura(player1, first);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, indexOf(player1, aura), null, second.getId());
        harness.passBothPriorities();

        assertThat(aura.getAttachedTo()).isEqualTo(second.getId());
        assertThat(aura.isTapped()).isFalse();
    }

    @Test
    void movingAuraMovesActivatedAbilityLockToNewCreature() {
        Permanent first = addCreatureReady(player2, new JhoirasTimebug());
        Permanent second = addCreatureReady(player2, new JhoirasTimebug());
        Permanent aura = attachAura(player1, first);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.activateAbility(player2, indexOf(player2, first), null, first.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, indexOf(player1, aura), null, second.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player2, indexOf(player2, first), null, first.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player2, indexOf(player2, second), null, second.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PrismaticLens());
        harness.setHand(player1, List.of(new DetainmentSpell()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void activatedAbilityCannotTargetNonCreaturePermanent() {
        Permanent creature = addCreatureReady(player2, new JhoirasTimebug());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PrismaticLens());
        Permanent aura = attachAura(player1, creature);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, aura), null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void enchantedCreatureCannotActivateGrantedManaAbility() {
        Permanent sliver = addCreatureReady(player2, new GemhideSliver());
        attachAura(player1, sliver);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player2, indexOf(player2, sliver), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
        assertThat(sliver.isTapped()).isFalse();
    }

    @Test
    void attachingToCurrentCreatureDoesNotChangeAuraTimestamp() {
        Permanent creature = addCreatureReady(player2, new JhoirasTimebug());
        Permanent aura = attachAura(player1, creature);
        long timestamp = aura.getTimestamp();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, indexOf(player1, aura), null, creature.getId());
        harness.passBothPriorities();

        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(aura.getTimestamp()).isEqualTo(timestamp);
    }

    @Test
    void moveAbilityLeavesAuraOnOriginalCreatureWhenTargetLeaves() {
        Permanent first = addCreatureReady(player2, new JhoirasTimebug());
        Permanent second = addCreatureReady(player2, new JhoirasTimebug());
        Permanent aura = attachAura(player1, first);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, indexOf(player1, aura), null, second.getId());
        gd.playerBattlefields.get(player2.getId()).remove(second);
        gd.playerGraveyards.get(player2.getId()).add(second.getCard());
        harness.passBothPriorities();

        assertThat(aura.getAttachedTo()).isEqualTo(first.getId());
        harness.assertOnBattlefield(player1, "Detainment Spell");
    }

    @Test
    void moveAbilityDoesNotReturnAuraThatLeftBattlefield() {
        Permanent first = addCreatureReady(player2, new JhoirasTimebug());
        Permanent second = addCreatureReady(player2, new JhoirasTimebug());
        Permanent aura = attachAura(player1, first);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, indexOf(player1, aura), null, second.getId());
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Detainment Spell");
        harness.assertInGraveyard(player1, "Detainment Spell");
    }

    private Permanent attachAura(Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new DetainmentSpell());
        aura.setAttachedTo(creature.getId());
        return aura;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
