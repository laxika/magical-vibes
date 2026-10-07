package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.e.ElementalMasterpiece;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TeachByExample.class, LightningBolt.class, GrizzlyBears.class, ElementalMasterpiece.class})
class TeachByExampleTest extends BaseCardTest {

    @Test
    @DisplayName("sets up a copy of the next instant or sorcery cast this turn")
    void setsUpPendingCopy() {
        harness.setHand(player1, List.of(new TeachByExample()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount.get(player1.getId())).isEqualTo(1);
        harness.assertInGraveyard(player1, "Teach by Example");
    }

    @Test
    @DisplayName("copies only the next instant or sorcery spell")
    void copiesNextInstantOnly() {
        harness.setHand(player1, List.of(new TeachByExample()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0);

        GameData gd = harness.getGameData();
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("Copy Lightning Bolt"));
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount).doesNotContainKey(player1.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("does not copy a creature spell")
    void ignoresCreatureSpell() {
        harness.setHand(player1, List.of(new TeachByExample()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().startsWith("Copy "));
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void mayChooseNewTargetWithoutChangingOriginal() {
        harness.setHand(player1, List.of(new TeachByExample(), new LightningBolt()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copiesUntargetedSorceryWithoutPayingItsCostAgain() {
        harness.setHand(player1, List.of(new TeachByExample(), new ElementalMasterpiece()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castAndResolveInstant(player1, 0);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4);
        harness.assertInGraveyard(player1, "Elemental Masterpiece");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void secondInstantIsNotCopied() {
        harness.setHand(player1, List.of(new TeachByExample(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player2, 14);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 11);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsSpellDoesNotConsumeCopy() {
        harness.setHand(player1, List.of(new TeachByExample(), new LightningBolt()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0);

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertLife(player1, 17);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player2, 14);
    }

    @Test
    void unusedCopyExpiresAtEndOfTurn() {
        harness.setHand(player1, List.of(new TeachByExample()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 17);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copyingSecondTeachByExampleCreatesTwoDelayedCopies() {
        harness.setHand(player1, List.of(new TeachByExample(), new TeachByExample(), new LightningBolt()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castInstant(player1, 0, player2.getId());
        for (int i = 0; i < 2; i++) {
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);
            harness.passBothPriorities();
        }
        harness.passBothPriorities();

        harness.assertLife(player2, 11);
        assertThat(gd.stack).isEmpty();
    }
}
