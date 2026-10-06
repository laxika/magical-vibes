package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.m.MaddeningCacophony;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeaGateStormcaller.class, LightningBolt.class, LavaAxe.class, MaddeningCacophony.class})
class SeaGateStormcallerTest extends BaseCardTest {

    @Test
    void copiesNextLowManaValueInstantOnceWithoutKicker() {
        castStormcaller(false);

        castLightningBolt();
        resolveCopies(1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    void copiesNextLowManaValueInstantTwiceWhenKicked() {
        castStormcaller(true);

        castLightningBolt();
        resolveCopies(2);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(11);
    }

    @Test
    void waitsForTheNextInstantOrSorceryWithManaValueTwoOrLess() {
        castStormcaller(false);

        harness.setHand(player1, List.of(new LavaAxe()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        castLightningBolt();
        resolveCopies(1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(9);
    }

    @Test
    void kickedStormcallerCreatesBothCopiesInOneDelayedTrigger() {
        castStormcaller(true);
        harness.setHand(player1, List.of(new MaddeningCacophony()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0);

        assertThat(gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY))
                .hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(3);
        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
    }

    @Test
    void copiesTargetlessSorceryAtManaValueTwoOnlyOnce() {
        castStormcaller(false);
        harness.setLibrary(player2, IntStream.range(0, 30)
                .mapToObj(i -> new SeaGateStormcaller()).toList());
        harness.setHand(player1, List.of(new MaddeningCacophony(), new MaddeningCacophony()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();
        assertThat(gd.playerLibraries.get(player2.getId())).hasSize(14);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();
        assertThat(gd.playerLibraries.get(player2.getId())).hasSize(6);
    }

    @Test
    void copyMayChooseANewTargetWithoutChangingTheOriginal() {
        castStormcaller(false);
        castLightningBolt();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
    }

    @Test
    void opponentSpellDoesNotConsumeTheDelayedTrigger() {
        castStormcaller(false);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertLife(player1, 17);

        castLightningBolt();
        resolveCopies(1);
        harness.assertLife(player2, 14);
    }

    @Test
    void delayedTriggerStillCopiesAfterStormcallerDies() {
        castStormcaller(false);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Sea Gate Stormcaller"));
        harness.assertNotOnBattlefield(player1, "Sea Gate Stormcaller");

        castLightningBolt();
        resolveCopies(1);
        harness.assertLife(player2, 14);
    }

    @Test
    void additionalKickerCostDoesNotDisqualifySpellAndCopyRetainsKicker() {
        castStormcaller(false);
        harness.setLibrary(player2, IntStream.range(0, 32)
                .mapToObj(i -> new SeaGateStormcaller()).toList());
        harness.setHand(player1, List.of(new MaddeningCacophony()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castKickedSorcery(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLibraries.get(player2.getId())).hasSize(8);
    }

    @Test
    void unusedDelayedTriggerExpiresAtEndOfTurn() {
        castStormcaller(false);
        harness.setLibrary(player2, List.of(new SeaGateStormcaller(), new SeaGateStormcaller()));
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        castLightningBolt();
        resolveAllTriggers();

        harness.assertLife(player2, 17);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void spellCastBeforeTheEnterAbilityResolvesIsNotCopied() {
        harness.setHand(player1, List.of(new SeaGateStormcaller()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        castLightningBolt();
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
        resolveAllTriggers();

        castLightningBolt();
        resolveCopies(1);
        harness.assertLife(player2, 11);
    }

    private void castStormcaller(boolean kicked) {
        harness.setHand(player1, List.of(new SeaGateStormcaller()));
        harness.addMana(player1, ManaColor.BLUE, kicked ? 2 : 1);
        harness.addMana(player1, ManaColor.COLORLESS, kicked ? 5 : 1);

        if (kicked) {
            harness.castKickedCreature(player1, 0);
        } else {
            harness.castCreature(player1, 0);
        }
        resolveAllTriggers();
    }

    private void castLightningBolt() {
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
    }

    private void resolveCopies(int copyCount) {
        for (int i = 0; i < copyCount; i++) {
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, false);
        }
        resolveAllTriggers();
    }
}
