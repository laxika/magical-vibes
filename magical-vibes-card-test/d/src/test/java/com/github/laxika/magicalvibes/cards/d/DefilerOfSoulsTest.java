package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.h.HonorOfThePure;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DefilerOfSouls.class, LlanowarElves.class, Ornithopter.class,
        HonorOfThePure.class, Unsummon.class})
class DefilerOfSoulsTest extends BaseCardTest {

    @Test
    @DisplayName("At a player's upkeep that player sacrifices their monocolored creature")
    void monocoloredCreatureSacrificedAtThatPlayersUpkeep() {
        addCreatureReady(player1, new DefilerOfSouls());
        Permanent mono = addCreature(player2, "Green Bear", CardColor.GREEN, null);
        Permanent colorless = addCreature(player2, "Colorless Golem", null, null);

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve the trigger

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(mono.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(colorless.getId()));
    }

    @Test
    @DisplayName("A colorless creature is not sacrificed")
    void colorlessCreatureNotSacrificed() {
        addCreatureReady(player1, new DefilerOfSouls());
        Permanent colorless = addCreature(player2, "Colorless Golem", null, null);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(colorless.getId()));
    }

    @Test
    @DisplayName("A multicolored creature is not sacrificed")
    void multicoloredCreatureNotSacrificed() {
        addCreatureReady(player1, new DefilerOfSouls());
        Permanent gold = addCreature(player2, "Gold Hybrid", CardColor.BLACK, CardColor.RED);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(gold.getId()));
    }

    @Test
    @DisplayName("Triggers at each player's upkeep, including the controller's own")
    void controllerAlsoSacrificesAtOwnUpkeep() {
        addCreatureReady(player1, new DefilerOfSouls());
        Permanent mono = addCreature(player1, "Red Ogre", CardColor.RED, null);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(mono.getId()));
    }

    @Test
    @DisplayName("With multiple monocolored creatures the player chooses which to sacrifice")
    void choosesAmongMultipleMonocoloredCreatures() {
        addCreatureReady(player1, new DefilerOfSouls());
        Permanent green = addCreature(player2, "Green Bear", CardColor.GREEN, null);
        Permanent red = addCreature(player2, "Red Ogre", CardColor.RED, null);

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve the trigger -> prompts a choice

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultiplePermanentsChosen(player2, List.of(green.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(green.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(red.getId()));
    }

    @Test
    @DisplayName("Only the active player sacrifices, and only a monocolored creature qualifies")
    void onlyActivePlayersMonocoloredCreatureIsSacrificed() {
        addCreatureReady(player1, new DefilerOfSouls());
        addCreatureReady(player1, new LlanowarElves());
        addCreatureReady(player2, new LlanowarElves());
        addCreatureReady(player2, new Ornithopter());
        addCreatureReady(player2, new DefilerOfSouls());
        harness.addToBattlefield(player2, new HonorOfThePure());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.assertOnBattlefield(player2, "Ornithopter");
        harness.assertOnBattlefield(player2, "Defiler of Souls");
        harness.assertOnBattlefield(player2, "Honor of the Pure");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An upkeep without a qualifying creature does not sacrifice a colored noncreature")
    void noQualifyingCreatureDoesNotPromptOrSacrificeNoncreature() {
        addCreatureReady(player1, new DefilerOfSouls());
        addCreatureReady(player1, new LlanowarElves());
        addCreatureReady(player2, new Ornithopter());
        harness.addToBattlefield(player2, new HonorOfThePure());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertOnBattlefield(player2, "Ornithopter");
        harness.assertOnBattlefield(player2, "Honor of the Pure");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Qualifying creatures are determined when the upkeep trigger resolves")
    void creatureEnteringAfterTriggerIsSacrificed() {
        addCreatureReady(player1, new DefilerOfSouls());

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        addCreatureReady(player2, new LlanowarElves());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Removing Defiler in response does not stop its upkeep trigger")
    void triggerResolvesAfterSourceLeavesBattlefield() {
        Permanent defiler = addCreatureReady(player1, new DefilerOfSouls());
        addCreatureReady(player2, new LlanowarElves());
        harness.setHand(player2, List.of(new Unsummon()));
        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, defiler.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Defiler of Souls");
        harness.assertInHand(player1, "Defiler of Souls");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    /**
     * Adds a 2/2 creature. A single non-null {@code primary} color makes it monocolored; passing both
     * colors makes it multicolored; passing {@code null, null} makes it colorless.
     */
    private Permanent addCreature(Player player, String name, CardColor primary, CardColor secondary) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        if (primary != null && secondary != null) {
            card.setColors(List.of(primary, secondary));
        } else if (primary != null) {
            card.setColor(primary);
        }
        return addCreatureReady(player, card);
    }
}
