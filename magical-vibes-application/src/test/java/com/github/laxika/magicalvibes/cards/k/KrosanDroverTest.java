package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AvenFarseer;
import com.github.laxika.magicalvibes.cards.d.DecreeOfPain;
import com.github.laxika.magicalvibes.cards.e.ElvishAberration;
import com.github.laxika.magicalvibes.cards.r.RootElemental;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KrosanDrover.class, ElvishAberration.class, AvenFarseer.class,
        DecreeOfPain.class, RootElemental.class})
class KrosanDroverTest extends BaseCardTest {

    @Test
    @DisplayName("Creature spells with mana value 6 or greater cost {2} less")
    void reducesHighManaValueCreatureSpellCost() {
        harness.addToBattlefield(player1, new KrosanDrover());
        harness.setHand(player1, List.of(new ElvishAberration()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Creature spells with mana value less than 6 are not reduced")
    void doesNotReduceLowerManaValueCreatureSpell() {
        harness.addToBattlefield(player1, new KrosanDrover());
        harness.setHand(player1, List.of(new AvenFarseer()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction does not affect an opponent's creature spells")
    void doesNotReduceOpponentCreatureSpell() {
        harness.addToBattlefield(player1, new KrosanDrover());
        harness.setHand(player2, List.of(new ElvishAberration()));
        harness.addMana(player2, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction does not affect noncreature spells")
    void doesNotReduceNoncreatureSpell() {
        harness.addToBattlefield(player1, new KrosanDrover());
        harness.setHand(player1, List.of(new DecreeOfPain()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction does not affect a face-down morph spell")
    void doesNotReduceFaceDownMorphSpell() {
        harness.addToBattlefield(player1, new KrosanDrover());
        harness.setHand(player1, List.of(new RootElemental()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreatureWithMorph(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreatureWithMorph(player1, 0);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
