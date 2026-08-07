package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class KytheonsTacticsTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control get +2/+1 until end of turn")
    void boostsOwnCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        cast(player1);

        Permanent bears = permanentOf(player1, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Opponent creatures are unaffected")
    void opponentCreaturesUnaffected() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast(player1);

        Permanent bears = permanentOf(player2, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Without spell mastery no vigilance is granted")
    void withoutSpellMasteryNoVigilance() {
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.addToBattlefield(player1, new GrizzlyBears());

        cast(player1);

        Permanent bears = permanentOf(player1, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Spell mastery grants vigilance in addition to the boost")
    void spellMasteryGrantsVigilance() {
        harness.setGraveyard(player1, List.of(new Shock(), new LavaAxe()));
        harness.addToBattlefield(player1, new GrizzlyBears());

        cast(player1);

        Permanent bears = permanentOf(player1, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
        assertThat(bears.hasKeyword(Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Boost and vigilance wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        harness.setGraveyard(player1, List.of(new Shock(), new LavaAxe()));
        harness.addToBattlefield(player1, new GrizzlyBears());

        cast(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = permanentOf(player1, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    private void cast(Player player) {
        harness.setHand(player, List.of(new KytheonsTactics()));
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.castInstant(player, 0, (UUID) null);
        harness.passBothPriorities();
    }

    private Permanent permanentOf(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> name.equals(p.getCard().getName()))
                .findFirst()
                .orElseThrow();
    }
}
