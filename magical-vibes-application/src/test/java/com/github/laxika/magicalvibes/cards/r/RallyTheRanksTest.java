package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BrokenWings;
import com.github.laxika.magicalvibes.cards.j.JasperaSentinel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RallyTheRanks.class, JasperaSentinel.class, BrokenWings.class})
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

        harness.castFromHand(player1, new RallyTheRanks(), "{1}{W}");
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
        harness.castFromHand(player1, new RallyTheRanks(), "{1}{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WARRIOR");

        Permanent rally = findPermanent(player1, "Rally the Ranks");
        assertThat(rally.getChosenSubtype()).isEqualTo(CardSubtype.WARRIOR);
    }

    @Test
    @DisplayName("A type absent from the battlefield can be chosen and boosts later arrivals")
    void boostsMatchingCreaturesEnteringLater() {
        harness.castFromHand(player1, new RallyTheRanks(), "{1}{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ELF");

        harness.castFromHand(player1, new JasperaSentinel(), "{G}");
        harness.passBothPriorities();

        Permanent sentinel = findPermanent(player1, "Jaspera Sentinel");
        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(3);
    }

    @Test
    @DisplayName("Separate type choices stack on a creature with both types")
    void separateChoicesStackOnCreatureWithBothTypes() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new JasperaSentinel());

        harness.castFromHand(player1, new RallyTheRanks(), "{1}{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ELF");

        harness.castFromHand(player1, new RallyTheRanks(), "{1}{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ROGUE");

        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(4);
    }

    @Test
    @DisplayName("The bonus ends when Rally the Ranks leaves the battlefield")
    void bonusEndsWhenEnchantmentIsDestroyed() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new JasperaSentinel());
        harness.castFromHand(player1, new RallyTheRanks(), "{1}{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ELF");

        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(3);

        harness.setHand(player1, List.of(new BrokenWings()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Rally the Ranks"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rally the Ranks");
        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(2);
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
