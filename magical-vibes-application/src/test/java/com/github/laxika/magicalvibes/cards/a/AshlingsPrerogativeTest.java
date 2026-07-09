package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaValueParity;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// GrizzlyBears = {1}{G} (mana value 2, even); LlanowarElves = {G} (mana value 1, odd).
class AshlingsPrerogativeTest extends BaseCardTest {

    private Permanent addPrerogativeWithParity(com.github.laxika.magicalvibes.model.Player owner, ManaValueParity parity) {
        harness.addToBattlefield(owner, new AshlingsPrerogative());
        Permanent perm = gd.playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Ashling's Prerogative"))
                .findFirst().orElseThrow();
        perm.setChosenManaValueParity(parity);
        return perm;
    }

    private Permanent find(com.github.laxika.magicalvibes.model.Player owner, String name) {
        return gd.playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }

    // ===== Enter-time odd/even choice =====

    @Test
    @DisplayName("Resolving awaits an odd/even choice")
    void resolvingAwaitsParityChoice() {
        harness.setHand(player1, List.of(new AshlingsPrerogative()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Choosing odd stores the parity on the permanent")
    void choosingSetsParity() {
        harness.setHand(player1, List.of(new AshlingsPrerogative()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ODD");

        assertThat(find(player1, "Ashling's Prerogative").getChosenManaValueParity())
                .isEqualTo(ManaValueParity.ODD);
    }

    // ===== Static: haste to creatures of the chosen quality (any controller) =====

    @Test
    @DisplayName("Even chosen: even-MV creatures have haste, odd-MV do not")
    void evenGrantsHasteToEvenCreatures() {
        addPrerogativeWithParity(player1, ManaValueParity.EVEN);
        harness.addToBattlefield(player1, new GrizzlyBears());  // MV 2, even
        harness.addToBattlefield(player1, new LlanowarElves()); // MV 1, odd

        assertThat(gqs.hasKeyword(gd, find(player1, "Grizzly Bears"), Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, find(player1, "Llanowar Elves"), Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Haste is granted to opponents' matching creatures too")
    void hasteAffectsOpponentCreatures() {
        addPrerogativeWithParity(player1, ManaValueParity.EVEN);
        harness.addToBattlefield(player2, new GrizzlyBears()); // MV 2, even

        assertThat(gqs.hasKeyword(gd, find(player2, "Grizzly Bears"), Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Odd chosen: odd-MV creatures have haste, even-MV do not")
    void oddGrantsHasteToOddCreatures() {
        addPrerogativeWithParity(player1, ManaValueParity.ODD);
        harness.addToBattlefield(player1, new GrizzlyBears());  // MV 2, even
        harness.addToBattlefield(player1, new LlanowarElves()); // MV 1, odd

        assertThat(gqs.hasKeyword(gd, find(player1, "Llanowar Elves"), Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, find(player1, "Grizzly Bears"), Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("No haste granted before a quality is chosen")
    void noHasteBeforeChoice() {
        harness.addToBattlefield(player1, new AshlingsPrerogative()); // parity unchosen (null)
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, find(player1, "Grizzly Bears"), Keyword.HASTE)).isFalse();
    }

    // ===== Replacement: creatures without the chosen quality enter tapped =====

    @Test
    @DisplayName("Even chosen: an odd-MV creature enters tapped")
    void unmatchedParityCreatureEntersTapped() {
        addPrerogativeWithParity(player1, ManaValueParity.EVEN);
        harness.setHand(player1, List.of(new LlanowarElves())); // MV 1, odd
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(find(player1, "Llanowar Elves").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Even chosen: an even-MV creature enters untapped")
    void matchedParityCreatureEntersUntapped() {
        addPrerogativeWithParity(player1, ManaValueParity.EVEN);
        harness.setHand(player1, List.of(new GrizzlyBears())); // MV 2, even
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(find(player1, "Grizzly Bears").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters-tapped applies to opponents' creatures too")
    void unmatchedParityOpponentCreatureEntersTapped() {
        addPrerogativeWithParity(player1, ManaValueParity.EVEN);
        harness.setHand(player2, List.of(new LlanowarElves())); // MV 1, odd
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(find(player2, "Llanowar Elves").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Odd chosen: an even-MV creature enters tapped")
    void oddChosenEvenCreatureEntersTapped() {
        addPrerogativeWithParity(player1, ManaValueParity.ODD);
        harness.setHand(player1, List.of(new GrizzlyBears())); // MV 2, even
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(find(player1, "Grizzly Bears").isTapped()).isTrue();
    }
}
