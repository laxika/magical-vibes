package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

    // ===== Helpers =====

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
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
