package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BrinebornCutthroat;
import com.github.laxika.magicalvibes.cards.d.DirgeBat;
import com.github.laxika.magicalvibes.cards.d.DoubleMajor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TeferiMageOfZhalfir;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CunningNightbonder.class, BrinebornCutthroat.class, Cancel.class, GrizzlyBears.class,
        DirgeBat.class, DoubleMajor.class, TeferiMageOfZhalfir.class})
class CunningNightbonderTest extends BaseCardTest {

    @Test
    @DisplayName("Flash spells cost {1} less to cast")
    void flashSpellsCostOneLess() {
        harness.addToBattlefield(player1, new CunningNightbonder());
        harness.setHand(player1, List.of(new BrinebornCutthroat()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Spells without flash are not reduced")
    void spellsWithoutFlashAreNotReduced() {
        harness.addToBattlefield(player1, new CunningNightbonder());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flash spells you cast cannot be countered")
    void flashSpellsCannotBeCountered() {
        harness.addToBattlefield(player1, new CunningNightbonder());
        BrinebornCutthroat cutthroat = new BrinebornCutthroat();
        harness.setHand(player1, List.of(cutthroat));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, cutthroat.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Brineborn Cutthroat");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    void multipleNightbondersReduceGenericCostCumulatively() {
        harness.addToBattlefield(player1, new CunningNightbonder());
        harness.addToBattlefield(player1, new CunningNightbonder());
        harness.setHand(player1, List.of(new DirgeBat()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dirge Bat");
    }

    @Test
    void reductionDoesNotPayColoredMana() {
        harness.addToBattlefield(player1, new CunningNightbonder());
        harness.setHand(player1, List.of(new CunningNightbonder()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentsFlashSpellsDoNotReceiveCostReduction() {
        harness.addToBattlefield(player2, new CunningNightbonder());
        harness.setHand(player1, List.of(new DirgeBat()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void nightbonderOnStackDoesNotProtectItself() {
        CunningNightbonder nightbonder = new CunningNightbonder();
        harness.setHand(player1, List.of(nightbonder));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, nightbonder.getId());

        harness.assertInGraveyard(player1, "Cunning Nightbonder");
        harness.assertNotOnBattlefield(player1, "Cunning Nightbonder");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enteringNightbonderProtectsAnAlreadyCastFlashSpell() {
        DirgeBat bat = new DirgeBat();
        harness.setHand(player1, List.of(bat, new CunningNightbonder()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bat.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dirge Bat");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    void copiesOfFlashSpellsCanBeCountered() {
        harness.addToBattlefield(player1, new CunningNightbonder());
        DirgeBat bat = new DirgeBat();
        harness.setHand(player1, List.of(bat, new DoubleMajor()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, bat.getId());
        StackEntry copy = gd.stack.stream().filter(StackEntry::isCopy).findFirst().orElseThrow();
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, copy.getTargetableId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetableId()).isEqualTo(bat.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Dirge Bat");
    }

    @Test
    void spellsGrantedFlashReceiveCostReduction() {
        harness.addToBattlefield(player1, new CunningNightbonder());
        harness.addToBattlefield(player1, new TeferiMageOfZhalfir());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void spellsGrantedFlashCannotBeCountered() {
        harness.addToBattlefield(player1, new CunningNightbonder());
        harness.addToBattlefield(player1, new TeferiMageOfZhalfir());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears, new Cancel()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void opponentsNightbonderDoesNotPreventCounteringYourFlashSpell() {
        harness.addToBattlefield(player2, new CunningNightbonder());
        CunningNightbonder nightbonder = new CunningNightbonder();
        harness.setHand(player1, List.of(nightbonder));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, nightbonder.getId());

        harness.assertInGraveyard(player1, "Cunning Nightbonder");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void spellsWithoutFlashCanStillBeCountered() {
        harness.addToBattlefield(player1, new CunningNightbonder());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }
}
