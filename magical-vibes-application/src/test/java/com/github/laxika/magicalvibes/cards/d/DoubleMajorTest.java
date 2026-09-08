package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AngelsMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MirriCatWarrior;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DoubleMajorTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a token copy of a creature spell you control")
    void createsTokenCopyOfCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears, new DoubleMajor()));
        addManaForDoubleMajor();

        harness.castCreature(player1, 0);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("A legendary creature spell becomes a nonlegendary token copy")
    void legendaryCreatureSpellBecomesNonlegendaryTokenCopy() {
        MirriCatWarrior mirri = new MirriCatWarrior();
        harness.setHand(player1, List.of(mirri, new DoubleMajor()));
        addManaForDoubleMajor();

        harness.castCreature(player1, 0);
        harness.castInstant(player1, 0, mirri.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Cannot target an instant spell")
    void cannotTargetInstantSpell() {
        AngelsMercy mercy = new AngelsMercy();
        harness.setHand(player1, List.of(mercy, new DoubleMajor()));
        addManaForDoubleMajor();

        harness.castInstant(player1, 0);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, mercy.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addManaForDoubleMajor() {
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
