package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        harness.setHand(player1, List.of(artifact("Elf Relic", CardSubtype.ELF)));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 11);
    }

    @Test
    @DisplayName("The activated ability returns a card of the chosen type and sacrifices the Pennant")
    void returnsCardOfChosenType() {
        Permanent pennant = addPennant(CardSubtype.ELF);
        Card elf = artifact("Graveyard Elf", CardSubtype.ELF);
        Card goblin = artifact("Graveyard Goblin", CardSubtype.GOBLIN);
        harness.setGraveyard(player1, List.of(elf, goblin));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int pennantIndex = gd.playerBattlefields.get(player1.getId()).indexOf(pennant);
        harness.activateAbilityWithGraveyardTargets(player1, pennantIndex, 0, List.of(elf.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Graveyard Elf");
        harness.assertNotInGraveyard(player1, "Graveyard Elf");
        harness.assertInGraveyard(player1, "Dawn-Blessed Pennant");
    }

    @Test
    @DisplayName("The activated ability cannot target a card of a different type")
    void cannotTargetDifferentType() {
        Permanent pennant = addPennant(CardSubtype.ELF);
        Card goblin = artifact("Graveyard Goblin", CardSubtype.GOBLIN);
        harness.setGraveyard(player1, List.of(goblin));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int pennantIndex = gd.playerBattlefields.get(player1.getId()).indexOf(pennant);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, pennantIndex, 0, List.of(goblin.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addPennant(CardSubtype chosenSubtype) {
        Permanent pennant = new Permanent(new DawnBlessedPennant());
        pennant.setChosenSubtype(chosenSubtype);
        gd.playerBattlefields.get(player1.getId()).add(pennant);
        return pennant;
    }

    private Card artifact(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setManaCost("{0}");
        card.setType(CardType.ARTIFACT);
        card.setSubtypes(List.of(subtype));
        return card;
    }
}
