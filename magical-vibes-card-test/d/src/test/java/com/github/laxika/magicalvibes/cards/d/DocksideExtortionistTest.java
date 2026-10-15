package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BidentOfThassa;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DocksideExtortionist.class, BidentOfThassa.class, LightningBolt.class})
class DocksideExtortionistTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Treasure for each opposing artifact or enchantment")
    void createsTreasureForOpposingArtifactsAndEnchantments() {
        addPermanent(player1, CardType.ARTIFACT, "Own artifact");
        addPermanent(player2, CardType.ARTIFACT, "Opposing artifact");
        addPermanent(player2, CardType.ENCHANTMENT, "Opposing enchantment one");
        addPermanent(player2, CardType.ENCHANTMENT, "Opposing enchantment two");
        addPermanent(player2, CardType.CREATURE, "Opposing creature");

        harness.setHand(player1, List.of(new DocksideExtortionist()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(3);
    }

    @Test
    @DisplayName("Creates no Treasure when opponents control no artifacts or enchantments")
    void createsNoTreasureWithoutOpposingArtifactsOrEnchantments() {
        addPermanent(player2, CardType.CREATURE, "Opposing creature");

        harness.setHand(player1, List.of(new DocksideExtortionist()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("An opposing artifact enchantment counts once and your own does not count")
    void countsArtifactEnchantmentOnlyOnce() {
        harness.addToBattlefield(player1, new BidentOfThassa());
        harness.addToBattlefield(player2, new BidentOfThassa());
        harness.setHand(player1, List.of(new DocksideExtortionist()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Counts opposing permanents on resolution rather than when the trigger is created")
    void countsPermanentsAddedAfterEntering() {
        harness.setHand(player1, List.of(new DocksideExtortionist()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        harness.addToBattlefield(player2, new BidentOfThassa());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("The trigger creates Treasure even after Dockside Extortionist dies")
    void triggerResolvesAfterSourceDies() {
        harness.addToBattlefield(player2, new BidentOfThassa());
        harness.setHand(player1, List.of(new DocksideExtortionist(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.castAndResolveInstant(player1, 0,
                findPermanent(player1, "Dockside Extortionist").getId());
        harness.assertInGraveyard(player1, "Dockside Extortionist");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    private void addPermanent(com.github.laxika.magicalvibes.model.Player player,
                              CardType type, String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        harness.addToBattlefield(player, card);
    }
}
