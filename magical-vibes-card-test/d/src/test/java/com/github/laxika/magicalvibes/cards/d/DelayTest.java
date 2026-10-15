package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BlindPhantasm;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Delay.class, BlindPhantasm.class})
class DelayTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the target spell and exiles it with three time counters")
    void countersAndSuspendsTargetSpell() {
        BlindPhantasm phantasm = castBlindPhantasmAndDelay();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Blind Phantasm");
        harness.assertNotInGraveyard(player1, "Blind Phantasm");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(phantasm);
        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(phantasm.getId(), player1.getId(), 3));
    }

    @Test
    @DisplayName("The suspended spell loses one time counter on each owner's upkeep")
    void removesTimeCounterOnOwnersUpkeep() {
        BlindPhantasm phantasm = castBlindPhantasmAndDelay();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(phantasm.getId(), player1.getId(), 2));
    }

    @Test
    @DisplayName("The owner may cast the exiled creature for free after its last time counter and it has haste")
    void freeCastsSuspendedCreatureWithHaste() {
        BlindPhantasm phantasm = castBlindPhantasmAndDelay();

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.suspendedSpellExiles).isEmpty();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        harness.passBothPriorities();

        Permanent entered = findPermanent(player1, "Blind Phantasm");
        assertThat(gqs.hasKeyword(gd, entered, Keyword.HASTE)).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(phantasm);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(gqs.hasKeyword(gd, entered, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Removing the last time counter puts a separate casting trigger on the stack")
    void lastCounterCreatesSeparateCastingTrigger() {
        BlindPhantasm phantasm = castBlindPhantasmAndDelay();

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        assertThat(gd.suspendedSpellExiles).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(phantasm);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("The opponent's upkeep does not remove time counters")
    void opponentsUpkeepDoesNotRemoveCounter() {
        BlindPhantasm phantasm = castBlindPhantasmAndDelay();

        advanceToUpkeep(player2);

        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(phantasm.getId(), player1.getId(), 3));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining the free cast leaves the card exiled without further suspend triggers")
    void decliningFreeCastLeavesCardExiled() {
        BlindPhantasm phantasm = castBlindPhantasmAndDelay();

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        advanceToUpkeep(player1);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(phantasm);
        assertThat(gd.suspendedSpellExiles).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Blind Phantasm");
    }

    @Test
    @DisplayName("Cannot target a permanent already on the battlefield")
    void cannotTargetPermanent() {
        Permanent phantasm = harness.addToBattlefieldAndReturn(player1, new BlindPhantasm());
        harness.setHand(player2, List.of(new Delay()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, phantasm.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private BlindPhantasm castBlindPhantasmAndDelay() {
        BlindPhantasm phantasm = new BlindPhantasm();
        harness.castFromHand(player1, phantasm, "{2}{U}");
        harness.setHand(player2, List.of(new Delay()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, phantasm.getId());
        return phantasm;
    }
}
