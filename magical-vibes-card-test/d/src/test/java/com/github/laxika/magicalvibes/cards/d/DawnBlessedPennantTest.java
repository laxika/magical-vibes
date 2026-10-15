package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.cards.c.CribSwap;
import com.github.laxika.magicalvibes.cards.e.EclipsedElf;
import com.github.laxika.magicalvibes.cards.e.EclipsedBoggart;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DawnBlessedPennant.class, EclipsedElf.class, EclipsedBoggart.class, CribSwap.class})
class DawnBlessedPennantTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a subtype offers only the types named by Dawn-Blessed Pennant")
    void subtypeChoiceIsRestricted() {
        harness.setHand(player1, List.of(new DawnBlessedPennant()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly(
                "ELEMENTAL", "ELF", "FAERIE", "GIANT", "GOBLIN", "KITHKIN", "MERFOLK", "TREEFOLK");
    }

    @Test
    @DisplayName("A permanent of the chosen type entering under your control gains you 1 life")
    void matchingPermanentGainsLife() {
        addPennant(CardSubtype.ELF);
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new EclipsedElf()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 11);
    }

    @Test
    @DisplayName("The activated ability returns a card of the chosen type and sacrifices the Pennant")
    void returnsCardOfChosenType() {
        Permanent pennant = addPennant(CardSubtype.ELF);
        Card elf = new EclipsedElf();
        Card goblin = new EclipsedBoggart();
        harness.setGraveyard(player1, List.of(elf, goblin));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int pennantIndex = gd.playerBattlefields.get(player1.getId()).indexOf(pennant);
        harness.activateAbilityWithGraveyardTargets(player1, pennantIndex, 0, List.of(elf.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Eclipsed Elf");
        harness.assertNotInGraveyard(player1, "Eclipsed Elf");
        harness.assertInGraveyard(player1, "Dawn-Blessed Pennant");
    }

    @Test
    @DisplayName("The activated ability cannot target a card of a different type")
    void cannotTargetDifferentType() {
        Permanent pennant = addPennant(CardSubtype.ELF);
        Card goblin = new EclipsedBoggart();
        harness.setGraveyard(player1, List.of(goblin));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int pennantIndex = gd.playerBattlefields.get(player1.getId()).indexOf(pennant);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, pennantIndex, 0, List.of(goblin.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addPennant(CardSubtype chosenSubtype) {
        Permanent pennant = harness.addToBattlefieldAndReturn(player1, new DawnBlessedPennant());
        pennant.setChosenSubtype(chosenSubtype);
        return pennant;
    }

    @Test
    void chosenTypeIsUsedByTheSacrificedAbility() {
        harness.setHand(player1, List.of(new DawnBlessedPennant()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, "ELF");

        harness.assertOnBattlefield(player1, "Dawn-Blessed Pennant");
        Card elf = new EclipsedElf();
        harness.setGraveyard(player1, List.of(elf));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(elf.getId()));
        harness.assertNotOnBattlefield(player1, "Dawn-Blessed Pennant");
        harness.assertNotInHand(player1, "Eclipsed Elf");
        harness.passBothPriorities();
        harness.assertInHand(player1, "Eclipsed Elf");
    }

    @Test
    void opponentsMatchingPermanentDoesNotGainLife() {
        addPennant(CardSubtype.ELF);
        harness.setLife(player1, 10);
        harness.setLibrary(player2, List.of());
        harness.enterBattlefieldAndReturn(player2, new EclipsedElf());
        harness.passBothPriorities();
        harness.assertLife(player1, 10);
    }

    @Test
    void nonmatchingPermanentDoesNotGainLife() {
        addPennant(CardSubtype.ELF);
        harness.setLife(player1, 10);
        harness.setLibrary(player1, List.of());
        harness.enterBattlefieldAndReturn(player1, new EclipsedBoggart());
        harness.passBothPriorities();
        harness.assertLife(player1, 10);
    }

    @Test
    void changelingTokenGainsLifeOnlyOnce() {
        addPennant(CardSubtype.ELF);
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new EclipsedElf());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new CribSwap()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0, elf.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 11);
    }

    @Test
    void returnsNoncreatureChangelingCard() {
        addPennant(CardSubtype.TREEFOLK);
        Card changeling = new CribSwap();
        harness.setGraveyard(player1, List.of(changeling));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(changeling.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Crib Swap");
        harness.assertNotInGraveyard(player1, "Crib Swap");
    }

    @Test
    void cannotReturnOpponentsCard() {
        addPennant(CardSubtype.ELF);
        Card elf = new EclipsedElf();
        harness.setGraveyard(player2, List.of(elf));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(elf.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Dawn-Blessed Pennant");
        harness.assertInGraveyard(player2, "Eclipsed Elf");
    }

    @Test
    void tappedPennantCannotActivate() {
        Permanent pennant = addPennant(CardSubtype.ELF);
        pennant.tap();
        Card elf = new EclipsedElf();
        harness.setGraveyard(player1, List.of(elf));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(elf.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Dawn-Blessed Pennant");
    }

    @Test
    void targetLeavingGraveyardDoesNotReturnOrRefundSacrifice() {
        addPennant(CardSubtype.ELF);
        Card elf = new EclipsedElf();
        harness.setGraveyard(player1, List.of(elf));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(elf.getId()));

        gd.playerGraveyards.get(player1.getId()).remove(elf);
        harness.setExile(player1, List.of(elf));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Eclipsed Elf");
        harness.assertNotOnBattlefield(player1, "Dawn-Blessed Pennant");
        harness.assertInGraveyard(player1, "Dawn-Blessed Pennant");
        assertThat(gd.exiledCards).anySatisfy(entry -> assertThat(entry.card()).isSameAs(elf));
    }
}
