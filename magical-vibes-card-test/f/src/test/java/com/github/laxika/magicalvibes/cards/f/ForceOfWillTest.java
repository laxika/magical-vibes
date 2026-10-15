package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AngelOfJubilation;
import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForceOfWill.class, GrizzlyBears.class, Counterspell.class, AngelOfJubilation.class})
class ForceOfWillTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving counters the target spell")
    void countersTargetSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new ForceOfWill()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can be cast by paying 1 life and exiling a blue card instead of paying mana")
    void castWithAlternateCost() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new ForceOfWill(), new Counterspell()));
        int lifeBefore = gd.getLife(player2.getId());

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstantWithAlternateExileFromHand(player2, 0, bears.getId(), 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(e -> e.card().getName()).containsExactly("Counterspell");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Alternate cost rejects exiling a non-blue card")
    void alternateCostRequiresBlueCard() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new ForceOfWill(), new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(player2, 0, bears.getId(), 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed(AngelOfJubilation.class)
    void alternateCostCannotPayLifeWhenLifePaymentsAreProhibited() {
        harness.addToBattlefield(player1, new AngelOfJubilation());

        GrizzlyBears bears = new GrizzlyBears();
        ForceOfWill forceOfWill = new ForceOfWill();
        Counterspell counterspell = new Counterspell();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(forceOfWill, counterspell));
        int lifeBefore = gd.getLife(player2.getId());

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(player2, 0, bears.getId(), 1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(forceOfWill, counterspell);
    }

    @Test
    @DisplayName("Alternate costs are paid on casting and can exile a blue card before the spell in hand")
    void alternateCostsArePaidBeforeResolutionWithEarlierHandIndex() {
        GrizzlyBears bears = new GrizzlyBears();
        ForceOfWill pitchCard = new ForceOfWill();
        ForceOfWill spell = new ForceOfWill();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(pitchCard, spell));
        harness.setLife(player2, 2);

        harness.castCreature(player1, 0);
        harness.castInstantWithAlternateExileFromHand(player2, 1, bears.getId(), 0);

        harness.assertLife(player2, 1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(e -> e.card()).containsExactly(pitchCard);
        assertThat(gd.stack).extracting(e -> e.getCard()).containsExactly(bears, spell);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Force of Will");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Force of Will cannot exile itself to pay its alternate cost")
    void cannotExileTheSpellBeingCast() {
        GrizzlyBears bears = new GrizzlyBears();
        ForceOfWill spell = new ForceOfWill();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(spell));
        int lifeBefore = gd.getLife(player2.getId());
        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(player2, 0, bears.getId(), 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player2, lifeBefore);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(spell);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.stack).extracting(e -> e.getCard()).containsExactly(bears);
    }

    @Test
    @DisplayName("Can counter an instant targeting its controller's creature spell")
    void countersInstantSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        Counterspell counterspell = new Counterspell();
        harness.setHand(player1, List.of(bears, new ForceOfWill()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.setHand(player2, List.of(counterspell));
        harness.addMana(player2, ManaColor.BLUE, 2);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castCreature(player1, 0);
        harness.castInstant(player2, 0, bears.getId());
        harness.castAndResolveInstant(player1, 0, counterspell.getId());

        harness.assertInGraveyard(player2, "Counterspell");
        harness.assertInGraveyard(player1, "Force of Will");
        harness.assertLife(player1, lifeBefore);
        assertThat(gd.exiledCards).isEmpty();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
