package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CullingRitualTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys nonland permanents with mana value 2 or less and adds mana for each")
    void destroysMatchingPermanentsAndAddsMana() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new RuleOfLaw());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new CullingRitual()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Rule of Law");
        harness.assertOnBattlefield(player1, "Mountain");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Adds no mana and asks no color choice when nothing is destroyed")
    void addsNoManaWhenNothingMatches() {
        harness.addToBattlefield(player1, new RuleOfLaw());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new CullingRitual()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rule of Law");
        harness.assertOnBattlefield(player1, "Mountain");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
