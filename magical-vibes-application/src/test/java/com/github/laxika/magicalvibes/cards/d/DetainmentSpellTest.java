package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
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

@CardUsed({DetainmentSpell.class, BottleGnomes.class, FountainOfYouth.class})
class DetainmentSpellTest extends BaseCardTest {

    @Test
    void resolvingAttachesAndLocksTheEnchantedCreature() {
        Permanent gnomes = readyCreature(player2);
        harness.setHand(player1, List.of(new DetainmentSpell()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, gnomes.getId());
        harness.passBothPriorities();

        assertThat(findAura(player1).getAttachedTo()).isEqualTo(gnomes.getId());
        assertThatThrownBy(() -> harness.activateAbility(player2, indexOf(player2, gnomes), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    void activatedAbilityMovesAuraToTargetCreature() {
        Permanent first = readyCreature(player2);
        Permanent second = readyCreature(player2);
        Permanent aura = attachAura(player1, first);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, indexOf(player1, aura), null, second.getId());
        harness.passBothPriorities();

        assertThat(aura.getAttachedTo()).isEqualTo(second.getId());
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new DetainmentSpell()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent readyCreature(Player player) {
        Permanent gnomes = new Permanent(new BottleGnomes());
        gnomes.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(gnomes);
        return gnomes;
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
