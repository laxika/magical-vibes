package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.h.HumbleBudoka;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReitoLantern.class, HumbleBudoka.class})
class ReitoLanternTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a card from your own graveyard on the bottom of your library")
    void tucksOwnGraveyardCard() {
        int lanternIdx = addLantern();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Card tucked = new HumbleBudoka();
        harness.setGraveyard(player1, new ArrayList<>(List.of(tucked)));
        harness.setLibrary(player1, new ArrayList<>(List.of(new HumbleBudoka(), new HumbleBudoka())));

        harness.activateAbilityWithGraveyardTargets(player1, lanternIdx, 0, List.of(tucked.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(3);
        assertThat(library.getLast().getId()).isEqualTo(tucked.getId());
    }

    @Test
    @DisplayName("Puts a card from an opponent's graveyard on the bottom of that opponent's library")
    void tucksOpponentGraveyardCard() {
        int lanternIdx = addLantern();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Card tucked = new HumbleBudoka();
        harness.setGraveyard(player2, new ArrayList<>(List.of(tucked)));
        harness.setLibrary(player2, new ArrayList<>(List.of(new HumbleBudoka())));

        harness.activateAbilityWithGraveyardTargets(player1, lanternIdx, 0, List.of(tucked.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        List<Card> library = gd.playerDecks.get(player2.getId());
        assertThat(library).hasSize(2);
        assertThat(library.getLast().getId()).isEqualTo(tucked.getId());
    }

    @Test
    @DisplayName("Does nothing if the target leaves the graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyard() {
        int lanternIdx = addLantern();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Card tucked = new HumbleBudoka();
        harness.setGraveyard(player1, new ArrayList<>(List.of(tucked)));
        harness.setLibrary(player1, new ArrayList<>(List.of(new HumbleBudoka())));

        harness.activateAbilityWithGraveyardTargets(player1, lanternIdx, 0, List.of(tucked.getId()));
        gd.playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).noneMatch(c -> c.getId().equals(tucked.getId()));
    }

    @Test
    @DisplayName("Cannot activate without three mana")
    void cannotActivateWithoutThreeMana() {
        int lanternIdx = addLantern();
        Card target = new HumbleBudoka();
        harness.setGraveyard(player1, List.of(target));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, lanternIdx, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target);
    }

    @Test
    @DisplayName("Can be activated again without tapping")
    void canBeActivatedAgainWithoutTapping() {
        int lanternIdx = addLantern();
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        Card firstTarget = new HumbleBudoka();
        Card secondTarget = new HumbleBudoka();
        Card existing = new HumbleBudoka();
        harness.setGraveyard(player1, new ArrayList<>(List.of(firstTarget, secondTarget)));
        harness.setLibrary(player1, new ArrayList<>(List.of(existing)));

        harness.activateAbilityWithGraveyardTargets(player1, lanternIdx, 0, List.of(firstTarget.getId()));
        harness.passBothPriorities();
        harness.activateAbilityWithGraveyardTargets(player1, lanternIdx, 0, List.of(secondTarget.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(existing.getId(), firstTarget.getId(), secondTarget.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()).get(lanternIdx).isTapped()).isFalse();
    }

    private int addLantern() {
        Permanent lantern = harness.addToBattlefieldAndReturn(player1, new ReitoLantern());
        lantern.setSummoningSick(false);
        return gd.playerBattlefields.get(player1.getId()).indexOf(lantern);
    }
}
