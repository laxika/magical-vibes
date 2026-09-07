package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.k.KjeldoranKnight;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CallToArms.class, BalduvianBears.class, KjeldoranKnight.class, ZuranOrb.class})
class CallToArmsTest extends BaseCardTest {

    private static Card createPermanent(String name, CardType type, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setManaCost("{1}");
        card.setColor(color);
        return card;
    }

    private static Card createCreature(String name, int power, int toughness, CardColor color) {
        Card card = createPermanent(name, CardType.CREATURE, color);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    private static Card createMulticolorCreature(String name, int power, int toughness, List<CardColor> colors) {
        Card card = createCreature(name, power, toughness, colors.getFirst());
        card.setColors(colors);
        return card;
    }

    private Permanent addCallToArms(CardColor chosen) {
        Permanent perm = new Permanent(new CallToArms());
        perm.setChosenColor(chosen);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    @Test
    @DisplayName("White creatures get +1/+1 while chosen color is strictly most common among opponent nontokens")
    void buffsWhiteCreaturesWhileConditionMet() {
        // Two reds so a white creature on the opponent does not tie the chosen color.
        harness.addToBattlefield(player2, createCreature("Red Goblin", 1, 1, CardColor.RED));
        harness.addToBattlefield(player2, createCreature("Red Knight", 1, 1, CardColor.RED));
        harness.addToBattlefield(player1, new KjeldoranKnight());
        harness.addToBattlefield(player2, new KjeldoranKnight());
        addCallToArms(CardColor.RED);

        Permanent own = findPermanent(player1, "Kjeldoran Knight");
        Permanent opp = findPermanent(player2, "Kjeldoran Knight");
        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, own)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opp)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opp)).isEqualTo(2);
    }

    @Test
    @DisplayName("Nonwhite creatures are unaffected")
    void doesNotBuffNonwhite() {
        harness.addToBattlefield(player2, createCreature("Red Goblin", 1, 1, CardColor.RED));
        harness.addToBattlefield(player1, new BalduvianBears());
        addCallToArms(CardColor.RED);

        Permanent bears = findPermanent(player1, "Balduvian Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Colored nontoken noncreature permanents count toward the majority")
    void noncreaturePermanentsCount() {
        harness.addToBattlefield(player2,
                createPermanent("Red Enchantment", CardType.ENCHANTMENT, CardColor.RED));
        harness.addToBattlefield(player1, new KjeldoranKnight());
        addCallToArms(CardColor.RED);

        Permanent knight = findPermanent(player1, "Kjeldoran Knight");
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
    }

    @Test
    @DisplayName("Colorless nontoken permanents do not count toward the majority")
    void colorlessPermanentsDoNotCount() {
        harness.addToBattlefield(player2, createCreature("Red Goblin", 1, 1, CardColor.RED));
        harness.addToBattlefield(player2, new ZuranOrb());
        harness.addToBattlefield(player1, new KjeldoranKnight());
        addCallToArms(CardColor.RED);

        Permanent knight = findPermanent(player1, "Kjeldoran Knight");
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
    }

    @Test
    @DisplayName("No boost when chosen color is tied for most common")
    void noBoostWhenTied() {
        harness.addToBattlefield(player2, createCreature("Red Goblin", 1, 1, CardColor.RED));
        harness.addToBattlefield(player2, createCreature("Blue Merfolk", 1, 1, CardColor.BLUE));
        harness.addToBattlefield(player1, new KjeldoranKnight());
        addCallToArms(CardColor.RED);

        Permanent vanguard = findPermanent(player1, "Kjeldoran Knight");
        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(1);
    }

    @Test
    @DisplayName("Token permanents do not count toward color majority")
    void tokensDoNotCount() {
        Card token = createCreature("Red Token", 1, 1, CardColor.RED);
        token.setToken(true);
        harness.addToBattlefield(player2, token);
        harness.addToBattlefield(player2, createCreature("Blue Merfolk", 1, 1, CardColor.BLUE));
        harness.addToBattlefield(player1, new KjeldoranKnight());
        // Only nontoken is blue → choosing red fails; choosing blue succeeds.
        addCallToArms(CardColor.BLUE);

        Permanent vanguard = findPermanent(player1, "Kjeldoran Knight");
        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multicolored permanents count for each of their colors")
    void multicolorCountsForEachColor() {
        harness.addToBattlefield(player2,
                createMulticolorCreature("Boros Scout", 1, 1, List.of(CardColor.RED, CardColor.WHITE)));
        harness.addToBattlefield(player1, new KjeldoranKnight());
        // One RGW-style permanent: red=1, white=1 → tied → no boost for either choice.
        addCallToArms(CardColor.RED);

        Permanent vanguard = findPermanent(player1, "Kjeldoran Knight");
        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting with chosen color strictly most common keeps Call to Arms and applies boost")
    void castAndChooseSurvivesWhenStrictlyMostCommon() {
        harness.addToBattlefield(player2, createCreature("Red Goblin", 1, 1, CardColor.RED));
        harness.addToBattlefield(player1, new KjeldoranKnight());
        harness.castFromHand(player1, new CallToArms(), "{1}{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        harness.assertOnBattlefield(player1, "Call to Arms");
        Permanent vanguard = findPermanent(player1, "Kjeldoran Knight");
        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrificed when chosen color is not strictly most common among opponent nontokens")
    void sacrificedWhenConditionFails() {
        harness.addToBattlefield(player2, createCreature("Blue Merfolk", 1, 1, CardColor.BLUE));
        harness.castFromHand(player1, new CallToArms(), "{1}{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Call to Arms");
        harness.assertInGraveyard(player1, "Call to Arms");
    }

    @Test
    @DisplayName("Sacrificed once another color catches up to the chosen color")
    void sacrificedWhenAnotherColorCatchesUp() {
        harness.addToBattlefield(player2, createCreature("Red Goblin", 1, 1, CardColor.RED));
        harness.castFromHand(player1, new CallToArms(), "{1}{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");
        harness.assertOnBattlefield(player1, "Call to Arms");

        harness.addToBattlefield(player2, createCreature("Blue Merfolk", 1, 1, CardColor.BLUE));
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Call to Arms");
        harness.assertInGraveyard(player1, "Call to Arms");
    }

    @Test
    @DisplayName("Sacrifices even if the majority is restored before the state trigger resolves")
    void sacrificesAfterStateTriggerEvenWhenConditionIsRestored() {
        harness.addToBattlefield(player2, createCreature("Red Goblin", 1, 1, CardColor.RED));
        harness.castFromHand(player1, new CallToArms(), "{1}{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");
        harness.assertOnBattlefield(player1, "Call to Arms");

        Permanent blue = harness.addToBattlefieldAndReturn(
                player2, createCreature("Blue Merfolk", 1, 1, CardColor.BLUE));
        harness.runStateBasedActions();
        gd.playerBattlefields.get(player2.getId()).remove(blue);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Call to Arms");
        harness.assertInGraveyard(player1, "Call to Arms");
    }
}
