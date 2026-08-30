package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.Aluren;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Nix.class, Aluren.class, GrizzlyBears.class})
class NixTest extends BaseCardTest {

    @Test
    void countersSpellCastWithoutSpendingMana() {
        harness.addToBattlefield(player1, new Aluren());
        GrizzlyBears targetSpell = new GrizzlyBears();
        harness.setHand(player1, List.of(targetSpell));
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Nix()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, targetSpell.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    void doesNotCounterSpellCastWithMana() {
        GrizzlyBears targetSpell = new GrizzlyBears();
        harness.setHand(player1, List.of(targetSpell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Nix()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, targetSpell.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Nix");
        assertThat(harness.getGameData().stack).isEmpty();
    }
}
