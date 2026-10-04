package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DoomedTraveler;
import com.github.laxika.magicalvibes.cards.v.VictimOfNight;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GeistHonoredMonk.class, DoomedTraveler.class, VictimOfNight.class})
class GeistHonoredMonkTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates two 1/1 white Spirit tokens with flying")
    void etbCreatesTwoSpiritTokens() {
        castAndResolveMonk();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(3); // Monk + 2 tokens
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(2);
        for (Permanent token : findPermanents(player1, "Spirit")) {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
            assertThat(gqs.isCreature(gd, token)).isTrue();
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveCardColor(gd, token.getCard())).isEqualTo(CardColor.WHITE);
        }
    }

    @Test
    @DisplayName("Spirit tokens have flying")
    void spiritTokensHaveFlying() {
        castAndResolveMonk();

        Permanent token = findPermanent(player1, "Spirit");
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Geist-Honored Monk is 3/3 after ETB resolves (itself + 2 Spirit tokens)")
    void ptAfterEtb() {
        castAndResolveMonk();

        Permanent monk = findPermanent(player1, "Geist-Honored Monk");
        // Monk + 2 Spirit tokens = 3 creatures
        assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(3);
    }

    @Test
    @DisplayName("P/T increases when more creatures enter")
    void ptIncreasesWithMoreCreatures() {
        castAndResolveMonk();
        harness.addToBattlefield(player1, new DoomedTraveler());

        Permanent monk = findPermanent(player1, "Geist-Honored Monk");
        // Monk + 2 Spirits + Traveler = 4
        assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(4);
    }

    @Test
    @DisplayName("P/T decreases when creatures leave")
    void ptDecreasesWhenCreaturesLeave() {
        castAndResolveMonk();

        Permanent monk = findPermanent(player1, "Geist-Honored Monk");
        assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(3);

        // Remove the Spirit tokens
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Spirit"));

        // Now only the Monk itself
        assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not count opponent's creatures")
    void doesNotCountOpponentCreatures() {
        castAndResolveMonk();
        harness.addToBattlefield(player2, new DoomedTraveler());
        harness.addToBattlefield(player2, new DoomedTraveler());

        Permanent monk = findPermanent(player1, "Geist-Honored Monk");
        // Only counts own creatures: Monk + 2 Spirits = 3
        assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(3);
    }

    @Test
    @DisplayName("Monk counts itself before its entry trigger creates Spirits")
    void countsItselfBeforeEntryTriggerResolves() {
        harness.setHand(player1, List.of(new GeistHonoredMonk()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent monk = findPermanent(player1, "Geist-Honored Monk");
        assertThat(countPermanents(player1, "Spirit")).isZero();
        assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(1);

        resolveAllTriggers();
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(2);
    }

    @Test
    @DisplayName("Entry trigger creates Spirits even if Monk is destroyed in response")
    void entryTriggerSurvivesSourceRemoval() {
        harness.setHand(player1, List.of(new GeistHonoredMonk()));
        harness.setHand(player2, List.of(new VictimOfNight()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent monk = findPermanent(player1, "Geist-Honored Monk");
        harness.castAndResolveInstant(player2, 0, monk.getId());
        harness.assertInGraveyard(player1, "Geist-Honored Monk");
        harness.assertNotOnBattlefield(player1, "Geist-Honored Monk");
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(2);
        assertThat(countPermanents(player2, "Spirit")).isZero();
    }

    @Test
    @DisplayName("Power and toughness in hand and graveyard count only battlefield creatures")
    void characteristicAbilityWorksOutsideBattlefield() {
        GeistHonoredMonk monk = new GeistHonoredMonk();
        harness.setHand(player1, List.of(monk));
        assertThat(gqs.getEffectiveCardPower(gd, monk)).isZero();
        assertThat(gqs.getEffectiveCardToughness(gd, monk)).isZero();

        harness.addToBattlefield(player1, new DoomedTraveler());
        harness.addToBattlefield(player2, new DoomedTraveler());
        assertThat(gqs.getEffectiveCardPower(gd, monk)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, monk)).isEqualTo(1);

        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(monk));
        assertThat(gqs.getEffectiveCardPower(gd, monk)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, monk)).isEqualTo(1);
    }

    private void castAndResolveMonk() {
        harness.setHand(player1, List.of(new GeistHonoredMonk()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

}
