package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DurkwoodBaloth;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.m.MagusOfTheScroll;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WordOfSeizing.class, DurkwoodBaloth.class, Mountain.class, ThinkTwice.class, MagusOfTheScroll.class})
class WordOfSeizingTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps, steals until end of turn, and grants haste to a permanent")
    void untapsStealsAndGrantsHaste() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DurkwoodBaloth());
        target.tap();
        castWordOfSeizing(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isTrue();
    }

    @Test
    @DisplayName("Can target and untap a land")
    void canTargetAndUntapLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mountain());
        target.tap();
        castWordOfSeizing(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Control and haste expire at cleanup")
    void controlAndHasteExpireAtCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DurkwoodBaloth());
        castWordOfSeizing(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
    }

    @Test
    @DisplayName("Fizzles if the target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DurkwoodBaloth());
        target.tap();
        harness.setHand(player1, List.of(new WordOfSeizing()));
        addMana(player1);

        harness.castInstant(player1, 0, target.getId());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Word of Seizing");
        harness.assertInGraveyard(player2, "Durkwood Baloth");
    }

    @Test
    @DisplayName("Split second prevents a spell response")
    void splitSecondPreventsSpellResponse() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DurkwoodBaloth());
        harness.setHand(player1, List.of(new WordOfSeizing()));
        addMana(player1);
        harness.setHand(player2, List.of(new ThinkTwice()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());

        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can untap and grant haste to a permanent already under your control")
    void canTargetOwnPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DurkwoodBaloth());
        target.tap();

        castWordOfSeizing(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        harness.assertOnBattlefield(player1, "Durkwood Baloth");
        harness.assertNotOnBattlefield(player2, "Durkwood Baloth");
    }

    @Test
    @DisplayName("Split second permits tapping a land for mana before resolution")
    void splitSecondAllowsManaAbility() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new WordOfSeizing()));
        addMana(player1);
        harness.castInstant(player1, 0, target.getId());

        harness.tapPermanent(player2, 0);

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(target.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Mountain");
    }

    @Test
    @DisplayName("Granted haste allows using a stolen creature's tap ability immediately")
    void hasteAllowsStolenCreaturesTapAbility() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MagusOfTheScroll());
        castWordOfSeizing(target);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Split second blocks a nonmana activated ability without paying its costs")
    void splitSecondBlocksNonManaAbility() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MagusOfTheScroll());
        target.setSummoningSick(false);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new WordOfSeizing()));
        addMana(player1);
        harness.castInstant(player1, 0, target.getId());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("split second");
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }

    private void castWordOfSeizing(Permanent target) {
        harness.setHand(player1, List.of(new WordOfSeizing()));
        addMana(player1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void addMana(Player player) {
        harness.addMana(player, ManaColor.RED, 2);
        harness.addMana(player, ManaColor.COLORLESS, 3);
    }
}
