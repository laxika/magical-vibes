package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AbjureTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a blue permanent and counters the target spell")
    void sacrificesBluePermanentAndCountersSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Permanent wizard = new Permanent(new FugitiveWizard());
        gd.playerBattlefields.get(player2.getId()).add(wizard);
        harness.setHand(player2, List.of(new Abjure()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstantWithSacrifice(player2, 0, bears.getId(), wizard.getId());

        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
        harness.assertInGraveyard(player2, "Fugitive Wizard");

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).noneMatch(se -> se.getCard().getName().equals("Grizzly Bears"));
        harness.assertInGraveyard(player2, "Abjure");
    }

    @Test
    @DisplayName("Cannot cast without a permanent to sacrifice")
    void cannotCastWithoutSacrifice() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Abjure()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player2, 0, bears.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot sacrifice a nonblue permanent")
    void cannotSacrificeNonbluePermanent() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Permanent greenCreature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(greenCreature);
        harness.setHand(player2, List.of(new Abjure()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() ->
                harness.castInstantWithSacrifice(player2, 0, bears.getId(), greenCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
