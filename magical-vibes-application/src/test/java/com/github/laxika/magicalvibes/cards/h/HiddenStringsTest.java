package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.m.MazeBehemoth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HiddenStrings.class, MazeBehemoth.class})
class HiddenStringsTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps a tapped permanent and taps an untapped one")
    void togglesBothTargets() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new MazeBehemoth());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new MazeBehemoth());
        mine.tap();
        harness.setHand(player1, List.of(new HiddenStrings()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(mine.getId(), theirs.getId()));
        chooseAction("Untap");
        chooseAction("Tap");
        harness.handleMayAbilityChosen(player1, false);

        assertThat(mine.isTapped()).isFalse();
        assertThat(theirs.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Hidden Strings");
    }

    @Test
    @DisplayName("Cannot choose the same permanent for both targets")
    void cannotTargetSamePermanentTwice() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MazeBehemoth());
        harness.setHand(player1, List.of(new HiddenStrings()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Encodes on a creature and casts a copy after combat damage")
    void encodesAndCastsCopy() {
        Permanent attacker = addCreatureReady(player1, new MazeBehemoth());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new MazeBehemoth());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new MazeBehemoth());
        harness.setHand(player1, List.of(new HiddenStrings()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(first.getId(), second.getId()));
        chooseAction("Tap");
        chooseAction("Tap");
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, attacker.getId());

        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getName().equals("Hidden Strings"));
        harness.assertNotInGraveyard(player1, "Hidden Strings");
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        chooseAction("Untap");
        chooseAction("Untap");

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
    }

    @Test
    @DisplayName("May decline both actions independently")
    void mayLeaveBothTargetsUnchanged() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MazeBehemoth());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new MazeBehemoth());
        first.tap();
        harness.setHand(player1, List.of(new HiddenStrings()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(first.getId(), second.getId()));
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Hidden Strings");
    }

    @Test
    @DisplayName("May decline the first action and accept the second")
    void mayChangeOnlySecondTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MazeBehemoth());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new MazeBehemoth());
        harness.setHand(player1, List.of(new HiddenStrings()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(first.getId(), second.getId()));
        harness.handleMayAbilityChosen(player1, false);
        chooseAction("Tap");
        harness.handleMayAbilityChosen(player1, false);

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isTrue();
    }

    @Test
    @DisplayName("May tap an already tapped permanent without untapping it")
    void mayChooseTapForTappedTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MazeBehemoth());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new MazeBehemoth());
        first.tap();
        harness.setHand(player1, List.of(new HiddenStrings()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(first.getId(), second.getId()));
        chooseAction("Tap");
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not resolve or encode when both targets leave")
    void bothTargetsIllegalPreventsCipher() {
        harness.addToBattlefield(player1, new MazeBehemoth());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new MazeBehemoth());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new MazeBehemoth());
        harness.setHand(player1, List.of(new HiddenStrings()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player2.getId()).removeAll(List.of(first, second));
        gd.playerGraveyards.get(player2.getId()).addAll(List.of(first.getCard(), second.getCard()));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.exiledCards).isEmpty();
        harness.assertInGraveyard(player1, "Hidden Strings");
    }

    @Test
    @DisplayName("Still resolves for the remaining legal target")
    void oneIllegalTargetDoesNotPreventRemainingActionOrCipher() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new MazeBehemoth());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MazeBehemoth());
        harness.setHand(player1, List.of(new HiddenStrings()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(first);
        gd.playerGraveyards.get(player2.getId()).add(first.getCard());
        harness.passBothPriorities();
        chooseAction("Tap");
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, second.getId());

        assertThat(second.isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Hidden Strings");
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getName().equals("Hidden Strings"));
    }

    @Test
    @DisplayName("Losing all abilities suppresses the encoded combat damage trigger")
    void losingAbilitiesSuppressesCipherTrigger() {
        Permanent attacker = addCreatureReady(player1, new MazeBehemoth());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new MazeBehemoth());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new MazeBehemoth());
        harness.setHand(player1, List.of(new HiddenStrings()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(first.getId(), second.getId()));
        chooseAction("Tap");
        chooseAction("Tap");
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, attacker.getId());
        attacker.setLosesAllAbilitiesUntilEndOfTurn(true);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 15);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.exiledCards).hasSize(1);
    }
    private void chooseAction(String action) {
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, action);
    }
}
