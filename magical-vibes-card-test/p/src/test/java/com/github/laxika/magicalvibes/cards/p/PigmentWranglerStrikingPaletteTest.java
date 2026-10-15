package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.t.TomeBlast;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PigmentWranglerStrikingPalette.class, LightningBolt.class, TomeBlast.class})
class PigmentWranglerStrikingPaletteTest extends BaseCardTest {

    @Test
    @DisplayName("Pigment Wrangler enters prepared with a Striking Palette copy in exile")
    void entersPrepared() {
        Permanent wrangler = castPigmentWrangler();

        assertThat(wrangler.isPrepared()).isTrue();
        assertThat(wrangler.getPreparedSpellCardId()).isNotNull();
        assertThat(gd.findExiledCard(wrangler.getPreparedSpellCardId())).isNotNull();
    }

    @Test
    @DisplayName("Casting Striking Palette copies the next instant or sorcery this turn")
    void strikingPaletteCopiesNextInstantOrSorcery() {
        Permanent wrangler = castPigmentWrangler();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, wrangler.getPreparedSpellCardId());
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(wrangler.isPrepared()).isFalse();
        assertThat(gameData.pendingNextInstantSorceryCopyThisTurnCount.get(player1.getId())).isEqualTo(1);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gameData.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getDescription().contains("Copy Lightning Bolt"));
        assertThat(gameData.pendingNextInstantSorceryCopyThisTurnCount).doesNotContainKey(player1.getId());
    }

    @Test
    @DisplayName("Entering prepared does not create a triggered ability")
    void preparationHappensDuringEntryWithoutATrigger() {
        harness.castFromHand(player1, new PigmentWranglerStrikingPalette(), "{4}{R}");
        harness.passBothPriorities();

        Permanent wrangler = findPermanent(player1, "Pigment Wrangler");
        assertThat(wrangler.isPrepared()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Striking Palette copies a sorcery once and the copy resolves")
    void copiedSorceryResolvesAndSecondSpellIsNotCopied() {
        castStrikingPalette();
        harness.setHand(player1, List.of(new TomeBlast(), new TomeBlast()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof TomeBlast).hasSize(1);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.assertLife(player2, 14);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The copy can choose a new target while the original retains its target")
    void copiedSorceryCanChooseNewTarget() {
        Permanent wrangler = castStrikingPalette();
        harness.setHand(player1, List.of(new TomeBlast()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, wrangler.getId());
        harness.passBothPriorities();

        assertThat(wrangler.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A creature spell does not consume Striking Palette's delayed copy")
    void creatureSpellDoesNotConsumeCopy() {
        castStrikingPalette();
        castPigmentWrangler();
        harness.setHand(player1, List.of(new TomeBlast()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's instant does not consume Striking Palette's delayed copy")
    void opponentsSpellDoesNotConsumeCopy() {
        castStrikingPalette();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertLife(player1, 17);

        harness.setHand(player1, List.of(new TomeBlast()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Striking Palette's unused delayed copy expires at the end of the turn")
    void unusedCopyExpiresAtEndOfTurn() {
        castStrikingPalette();
        harness.setLibrary(player1, List.of(new PigmentWranglerStrikingPalette()));
        harness.setLibrary(player2, List.of(new PigmentWranglerStrikingPalette()));
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new TomeBlast()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent castStrikingPalette() {
        Permanent wrangler = castPigmentWrangler();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, wrangler.getPreparedSpellCardId());
        assertThat(wrangler.isPrepared()).isFalse();
        harness.passBothPriorities();
        return wrangler;
    }

    private Permanent castPigmentWrangler() {
        harness.castFromHand(player1, new PigmentWranglerStrikingPalette(), "{4}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        return findPermanent(player1, "Pigment Wrangler");
    }
}
