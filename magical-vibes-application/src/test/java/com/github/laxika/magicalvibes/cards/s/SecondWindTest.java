package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SecondWind.class, GrizzlyBears.class, Island.class})
class SecondWindTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Second Wind attaches it to a target creature")
    void resolvingAttachesToTargetCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SecondWind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Second Wind")
                        && permanent.isAttached()
                        && permanent.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("The tap ability taps both the enchanted creature and Second Wind")
    void tapAbilityTapsEnchantedCreatureAndAura() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = addAttachedAura(creature);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(aura.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The untap ability untaps the enchanted creature and taps Second Wind")
    void untapAbilityUntapsEnchantedCreatureAndTapsAura() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.tap();
        Permanent aura = addAttachedAura(creature);

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(aura.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Second Wind can target only a creature")
    void cannotEnchantIsland() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new SecondWind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Permanent island = findPermanent(player1, "Island");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addAttachedAura(Permanent creature) {
        Permanent aura = new Permanent(new SecondWind());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }
}
