package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BantSureblade;
import com.github.laxika.magicalvibes.cards.d.DromokaTheEternal;
import com.github.laxika.magicalvibes.cards.u.UginsConstruct;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NeutralizingBlast.class, GrizzlyBears.class, BantSureblade.class,
        DromokaTheEternal.class, UginsConstruct.class})
class NeutralizingBlastTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a multicolored spell")
    void countersMulticoloredSpell() {
        BantSureblade sureblade = new BantSureblade();
        harness.setHand(player1, List.of(sureblade));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.setHand(player2, List.of(new NeutralizingBlast()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, sureblade.getId());

        harness.assertInGraveyard(player1, "Bant Sureblade");
        harness.assertNotOnBattlefield(player1, "Bant Sureblade");
    }

    @Test
    @DisplayName("Cannot target a monocolored spell")
    void cannotTargetMonocoloredSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new NeutralizingBlast()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters a two-color spell")
    void countersTwoColorSpell() {
        DromokaTheEternal dromoka = new DromokaTheEternal();
        harness.setHand(player1, List.of(dromoka));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player2, List.of(new NeutralizingBlast()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, dromoka.getId());

        harness.assertInGraveyard(player1, "Dromoka, the Eternal");
        harness.assertNotOnBattlefield(player1, "Dromoka, the Eternal");
        harness.assertInGraveyard(player2, "Neutralizing Blast");
    }

    @Test
    @DisplayName("Cannot target a colorless spell")
    void cannotTargetColorlessSpell() {
        UginsConstruct construct = new UginsConstruct();
        harness.setHand(player1, List.of(construct));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setHand(player2, List.of(new NeutralizingBlast()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, construct.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player2, "Neutralizing Blast");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Ugin's Construct");
    }
}
