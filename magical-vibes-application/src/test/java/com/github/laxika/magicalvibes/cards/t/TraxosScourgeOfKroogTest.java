package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.ShortSword;
import com.github.laxika.magicalvibes.cards.d.DanithaCapashenParagon;
import com.github.laxika.magicalvibes.cards.h.HistoryOfBenalia;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TraxosScourgeOfKroog.class, ShortSword.class, LlanowarElves.class,
        DanithaCapashenParagon.class, HistoryOfBenalia.class})
class TraxosScourgeOfKroogTest extends BaseCardTest {

    @Test
    @DisplayName("Traxos enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new TraxosScourgeOfKroog()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent traxos = findPermanent(player1, "Traxos, Scourge of Kroog");

        assertThat(traxos.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapped Traxos does not untap during controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent traxosPerm = addTraxosReady(player1);
        traxosPerm.tap();

        harness.performUntapStep(player1);

        assertThat(traxosPerm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting an artifact triggers untap Traxos")
    void artifactSpellTriggersUntap() {
        Permanent traxosPerm = addTraxosReady(player1);
        traxosPerm.tap();
        assertThat(traxosPerm.isTapped()).isTrue();

        harness.setHand(player1, List.of(new ShortSword()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);

        GameData gd = harness.getGameData();
        // Short Sword on stack + triggered ability
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Traxos, Scourge of Kroog"));
    }

    @Test
    @DisplayName("Resolving artifact-triggered ability untaps Traxos")
    void artifactTriggerUntapsTraxos() {
        Permanent traxosPerm = addTraxosReady(player1);
        traxosPerm.tap();
        assertThat(traxosPerm.isTapped()).isTrue();

        harness.setHand(player1, List.of(new ShortSword()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        // Resolve the triggered ability (LIFO â€” trigger on top)
        harness.passBothPriorities();

        assertThat(traxosPerm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Casting a non-historic creature does not trigger untap")
    void nonHistoricDoesNotTrigger() {
        addTraxosReady(player1);

        harness.setHand(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        // Only the creature spell on the stack, no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Opponent casting an artifact does not trigger controller's Traxos")
    void opponentHistoricDoesNotTrigger() {
        addTraxosReady(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new ShortSword()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castArtifact(player2, 0);

        GameData gd = harness.getGameData();
        // Only the artifact spell on stack, no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }

    @Test
    void legendaryNonArtifactSpellUntapsTraxosBeforeSpellResolves() {
        Permanent traxos = addTraxosReady(player1);
        traxos.tap();
        harness.setHand(player1, List.of(new DanithaCapashenParagon()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(traxos.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(traxos.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Danitha Capashen, Paragon");
    }

    @Test
    void nonLegendarySagaSpellUntapsTraxosBeforeSpellResolves() {
        Permanent traxos = addTraxosReady(player1);
        traxos.tap();
        harness.setHand(player1, List.of(new HistoryOfBenalia()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(traxos.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(traxos.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "History of Benalia");
    }

    @Test
    void castingTraxosDoesNotTriggerItsOwnUntapAbility() {
        harness.setHand(player1, List.of(new TraxosScourgeOfKroog()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Traxos, Scourge of Kroog").isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void spellThatIsBothArtifactAndLegendaryTriggersOnlyOnce() {
        Permanent traxos = addTraxosReady(player1);
        traxos.tap();
        harness.setHand(player1, List.of(new TraxosScourgeOfKroog()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(traxos.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void historicPermanentEnteringWithoutBeingCastDoesNotUntapTraxos() {
        Permanent traxos = addTraxosReady(player1);
        traxos.tap();

        harness.enterBattlefieldAndReturn(player1, new ShortSword());

        assertThat(traxos.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Short Sword");
    }

    private Permanent addTraxosReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new TraxosScourgeOfKroog());
        perm.setSummoningSick(false);
        return perm;
    }

}
