package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EntrancingMelodyTest extends BaseCardTest {

    @Test
    @DisplayName("Card has correct effects configured")
    void hasCorrectEffects() {
        EntrancingMelody card = new EntrancingMelody();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(GainControlOfTargetPermanentEffect.class);
    }

    @Test
    @DisplayName("Gains control of target creature with mana value equal to X")
    void gainsControlWithMatchingManaValue() {
        // Grizzly Bears has mana value 2
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new EntrancingMelody()));
        // X=2, plus {U}{U} = 4 total mana needed
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 2, bearsId);
        harness.passBothPriorities();

        // Creature should now be controlled by player1
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(bearsId));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bearsId));

        // Control is permanent
        assertThat(gd.permanentControlStolenCreatures).contains(bearsId);
    }

    @Test
    @DisplayName("Cannot target creature with mana value different from X")
    void cannotTargetCreatureWithWrongManaValue() {
        // Grizzly Bears has mana value 2, but we'll cast with X=1
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new EntrancingMelody()));
        // X=1, plus {U}{U} = 3 total mana
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, bearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature with mana value equal to X");
    }

    @Test
    @DisplayName("Cannot target creature with mana value 2 when X=0")
    void cannotTargetWhenXIsZeroAndManaValueIsHigher() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new EntrancingMelody()));
        // X=0, plus {U}{U} = 2 total mana
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, bearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature with mana value equal to X");
    }

    @Test
    @DisplayName("Gains control of 1-MV creature with X=1")
    void gainsControlOfOneManaValueCreature() {
        // Elite Vanguard has mana value 1 ({W})
        harness.addToBattlefield(player2, new EliteVanguard());
        UUID vanguardId = harness.getPermanentId(player2, "Elite Vanguard");

        harness.setHand(player1, List.of(new EntrancingMelody()));
        // X=1, plus {U}{U} = 3 total mana
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 1, vanguardId);
        harness.passBothPriorities();

        // Should now be controlled by player1
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(vanguardId));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(vanguardId));
    }

    @Test
    @DisplayName("Cannot target 1-MV creature with X=2")
    void cannotTargetOneManaValueCreatureWithXTwo() {
        // Elite Vanguard has mana value 1, but X=2
        harness.addToBattlefield(player2, new EliteVanguard());
        UUID vanguardId = harness.getPermanentId(player2, "Elite Vanguard");

        harness.setHand(player1, List.of(new EntrancingMelody()));
        // X=2, plus {U}{U} = 4 total mana
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, vanguardId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature with mana value equal to X");
    }
}
