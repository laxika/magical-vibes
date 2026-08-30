package com.github.laxika.magicalvibes.cards.r;

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

class RallyTheRanksTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a creature type boosts your matching creatures only")
    void boostsYourMatchingCreaturesOnly() {
        Permanent ownElf = harness.addToBattlefieldAndReturn(player1,
                createCreature("Own Elf", CardSubtype.ELF));
        Permanent ownGoblin = harness.addToBattlefieldAndReturn(player1,
                createCreature("Own Goblin", CardSubtype.GOBLIN));
        Permanent opponentElf = harness.addToBattlefieldAndReturn(player2,
                createCreature("Opponent Elf", CardSubtype.ELF));

        harness.setHand(player1, List.of(new RallyTheRanks()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "ELF");

        assertThat(gqs.getEffectivePower(gd, ownElf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownElf)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownGoblin)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownGoblin)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentElf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentElf)).isEqualTo(1);
    }

    @Test
    @DisplayName("The chosen type is stored on Rally the Ranks")
    void choosingTypeStoresItOnPermanent() {
        harness.setHand(player1, List.of(new RallyTheRanks()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WARRIOR");

        Permanent rally = findPermanent(player1, "Rally the Ranks");
        assertThat(rally.getChosenSubtype()).isEqualTo(CardSubtype.WARRIOR);
    }

    private static Card createCreature(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(subtype));
        return card;
    }
}
