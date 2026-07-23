package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CallToArmsTest extends BaseCardTest {

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

    private Permanent find(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }

    private boolean controlsCallToArms(Player owner) {
        return gd.playerBattlefields.get(owner.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals("Call to Arms"));
    }

    @Test
    @DisplayName("White creatures get +1/+1 while chosen color is strictly most common among opponent nontokens")
    void buffsWhiteCreaturesWhileConditionMet() {
        // Two reds so a white creature on the opponent does not tie the chosen color.
        harness.addToBattlefield(player2, createCreature("Red Goblin", 1, 1, CardColor.RED));
        harness.addToBattlefield(player2, createCreature("Red Knight", 1, 1, CardColor.RED));
        harness.addToBattlefield(player1, new EliteVanguard());
        harness.addToBattlefield(player2, new EliteVanguard());
        addCallToArms(CardColor.RED);

        Permanent own = find(player1, "Elite Vanguard");
        Permanent opp = find(player2, "Elite Vanguard");
        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, own)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opp)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opp)).isEqualTo(2);
    }

    @Test
    @DisplayName("Nonwhite creatures are unaffected")
    void doesNotBuffNonwhite() {
        harness.addToBattlefield(player2, createCreature("Red Goblin", 1, 1, CardColor.RED));
        harness.addToBattlefield(player1, new GrizzlyBears());
        addCallToArms(CardColor.RED);

        Permanent bears = find(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("No boost when chosen color is tied for most common")
    void noBoostWhenTied() {
        harness.addToBattlefield(player2, createCreature("Red Goblin", 1, 1, CardColor.RED));
        harness.addToBattlefield(player2, createCreature("Blue Merfolk", 1, 1, CardColor.BLUE));
        harness.addToBattlefield(player1, new EliteVanguard());
        addCallToArms(CardColor.RED);

        Permanent vanguard = find(player1, "Elite Vanguard");
        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(1);
    }

    @Test
    @DisplayName("Token permanents do not count toward color majority")
    void tokensDoNotCount() {
        Card token = createCreature("Red Token", 1, 1, CardColor.RED);
        token.setToken(true);
        harness.addToBattlefield(player2, token);
        harness.addToBattlefield(player2, createCreature("Blue Merfolk", 1, 1, CardColor.BLUE));
        harness.addToBattlefield(player1, new EliteVanguard());
        // Only nontoken is blue → choosing red fails; choosing blue succeeds.
        addCallToArms(CardColor.BLUE);

        Permanent vanguard = find(player1, "Elite Vanguard");
        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(3);
    }

    @Test
    @DisplayName("Multicolored permanents count for each of their colors")
    void multicolorCountsForEachColor() {
        harness.addToBattlefield(player2,
                createMulticolorCreature("Boros Scout", 1, 1, List.of(CardColor.RED, CardColor.WHITE)));
        harness.addToBattlefield(player1, new EliteVanguard());
        // One RGW-style permanent: red=1, white=1 → tied → no boost for either choice.
        addCallToArms(CardColor.RED);

        Permanent vanguard = find(player1, "Elite Vanguard");
        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting with chosen color strictly most common keeps Call to Arms and applies boost")
    void castAndChooseSurvivesWhenStrictlyMostCommon() {
        harness.addToBattlefield(player2, createCreature("Red Goblin", 1, 1, CardColor.RED));
        harness.addToBattlefield(player1, new EliteVanguard());
        harness.setHand(player1, List.of(new CallToArms()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThat(controlsCallToArms(player1)).isTrue();
        Permanent vanguard = find(player1, "Elite Vanguard");
        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(3);
    }

    @Test
    @DisplayName("Sacrificed when chosen color is not strictly most common among opponent nontokens")
    void sacrificedWhenConditionFails() {
        harness.addToBattlefield(player2, createCreature("Blue Merfolk", 1, 1, CardColor.BLUE));
        harness.setHand(player1, List.of(new CallToArms()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");
        harness.passBothPriorities();

        assertThat(controlsCallToArms(player1)).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Call to Arms"));
    }

    @Test
    @DisplayName("Sacrificed once another color catches up to the chosen color")
    void sacrificedWhenAnotherColorCatchesUp() {
        harness.addToBattlefield(player2, createCreature("Red Goblin", 1, 1, CardColor.RED));
        harness.setHand(player1, List.of(new CallToArms()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");
        assertThat(controlsCallToArms(player1)).isTrue();

        harness.addToBattlefield(player2, createCreature("Blue Merfolk", 1, 1, CardColor.BLUE));
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(controlsCallToArms(player1)).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Call to Arms"));
    }
}
