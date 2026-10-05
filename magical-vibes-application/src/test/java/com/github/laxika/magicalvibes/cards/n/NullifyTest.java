package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AbundantGrowth;
import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Nullify.class, GrizzlyBears.class, Forest.class, AbundantGrowth.class, AngelicChorus.class,
        NyxbornWolf.class})
class NullifyTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a creature spell")
    void countersCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(new Nullify()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castFromHand(player1, bears, "{1}{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Nullify");
    }

    @Test
    @DisplayName("Counters an Aura spell")
    void countersAuraSpell() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        AbundantGrowth growth = new AbundantGrowth();
        harness.setHand(player1, List.of(growth));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new Nullify()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, growth.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Abundant Growth");
        harness.assertNotOnBattlefield(player1, "Abundant Growth");
    }

    @Test
    @DisplayName("Cannot target a non-Aura enchantment spell")
    void cannotTargetNonAuraEnchantmentSpell() {
        AngelicChorus chorus = new AngelicChorus();
        harness.setHand(player2, List.of(new Nullify()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castFromHand(player1, chorus, "{3}{W}{W}");
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, chorus.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters a creature cast for its bestow cost as an Aura")
    void countersBestowedCreature() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new NyxbornWolf());
        NyxbornWolf spell = new NyxbornWolf();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player2, List.of(new Nullify()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, spell.getId());

        harness.assertInGraveyard(player1, "Nyxborn Wolf");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .containsExactly(host);
        harness.assertInGraveyard(player2, "Nullify");
    }

    @Test
    @DisplayName("Can counter its controller's own creature spell")
    void countersOwnCreatureSpell() {
        NyxbornWolf wolf = new NyxbornWolf();
        harness.castFromHand(player1, wolf, "{2}{G}");
        harness.setHand(player1, List.of(new Nullify()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, wolf.getId());

        harness.assertInGraveyard(player1, "Nyxborn Wolf");
        harness.assertNotOnBattlefield(player1, "Nyxborn Wolf");
        harness.assertInGraveyard(player1, "Nullify");
    }

    @Test
    @DisplayName("Cannot target another instant spell")
    void cannotTargetInstantSpell() {
        NyxbornWolf wolf = new NyxbornWolf();
        harness.castFromHand(player1, wolf, "{2}{G}");
        Nullify first = new Nullify();
        harness.setHand(player1, List.of(first));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, wolf.getId());
        harness.passPriority(player1);
        harness.setHand(player2, List.of(new Nullify()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, first.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
