package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReitoLanternTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a card from your own graveyard on the bottom of your library")
    void tucksOwnGraveyardCard() {
        int lanternIdx = addLantern();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Card tucked = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(tucked)));
        harness.setLibrary(player1, new ArrayList<>(List.of(new HillGiant(), new GrizzlyBears())));

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

        Card tucked = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(tucked)));
        harness.setLibrary(player2, new ArrayList<>(List.of(new HillGiant())));

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

        Card tucked = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(tucked)));
        harness.setLibrary(player1, new ArrayList<>(List.of(new HillGiant())));

        harness.activateAbilityWithGraveyardTargets(player1, lanternIdx, 0, List.of(tucked.getId()));
        gd.playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).noneMatch(c -> c.getId().equals(tucked.getId()));
    }

    private int addLantern() {
        harness.addToBattlefield(player1, new ReitoLantern());
        Permanent lantern = findPermanent(player1, "Reito Lantern");
        lantern.setSummoningSick(false);
        return gd.playerBattlefields.get(player1.getId()).indexOf(lantern);
    }
}
