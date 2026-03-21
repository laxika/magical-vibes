package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FeelingOfDreadTest extends BaseCardTest {

    @Test
    @DisplayName("Taps two target creatures")
    void tapsTwoTargetCreatures() {
        Permanent creature1 = addReadyCreature(player2, new GrizzlyBears());
        Permanent creature2 = addReadyCreature(player2, new GiantSpider());

        harness.setHand(player1, List.of(new FeelingOfDread()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, List.of(creature1.getId(), creature2.getId()));
        harness.passBothPriorities();

        assertThat(creature1.isTapped()).isTrue();
        assertThat(creature2.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target just one creature")
    void canTargetJustOne() {
        Permanent creature = addReadyCreature(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new FeelingOfDread()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target more than two creatures")
    void cannotTargetMoreThanTwo() {
        Permanent c1 = addReadyCreature(player2, new GrizzlyBears());
        Permanent c2 = addReadyCreature(player2, new GiantSpider());
        Permanent c3 = addReadyCreature(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new FeelingOfDread()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(c1.getId(), c2.getId(), c3.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        addReadyCreature(player2, new GrizzlyBears()); // valid target so spell is playable
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new FeelingOfDread()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID fountainId = harness.getPermanentId(player2, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(fountainId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Goes to graveyard after resolving (normal cast)")
    void goesToGraveyardAfterResolving() {
        Permanent creature = addReadyCreature(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new FeelingOfDread()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Feeling of Dread"));
    }

    @Test
    @DisplayName("Skips targets that left the battlefield before resolution")
    void skipsRemovedTargets() {
        Permanent creature1 = addReadyCreature(player2, new GrizzlyBears());
        Permanent creature2 = addReadyCreature(player2, new GiantSpider());

        harness.setHand(player1, List.of(new FeelingOfDread()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, List.of(creature1.getId(), creature2.getId()));

        // Remove creature1 before resolution
        gd.playerBattlefields.get(player2.getId()).remove(creature1);

        harness.passBothPriorities();

        // creature2 should still be tapped
        assertThat(creature2.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Flashback from graveyard taps target creatures")
    void flashbackTapsTargetCreatures() {
        Permanent creature1 = addReadyCreature(player2, new GrizzlyBears());
        Permanent creature2 = addReadyCreature(player2, new GiantSpider());

        harness.setGraveyard(player1, List.of(new FeelingOfDread()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0, List.of(creature1.getId(), creature2.getId()));
        harness.passBothPriorities();

        assertThat(creature1.isTapped()).isTrue();
        assertThat(creature2.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Flashback exiles the spell after resolving")
    void flashbackExilesAfterResolving() {
        Permanent creature = addReadyCreature(player2, new GrizzlyBears());

        harness.setGraveyard(player1, List.of(new FeelingOfDread()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Should NOT be in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Feeling of Dread"));
        // Should be in exile
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Feeling of Dread"));
    }

    @Test
    @DisplayName("Flashback pays the flashback cost, not the mana cost")
    void flashbackPaysFlashbackCost() {
        Permanent creature = addReadyCreature(player2, new GrizzlyBears());

        harness.setGraveyard(player1, List.of(new FeelingOfDread()));
        // Only add blue mana (flashback cost is {1}{U}, not {1}{W})
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0, List.of(creature.getId()));

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot cast flashback without enough mana")
    void flashbackFailsWithoutMana() {
        Permanent creature = addReadyCreature(player2, new GrizzlyBears());

        harness.setGraveyard(player1, List.of(new FeelingOfDread()));
        // No mana added

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback removes card from graveyard when cast")
    void flashbackRemovesFromGraveyard() {
        Permanent creature = addReadyCreature(player2, new GrizzlyBears());

        harness.setGraveyard(player1, List.of(new FeelingOfDread()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0, List.of(creature.getId()));

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Feeling of Dread"));
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
