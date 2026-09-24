package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FierceGuardianship.class, EdgarMarkov.class, GrizzlyBears.class, Opt.class})
class FierceGuardianshipTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast without paying its mana cost while controlling a commander")
    void freeCastWhileControllingCommander() {
        addCommanderToBattlefield();
        Opt target = new Opt();
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.setHand(player2, List.of(new FierceGuardianship()));
        harness.castInstant(player1, 0);
        harness.passPriority(player1);
        harness.castInstantWithAlternateCost(player2, 0, target.getId(), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Opt");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot use the free alternate cost without controlling a commander")
    void freeCastRequiresCommander() {
        harness.setHand(player1, List.of(new FierceGuardianship()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player1, 0, null, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new FierceGuardianship()));
        addCommanderToBattlefield();

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player2, 0, bears.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("noncreature spell");
    }

    private void addCommanderToBattlefield() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.makeCommander(player2.getId(), commander);
        harness.addToBattlefield(player2, commander);
    }
}
