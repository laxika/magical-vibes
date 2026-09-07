package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.ApocalypseChime;
import com.github.laxika.magicalvibes.cards.a.AysenBureaucrats;
import com.github.laxika.magicalvibes.cards.b.BeastWalkers;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DrySpell.class, AysenBureaucrats.class, BeastWalkers.class, ApocalypseChime.class})
class DrySpellTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Dry Spell puts it on the stack as a sorcery")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new DrySpell()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
    }

    @Test
    @DisplayName("Dry Spell deals 1 damage to each creature on both sides")
    void dealsOneDamageToEachCreature() {
        var dyingCreature = harness.addToBattlefieldAndReturn(player1, new AysenBureaucrats());
        var survivingCreature = harness.addToBattlefieldAndReturn(player2, new BeastWalkers());
        harness.setHand(player1, List.of(new DrySpell()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(dyingCreature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(survivingCreature.getId()));
        assertThat(survivingCreature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Dry Spell deals 1 damage to each player")
    void dealsOneDamageToEachPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new DrySpell()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void doesNotDamageNoncreaturePermanents() {
        var artifact = harness.addToBattlefieldAndReturn(player1, new ApocalypseChime());
        harness.setHand(player1, List.of(new DrySpell()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        assertThat(artifact.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot cast Dry Spell without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new DrySpell()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card is not playable");
    }
}
