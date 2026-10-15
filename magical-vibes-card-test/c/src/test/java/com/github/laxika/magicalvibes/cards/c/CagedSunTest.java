package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.s.ShimmeringGrotto;
import com.github.laxika.magicalvibes.cards.w.WoodlandCemetery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CagedSun.class, Forest.class, Mountain.class, ShimmeringGrotto.class, WoodlandCemetery.class})
class CagedSunTest extends BaseCardTest {

    private static Card createCreature(String name, int power, int toughness, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    @Test
    @DisplayName("Casting Caged Sun puts it on the stack as an artifact spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new CagedSun()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }

    @Test
    @DisplayName("Resolving Caged Sun enters battlefield and awaits color choice")
    void resolvingTriggersColorChoice() {
        harness.setHand(player1, List.of(new CagedSun()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Caged Sun");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Choosing a color sets chosenColor on Caged Sun")
    void choosingColorSetsOnPermanent() {
        harness.setHand(player1, List.of(new CagedSun()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        Permanent cagedSun = findPermanent(player1, "Caged Sun");
        assertThat(cagedSun.getChosenColor()).isEqualTo(CardColor.GREEN);
    }

    @Test
    @DisplayName("Creatures of chosen color get +1/+1")
    void boostsCreaturesOfChosenColor() {
        Card greenCreature = createCreature("Green Bear", 2, 2, CardColor.GREEN);
        harness.addToBattlefield(player1, greenCreature);

        // Add Caged Sun with chosen color green
        Permanent cagedSunPerm = harness.addToBattlefieldAndReturn(player1, new CagedSun());
        cagedSunPerm.setChosenColor(CardColor.GREEN);

        Permanent bear = findPermanent(player1, "Green Bear");

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
    }

    @Test
    @DisplayName("Creatures of different color do not get boosted")
    void doesNotBoostDifferentColor() {
        Card redCreature = createCreature("Red Goblin", 1, 1, CardColor.RED);
        harness.addToBattlefield(player1, redCreature);

        Permanent cagedSunPerm = harness.addToBattlefieldAndReturn(player1, new CagedSun());
        cagedSunPerm.setChosenColor(CardColor.GREEN);

        Permanent goblin = findPermanent(player1, "Red Goblin");

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not boost opponent's creatures of chosen color")
    void doesNotBoostOpponentCreatures() {
        Card greenCreature = createCreature("Green Bear", 2, 2, CardColor.GREEN);
        harness.addToBattlefield(player2, greenCreature);

        Permanent cagedSunPerm = harness.addToBattlefieldAndReturn(player1, new CagedSun());
        cagedSunPerm.setChosenColor(CardColor.GREEN);

        Permanent bear = findPermanent(player2, "Green Bear");

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("No boost if no color was chosen yet")
    void noBoostWithoutChosenColor() {
        Card greenCreature = createCreature("Green Bear", 2, 2, CardColor.GREEN);
        harness.addToBattlefield(player1, greenCreature);

        // Add Caged Sun without setting chosen color
        harness.addToBattlefield(player1, new CagedSun());

        Permanent bear = findPermanent(player1, "Green Bear");

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapping a Forest with green chosen adds extra green mana")
    void extraManaOnMatchingLandTap() {
        Permanent cagedSunPerm = harness.addToBattlefieldAndReturn(player1, new CagedSun());
        cagedSunPerm.setChosenColor(CardColor.GREEN);

        harness.addToBattlefield(player1, new Forest());

        // Forest is at index 1 (Caged Sun at index 0)
        harness.tapPermanent(player1, 1);

        // Should get 2 green mana: 1 from Forest + 1 from Caged Sun trigger
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapping a land of non-chosen color does not add extra mana")
    void noExtraManaOnNonMatchingLandTap() {
        Permanent cagedSunPerm = harness.addToBattlefieldAndReturn(player1, new CagedSun());
        cagedSunPerm.setChosenColor(CardColor.GREEN);

        harness.addToBattlefield(player1, new Mountain());

        // Mountain is at index 1 (Caged Sun at index 0)
        harness.tapPermanent(player1, 1);

        // Should get 1 red mana (no bonus)
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    @DisplayName("Opponent's land tap does not trigger extra mana")
    void noExtraManaForOpponentLandTap() {
        Permanent cagedSunPerm = harness.addToBattlefieldAndReturn(player1, new CagedSun());
        cagedSunPerm.setChosenColor(CardColor.GREEN);

        harness.addToBattlefield(player2, new Forest());

        // Opponent taps their Forest
        harness.tapPermanent(player2, 0);

        // Opponent should get 1 green mana only (no bonus from player1's Caged Sun)
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple Forests each get the extra mana")
    void extraManaOnMultipleLandTaps() {
        Permanent cagedSunPerm = harness.addToBattlefieldAndReturn(player1, new CagedSun());
        cagedSunPerm.setChosenColor(CardColor.GREEN);

        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        // Tap both Forests (indices 1 and 2, Caged Sun at 0)
        harness.tapPermanent(player1, 1);
        harness.tapPermanent(player1, 2);

        // Should get 4 green mana: (1+1) + (1+1)
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(4);
    }

    @Test
    @DisplayName("No extra mana if no color was chosen yet")
    void noExtraManaWithoutChosenColor() {
        // Add Caged Sun without chosen color
        harness.addToBattlefield(player1, new CagedSun());
        harness.addToBattlefield(player1, new Forest());

        // Forest is at index 1 (Caged Sun at index 0)
        harness.tapPermanent(player1, 1);

        // Should get only 1 green mana (no bonus)
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Full flow: cast, resolve, choose color, boost creature, get extra mana")
    void fullIntegrationTest() {
        Card greenCreature = createCreature("Green Bear", 2, 2, CardColor.GREEN);
        harness.addToBattlefield(player1, greenCreature);
        harness.addToBattlefield(player1, new Forest());

        harness.setHand(player1, List.of(new CagedSun()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        // Cast and resolve Caged Sun
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        // Choose green
        harness.handleListChoice(player1, "GREEN");

        // Verify creature is boosted
        Permanent bear = findPermanent(player1, "Green Bear");
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);

        harness.tapPermanent(player1, 1);

        // Should get 2 green mana: 1 from Forest + 1 from Caged Sun
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("An activated land ability producing the chosen color adds one extra mana")
    void activatedLandManaGetsBonus() {
        Permanent sun = harness.addToBattlefieldAndReturn(player1, new CagedSun());
        sun.setChosenColor(CardColor.GREEN);
        harness.addToBattlefield(player1, new WoodlandCemetery());

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing the matching color from a land's mana ability adds one extra mana")
    void chosenLandManaGetsBonus() {
        Permanent sun = harness.addToBattlefieldAndReturn(player1, new CagedSun());
        sun.setChosenColor(CardColor.GREEN);
        harness.addToBattlefield(player1, new ShimmeringGrotto());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A land's ability producing another color does not add chosen-color mana")
    void activatedLandManaOfOtherColorGetsNoBonus() {
        Permanent sun = harness.addToBattlefieldAndReturn(player1, new CagedSun());
        sun.setChosenColor(CardColor.GREEN);
        harness.addToBattlefield(player1, new WoodlandCemetery());

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Two Caged Suns each add one extra mana from the same land ability")
    void multipleSunsEachAddMana() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CagedSun());
        first.setChosenColor(CardColor.GREEN);
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CagedSun());
        second.setChosenColor(CardColor.GREEN);
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 2);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }
}
