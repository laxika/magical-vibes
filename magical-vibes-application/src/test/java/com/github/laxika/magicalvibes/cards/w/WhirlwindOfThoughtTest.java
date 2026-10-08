package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.UnholyStrength;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WhirlwindOfThought.class, GrizzlyBears.class, UnholyStrength.class})
class WhirlwindOfThoughtTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when the controller casts a noncreature spell")
    void drawsWhenControllerCastsNoncreatureSpell() {
        harness.addToBattlefield(player1, new WhirlwindOfThought());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new UnholyStrength()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0,
                findPermanent(player1, "Grizzly Bears").getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("Does not trigger for a creature spell")
    void doesNotTriggerForCreatureSpell() {
        harness.addToBattlefield(player1, new WhirlwindOfThought());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack)
                .noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && entry.getCard().getName().equals("Whirlwind of Thought"));
    }

    @Test
    void doesNotTriggerForOpponentsNoncreatureSpell() {
        harness.addToBattlefield(player1, new WhirlwindOfThought());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new UnholyStrength()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castEnchantment(player2, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void drawsForEachNoncreatureSpellBeforeThatSpellResolves() {
        harness.addToBattlefield(player1, new WhirlwindOfThought());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new UnholyStrength(), new UnholyStrength()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Unholy Strength");
        resolveAllTriggers();

        harness.castEnchantment(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName()).containsExactly("Grizzly Bears", "Grizzly Bears");
    }

    @Test
    void doesNotTriggerForItsOwnCast() {
        harness.setHand(player1, List.of(new WhirlwindOfThought()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Whirlwind of Thought");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
