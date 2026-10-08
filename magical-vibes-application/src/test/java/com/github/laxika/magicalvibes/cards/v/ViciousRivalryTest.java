package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ViciousRivalry.class, GrizzlyBears.class, HillGiant.class, Island.class,
        MindStone.class, Ornithopter.class, Cancel.class, GloriousAnthem.class})
class ViciousRivalryTest extends BaseCardTest {

    private void castForX(int xValue) {
        harness.setHand(player1, List.of(new ViciousRivalry()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2 + xValue);
        harness.castAndResolveSorcery(player1, 0, xValue);
    }

    @Test
    @DisplayName("Pays X life and destroys artifacts and creatures with mana value X or less")
    void destroysArtifactsAndCreaturesWithinX() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new MindStone());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new Island());

        castForX(2);

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertNotOnBattlefield(player1, "Mind Stone");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player2, "Island");
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("X=0 destroys only zero-mana-value artifacts and creatures")
    void xZeroDestroysOnlyFreePermanents() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new GrizzlyBears());

        castForX(0);

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cannot pay more life than the caster has")
    void cannotPayMoreLifeThanAvailable() {
        harness.setLife(player1, 3);
        harness.setHand(player1, List.of(new ViciousRivalry()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 4))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 3);
    }

    @Test
    @DisplayName("X costs life, not extra mana, and life is paid before resolution")
    void fixedManaCostAndImmediateLifePayment() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new ViciousRivalry()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 4);

        harness.assertLife(player1, 16);
        harness.assertOnBattlefield(player2, "Hill Giant");

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player2, "Glorious Anthem");
        harness.assertOnBattlefield(player2, "Island");
        harness.assertLife(player1, 16);
    }

    @Test
    @DisplayName("Countering the spell does not refund its additional life cost")
    void counteredSpellKeepsLifePayment() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        ViciousRivalry rivalry = new ViciousRivalry();
        harness.setHand(player1, List.of(rivalry));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 2);
        harness.assertLife(player1, 18);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, rivalry.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Vicious Rivalry");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("A negative life payment cannot be announced")
    void cannotChooseNegativeX() {
        harness.setHand(player1, List.of(new ViciousRivalry()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, -1))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 20);
    }
}
