package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RunForYourLife.class, GrizzlyBears.class, RagingGoblin.class})
class RunForYourLifeTest extends BaseCardTest {

    @Test
    @DisplayName("One target gains haste and can only be blocked by creatures with haste")
    void oneTargetGainsHasteAndHasteOnlyEvasion() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        castOn(attacker);

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.HASTE)).isTrue();

        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("haste");
    }

    @Test
    @DisplayName("A creature with haste can block")
    void creatureWithHasteCanBlock() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        castOn(attacker);

        Permanent blocker = addCreatureReady(player2, new RagingGoblin());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Both targets receive haste and the blocking restriction")
    void twoTargetsReceiveBothEffects() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        castOn(first, second);

        assertThat(gqs.hasKeyword(gd, first, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.HASTE)).isTrue();

        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0, 1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("haste");
    }

    @Test
    @DisplayName("Escape exiles four other graveyard cards")
    void escapeExilesFourOtherCards() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        RunForYourLife spell = new RunForYourLife();
        List<Card> graveyard = new ArrayList<>(List.of(
                spell, new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setGraveyard(player1, graveyard);
        addEscapeMana();

        gs.playFlashbackSpell(gd, player1, 0, null, null, List.of(target.getId()), List.of(1, 2, 3, 4));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(
                graveyard.get(1), graveyard.get(2), graveyard.get(3), graveyard.get(4));

        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    private void castOn(Permanent... targets) {
        harness.setHand(player1, List.of(new RunForYourLife()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, Arrays.stream(targets).map(Permanent::getId).toList());
    }

    @Test
    @DisplayName("Escape returns the resolved spell to the graveyard")
    void escapedSpellReturnsToGraveyard() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        RunForYourLife spell = prepareEscape();

        gs.playFlashbackSpell(gd, player1, 0, null, null, List.of(target.getId()), List.of(1, 2, 3, 4));
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(spell);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("An escaped spell with no remaining legal target returns to the graveyard")
    void escapedSpellWithRemovedTargetReturnsToGraveyard() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        RunForYourLife spell = prepareEscape();

        gs.playFlashbackSpell(gd, player1, 0, null, null, List.of(target.getId()), List.of(1, 2, 3, 4));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, target));
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(spell);
    }

    @Test
    @DisplayName("Escape cannot exile the spell itself as one of the four other cards")
    void escapeCannotExileItself() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        RunForYourLife spell = prepareEscape();

        assertThatThrownBy(() -> gs.playFlashbackSpell(gd, player1, 0, null, null,
                List.of(target.getId()), List.of(0, 1, 2, 3)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5).contains(spell);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Escape requires four distinct other cards")
    void escapeCannotPayWithOnlyThreeCards() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        RunForYourLife spell = prepareEscape();

        assertThatThrownBy(() -> gs.playFlashbackSpell(gd, player1, 0, null, null,
                List.of(target.getId()), List.of(1, 2, 3)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5).contains(spell);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The remaining target receives both effects when the other target leaves")
    void remainingTargetReceivesBothEffects() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RunForYourLife()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, first));
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);

        assertThat(gqs.hasKeyword(gd, second, Keyword.HASTE)).isTrue();
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("haste");
    }

    @Test
    @DisplayName("Haste and the blocking restriction expire at end of turn")
    void bothEffectsExpireAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        castOn(attacker);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.HASTE)).isFalse();
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(blocker.isBlocking()).isTrue();
    }

    private RunForYourLife prepareEscape() {
        RunForYourLife spell = new RunForYourLife();
        harness.setGraveyard(player1, List.of(spell,
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        addEscapeMana();
        return spell;
    }

    @Test
    @DisplayName("Granted haste lets a newly controlled creature attack")
    void grantedHasteAllowsSummoningSickCreatureToAttack() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(true);
        castOn(attacker);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(attacker.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Creatures controlled by an opponent can also be targeted")
    void opponentCreatureReceivesBothEffects() {
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        castOn(attacker);

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.HASTE)).isTrue();
        addCreatureReady(player1, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(player2, List.of(0));
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("haste");
    }

    private void addEscapeMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
