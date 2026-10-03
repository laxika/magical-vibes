package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.ProvokeTheTrolls;
import com.github.laxika.magicalvibes.cards.s.StarnheimUnleashed;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DualStrike.class, LightningBolt.class, LavaAxe.class, CounselOfTheSoratami.class,
        ProvokeTheTrolls.class, StarnheimUnleashed.class})
class DualStrikeTest extends BaseCardTest {

    @Test
    @DisplayName("Copies the next instant with mana value 4 or less")
    void copiesQualifyingSpell() {
        castDualStrike();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getDescription().contains("Copy Lightning Bolt"));

        resolveCopyAndOriginal();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Waits for a qualifying spell after an instant or sorcery above the mana-value limit")
    void ignoresSpellAboveManaValueLimit() {
        castDualStrike();

        harness.setHand(player1, List.of(new LavaAxe()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castSorcery(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getDescription().contains("Copy Lava Axe"));
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getDescription().contains("Copy Lightning Bolt"));

        resolveCopyAndOriginal();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(9);
    }

    @Test
    @DisplayName("Foretell exiles Dual Strike face down and allows it to be cast on a later turn")
    void foretellsAndCasts() {
        DualStrike spell = new DualStrike();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = harness.getGameData().findExiledCard(spell.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();

        harness.getGameData().turnNumber++;
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);

        assertThat(harness.getGameData().stack).anyMatch(stackEntry ->
                stackEntry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && stackEntry.getDescription().contains("Copy Counsel of the Soratami"));
    }

    private void castDualStrike() {
        harness.setHand(player1, List.of(new DualStrike()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0);
    }

    private void resolveCopyAndOriginal() {
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
    }

    @Test
    void copiesManaValueFourAndMayChangeTheCopyTarget() {
        castDualStrike();
        harness.setHand(player1, List.of(new ProvokeTheTrolls()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
    }

    @Test
    void copiesOnlyTheNextQualifyingSpell() {
        castDualStrike();
        harness.setHand(player1, List.of(new ProvokeTheTrolls()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castInstant(player1, 0, player2.getId());
        resolveCopyAndOriginal();
        harness.assertLife(player2, 14);

        harness.setHand(player1, List.of(new ProvokeTheTrolls()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.assertLife(player2, 11);
    }

    @Test
    void opponentsSpellDoesNotConsumeTheDelayedCopy() {
        castDualStrike();
        harness.setHand(player2, List.of(new ProvokeTheTrolls()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        resolveAllTriggers();
        harness.assertLife(player1, 17);

        harness.setHand(player1, List.of(new ProvokeTheTrolls()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castInstant(player1, 0, player2.getId());
        resolveCopyAndOriginal();
        harness.assertLife(player2, 14);
    }

    @Test
    void copiesAnUntargetedSorcery() {
        castDualStrike();
        harness.setHand(player1, List.of(new StarnheimUnleashed()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Angel Warrior")).isEqualTo(2);
    }

    @Test
    void unusedDelayedCopyExpiresAtEndOfTurn() {
        castDualStrike();
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new ProvokeTheTrolls()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.assertLife(player2, 17);
    }

    @Test
    void copyPreservesForetellCostAndXDespitePayingMoreThanFourMana() {
        StarnheimUnleashed spell = new StarnheimUnleashed();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        gd.turnNumber++;
        castDualStrike();

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.ensurePriority(player1);
        gs.playCardFromExile(gd, player1, spell.getId(), 2, null);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Angel Warrior")).isEqualTo(4);
    }

    @Test
    void cannotCastDualStrikeOnTheTurnItWasForetold() {
        DualStrike spell = new DualStrike();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
    }
}
