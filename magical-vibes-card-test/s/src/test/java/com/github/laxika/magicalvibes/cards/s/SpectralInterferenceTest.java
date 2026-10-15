package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpectralInterference.class, GrizzlyBears.class, LlanowarElves.class, MightOfOaks.class, Millstone.class})
class SpectralInterferenceTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an artifact spell when its controller cannot pay")
    void countersArtifactSpell() {
        Millstone millstone = new Millstone();

        harness.setHand(player2, List.of(new SpectralInterference()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castFromHand(player1, millstone, "{2}");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, millstone.getId());

        harness.assertInGraveyard(player1, "Millstone");
        harness.assertInGraveyard(player2, "Spectral Interference");
    }

    @Test
    @DisplayName("Lets a creature spell resolve when its controller pays {4}")
    void creatureSpellResolvesWhenControllerPays() {
        LlanowarElves elves = new LlanowarElves();
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new SpectralInterference()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castFromHand(player1, elves, "{G}");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, elves.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Spectral Interference");
    }

    @Test
    @DisplayName("Counters a creature spell when its controller declines to pay")
    void creatureSpellIsCounteredWhenControllerDeclines() {
        LlanowarElves elves = new LlanowarElves();
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new SpectralInterference()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castFromHand(player1, elves, "{G}");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, elves.getId());

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Spectral Interference");
    }

    @Test
    @DisplayName("Cannot target a non-artifact, noncreature spell")
    void cannotTargetNonArtifactNoncreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.setHand(player2, List.of(new SpectralInterference()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, might.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Three remaining mana cannot pay the four-mana tax")
    void countersCreatureWithOnlyThreeManaRemaining() {
        LlanowarElves elves = new LlanowarElves();
        harness.castFromHand(player1, elves, "{G}");
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.setHand(player2, List.of(new SpectralInterference()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);

        harness.castAndResolveInstant(player2, 0, elves.getId());

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    @DisplayName("An artifact spell survives payment with four colored mana")
    void artifactSpellResolvesAfterPayingWithColoredMana() {
        Millstone millstone = new Millstone();
        harness.castFromHand(player1, millstone, "{2}");
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player2, List.of(new SpectralInterference()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);

        harness.castAndResolveInstant(player2, 0, millstone.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Millstone");
        harness.assertNotInGraveyard(player1, "Millstone");
    }

    @Test
    @DisplayName("Cannot target an activated ability of an artifact")
    void cannotTargetArtifactActivatedAbility() {
        harness.addToBattlefield(player1, new Millstone());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new SpectralInterference()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, null, player2.getId());
        var abilityId = gd.stack.getFirst().getTargetableId();
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, abilityId))
                .isInstanceOf(IllegalStateException.class);
    }
}
