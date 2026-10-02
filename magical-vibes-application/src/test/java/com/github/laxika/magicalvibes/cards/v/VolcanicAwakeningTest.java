package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BenalishCavalry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VolcanicAwakening.class, Forest.class, BenalishCavalry.class})
class VolcanicAwakeningTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target land")
    void destroysTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        castVolcanicAwakening(forest.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BenalishCavalry());

        harness.setHand(player1, List.of(new VolcanicAwakening()));
        addManaForVolcanicAwakening();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("land");
    }

    @Test
    @DisplayName("Storm creates one copy for each spell cast before Volcanic Awakening")
    void stormCreatesCopiesForEachPriorSpell() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        gd.recordSpellCast(player1.getId(), new BenalishCavalry());
        gd.recordSpellCast(player2.getId(), new BenalishCavalry());

        castVolcanicAwakening(forest.getId());
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
    }

    @Test
    @DisplayName("Storm copy may be retargeted to destroy another land")
    void stormCopyMayBeRetargetedToAnotherLand() {
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent newTarget = harness.addToBattlefieldAndReturn(player2, new Forest());
        gd.recordSpellCast(player1.getId(), new BenalishCavalry());

        castVolcanicAwakening(originalTarget.getId());
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, newTarget.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    private void castVolcanicAwakening(UUID targetId) {
        harness.setHand(player1, List.of(new VolcanicAwakening()));
        addManaForVolcanicAwakening();
        harness.castSorcery(player1, 0, targetId);
    }

    private void addManaForVolcanicAwakening() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
