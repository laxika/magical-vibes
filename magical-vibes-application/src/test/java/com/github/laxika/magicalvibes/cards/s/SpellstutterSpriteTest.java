package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpellstutterSpriteTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell whose mana value is within the Faerie count (X=1 counts itself)")
    void countersSpellWithinFaerieCount() {
        LlanowarElves elves = new LlanowarElves();
        SpellstutterSprite sprite = new SpellstutterSprite();
        harness.setHand(player1, List.of(elves, sprite));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        // Cast Llanowar Elves (mana value 1), then flash in Spellstutter Sprite in response.
        harness.castCreature(player1, 0);
        harness.castCreature(player1, 0);
        // Resolve the Sprite → it enters, and its ETB spell-target trigger offers the Elves
        // (X = 1, the Sprite counts itself).
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(elves.getId());

        harness.handlePermanentChosen(player1, elves.getId());
        // Resolve the ETB trigger → counter Llanowar Elves.
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(elves.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(elves.getId()));
    }

    @Test
    @DisplayName("Cannot target a spell whose mana value exceeds the Faerie count")
    void cannotTargetSpellAboveFaerieCount() {
        GrizzlyBears bears = new GrizzlyBears();
        SpellstutterSprite sprite = new SpellstutterSprite();
        harness.setHand(player1, List.of(bears, sprite));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        // Grizzly Bears has mana value 2; with only the Sprite as a Faerie (X = 1) it is not a legal target.
        harness.castCreature(player1, 0);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // No valid target → the ETB is skipped, nothing to choose.
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        // Grizzly Bears was not countered — it never reached a graveyard.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("A larger spell becomes counterable when more Faeries are controlled")
    void countersLargerSpellWithMoreFaeries() {
        // A second Faerie already on the battlefield raises X to 2.
        harness.addToBattlefield(player1, new SpellstutterSprite());

        GrizzlyBears bears = new GrizzlyBears();
        SpellstutterSprite sprite = new SpellstutterSprite();
        harness.setHand(player1, List.of(bears, sprite));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.castCreature(player1, 0);
        // Sprite enters → X = 2 (itself + the pre-placed Faerie), so Grizzly Bears (MV 2) is legal.
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(bears.getId());

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(bears.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(bears.getId()));
    }
}
