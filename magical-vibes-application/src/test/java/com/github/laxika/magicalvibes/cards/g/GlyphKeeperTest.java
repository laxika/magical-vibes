package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GlyphKeeperTest extends BaseCardTest {

    private UUID addGlyphKeeper() {
        harness.addToBattlefield(player1, new GlyphKeeper());
        Permanent gk = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Glyph Keeper"))
                .findFirst().orElseThrow();
        gk.setSummoningSick(false);
        return gk.getId();
    }

    // ===== Counter trigger =====

    @Test
    @DisplayName("Counters the first spell that targets it each turn")
    void countersFirstSpellEachTurn() {
        UUID gkId = addGlyphKeeper();

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, gkId);

        // Lightning Bolt plus Glyph Keeper's counter trigger on top.
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        harness.passBothPriorities(); // resolve the counter trigger

        // The bolt was countered — Glyph Keeper survives and the bolt is in its owner's graveyard.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Glyph Keeper"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Lightning Bolt"));
    }

    @Test
    @DisplayName("Counters an activated ability that targets it")
    void countersTargetingAbility() {
        UUID gkId = addGlyphKeeper();

        harness.addToBattlefield(player2, new IcyManipulator());
        Permanent icy = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Icy Manipulator"))
                .findFirst().orElseThrow();
        icy.setSummoningSick(false);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(icy), null, gkId);

        // Icy Manipulator's ability plus Glyph Keeper's counter trigger on top.
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        harness.passBothPriorities(); // resolve the counter trigger

        // The ability was countered — Glyph Keeper is not tapped.
        Permanent gk = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(gkId)).findFirst().orElseThrow();
        assertThat(gk.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Counters its own controller's spell (no controller restriction)")
    void countersControllersOwnSpell() {
        UUID gkId = addGlyphKeeper();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, gkId);

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        harness.passBothPriorities(); // resolve the counter trigger

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Glyph Keeper"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Lightning Bolt"));
    }

    @Test
    @DisplayName("A second spell the same turn is not countered")
    void secondSpellSameTurnNotCountered() {
        UUID gkId = addGlyphKeeper();

        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);

        // First bolt is countered.
        harness.castInstant(player2, 0, gkId);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Glyph Keeper"));

        // Second bolt the same turn: the trigger does not fire again — only the bolt is on the stack.
        harness.castInstant(player2, 0, gkId);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Lightning Bolt");

        harness.passBothPriorities(); // resolve the bolt — 3 damage is lethal to the 5/3

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Glyph Keeper"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Glyph Keeper"));
    }

    // ===== Embalm =====

    @Test
    @DisplayName("Embalm creates a white Zombie token copy with no mana cost")
    void embalmCreatesWhiteZombieTokenCopy() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new GlyphKeeper()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities(); // resolve the Embalm ability

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Glyph Keeper") && p.getCard().isToken())
                .findFirst().orElseThrow();

        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(token.getCard().getManaCost()).isEmpty();
    }
}
