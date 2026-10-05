package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.SafeholdElite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MadblindMountain.class, MudbrawlerCohort.class, SafeholdElite.class})
class MadblindMountainTest extends BaseCardTest {

    @Test
    @DisplayName("Shuffle ability resolves when controlling two or more red permanents")
    void shuffleWithTwoRedPermanents() {
        Permanent mountain = addMountain(player1);
        addRedPermanents(player1, 2); // the colorless land contributes nothing, so both come from here
        harness.addMana(player1, ManaColor.RED, 1);

        int mountainIdx = gd.playerBattlefields.get(player1.getId()).indexOf(mountain);
        harness.activateAbility(player1, mountainIdx, 1, null, null);
        harness.passBothPriorities();

        // The {R} cost was paid and the land is tapped.
        assertThat(mountain.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    @DisplayName("Shuffle ability cannot be activated with fewer than two red permanents")
    void shuffleRejectedWithTooFewRedPermanents() {
        Permanent mountain = addMountain(player1);
        // One red permanent — the colorless mountain does not make up the second.
        addRedPermanents(player1, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        int mountainIdx = gd.playerBattlefields.get(player1.getId()).indexOf(mountain);
        assertThatThrownBy(() -> harness.activateAbility(player1, mountainIdx, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-red permanents do not count toward the activation restriction")
    void nonRedPermanentsDoNotCount() {
        Permanent mountain = addMountain(player1);
        addCreatureReady(player1, new SafeholdElite());
        addCreatureReady(player1, new SafeholdElite());
        harness.addMana(player1, ManaColor.RED, 1);

        // Neither the colorless mountain nor the two green creatures count — nothing red is here.
        int mountainIdx = gd.playerBattlefields.get(player1.getId()).indexOf(mountain);
        assertThatThrownBy(() -> harness.activateAbility(player1, mountainIdx, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tap ability adds red mana")
    void manaAbilityAddsRed() {
        Permanent mountain = addMountain(player1);

        int mountainIdx = gd.playerBattlefields.get(player1.getId()).indexOf(mountain);
        harness.activateAbility(player1, mountainIdx, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void entersTappedWhenPlayed() {
        harness.setHand(player1, List.of(new MadblindMountain()));
        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Madblind Mountain").isTapped()).isTrue();
    }

    @Test
    void shuffleUsesStackAndPreservesBothLibraries() {
        Permanent mountain = addMountain(player1);
        addRedPermanents(player1, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        MadblindMountain first = new MadblindMountain();
        MudbrawlerCohort second = new MudbrawlerCohort();
        SafeholdElite third = new SafeholdElite();
        SafeholdElite opponentCard = new SafeholdElite();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setLibrary(player2, List.of(opponentCard));

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mountain), 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gameLogContains("shuffles their library.")).isFalse();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains(player1.getUsername() + " shuffles their library.")).isTrue();
        assertThat(gameLogContains(player2.getUsername() + " shuffles their library.")).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second, third);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
    }

    @Test
    void opponentsRedPermanentsDoNotCount() {
        Permanent mountain = addMountain(player1);
        addRedPermanents(player1, 1);
        addRedPermanents(player2, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mountain), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mountain.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void shuffleStillResolvesAfterRedPermanentsLeave() {
        Permanent mountain = addMountain(player1);
        addRedPermanents(player1, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLibrary(player1, List.of());
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mountain), 1, null, null);
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p != mountain);

        harness.passBothPriorities();

        assertThat(gameLogContains(player1.getUsername() + " shuffles their library.")).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void shuffleRequiresRedMana() {
        Permanent mountain = addMountain(player1);
        addRedPermanents(player1, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mountain), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mountain.isTapped()).isFalse();
    }

    @Test
    void tappedMountainCannotShuffle() {
        Permanent mountain = addMountain(player1);
        mountain.tap();
        addRedPermanents(player1, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mountain), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addMountain(Player player) {
        Permanent mountain = harness.addToBattlefieldAndReturn(player, new MadblindMountain());
        mountain.setSummoningSick(false);
        mountain.untap();
        return mountain;
    }

    private void addRedPermanents(Player player, int count) {
        for (int i = 0; i < count; i++) {
            addCreatureReady(player, new MudbrawlerCohort());
        }
    }
}
