package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.j.JhoirasTimebug;
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

@CardUsed({DetainmentSpell.class, JhoirasTimebug.class, PrismaticLens.class})
class DetainmentSpellTest extends BaseCardTest {

    @Test
    void resolvingAttachesAndLocksTheEnchantedCreature() {
        Permanent timebug = addCreatureReady(player2, new JhoirasTimebug());
        harness.setHand(player1, List.of(new DetainmentSpell()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, timebug.getId());
        harness.passBothPriorities();

        assertThat(findAura(player1).getAttachedTo()).isEqualTo(timebug.getId());
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

    private Permanent attachAura(Player controller, Permanent creature) {
        Permanent aura = new Permanent(new DetainmentSpell());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }

    private Permanent findAura(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof DetainmentSpell)
                .findFirst()
                .orElseThrow();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
