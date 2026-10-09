package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.n.NorthernAirTemple;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrescentIslandTemple.class, Shock.class, NorthernAirTemple.class})
class CrescentIslandTempleTest extends BaseCardTest {

    @Test
    void entryCreatesOneMonkPerShrineYouControl() {
        harness.addToBattlefield(player1, shrine());
        harness.addToBattlefield(player1, shrine());
        harness.setHand(player1, List.of(new CrescentIslandTemple()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Monk")).hasSize(3);
    }

    @Test
    void anotherShrineEnteringCreatesAMonk() {
        harness.setHand(player1, List.of(new CrescentIslandTemple()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Monk")).hasSize(1);

        harness.enterBattlefieldAndReturn(player1, shrine());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Monk")).hasSize(2);
    }

    @Test
    void createdMonksHaveProwess() {
        harness.setHand(player1, List.of(new CrescentIslandTemple()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent monk = findPermanents(player1, "Monk").getFirst();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(2);
    }

    @Test
    void entryCountsOnlyYourShrinesAtResolution() {
        harness.addToBattlefield(player2, new NorthernAirTemple());
        harness.setHand(player1, List.of(new CrescentIslandTemple()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new NorthernAirTemple());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Monk")).hasSize(2);
        assertThat(findPermanents(player2, "Monk")).isEmpty();
    }

    @Test
    void opponentsShrineDoesNotTriggerTokenCreation() {
        harness.addToBattlefield(player1, new CrescentIslandTemple());
        harness.enterBattlefieldAndReturn(player2, new NorthernAirTemple());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Monk")).isEmpty();
        assertThat(findPermanents(player2, "Monk")).isEmpty();
    }

    @Test
    void monksFromAnotherShrineHaveProwess() {
        harness.addToBattlefield(player1, new CrescentIslandTemple());
        harness.enterBattlefieldAndReturn(player1, new NorthernAirTemple());
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent monk = findPermanents(player1, "Monk").getFirst();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(2);
        harness.passBothPriorities();
    }

    @Test
    void opponentsNoncreatureSpellDoesNotTriggerProwess() {
        harness.enterBattlefieldAndReturn(player1, new CrescentIslandTemple());
        harness.passBothPriorities();
        Permanent monk = findPermanents(player1, "Monk").getFirst();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private Card shrine() {
        Card card = new Card();
        card.setName("Test Shrine");
        card.setType(CardType.ENCHANTMENT);
        card.setSubtypes(List.of(CardSubtype.SHRINE));
        return card;
    }
}
