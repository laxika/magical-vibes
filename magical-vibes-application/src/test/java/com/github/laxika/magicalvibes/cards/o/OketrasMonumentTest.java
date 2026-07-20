package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OketrasMonumentTest extends BaseCardTest {

    // ===== Cost reduction: white creature spells cost {1} less =====

    @Test
    @DisplayName("White creature spells you cast cost {1} less")
    void whiteCreatureSpellsCostOneLess() {
        harness.addToBattlefield(player1, new OketrasMonument());
        // Angel of Mercy costs {4}{W} — with {1} reduction it should cost {3}{W}
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(e -> e.getCard().getName().equals("Angel of Mercy"));
    }

    @Test
    @DisplayName("Cannot cast a white creature without enough mana even with the reduction")
    void cannotCastWithoutEnoughManaDespiteReduction() {
        harness.addToBattlefield(player1, new OketrasMonument());
        // Angel of Mercy reduced cost is {3}{W}; {2}{W} is one short
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-white creature spells are not reduced")
    void nonWhiteCreatureSpellsNotReduced() {
        harness.addToBattlefield(player1, new OketrasMonument());
        // Hill Giant costs {3}{R}; it is red, so it is not reduced and {2}{R} is not enough
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Trigger: create a 1/1 white Warrior with vigilance on each creature cast =====

    @Test
    @DisplayName("Casting a creature spell creates a 1/1 white Warrior with vigilance")
    void castingCreatureCreatesWarriorToken() {
        harness.addToBattlefield(player1, new OketrasMonument());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        // Resolve the spell-cast trigger (on top of the stack)
        harness.passBothPriorities();

        Permanent warrior = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Warrior"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Casting a noncreature spell does not create a Warrior token")
    void castingNoncreatureSpellCreatesNoToken() {
        harness.addToBattlefield(player1, new OketrasMonument());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Warrior"));
    }
}
