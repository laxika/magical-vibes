package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HoverstonePilgrim.class, GrizzlyBears.class, HolyDay.class})
class HoverstonePilgrimTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a target card from an opponent's graveyard on the bottom of its owner's library")
    void putsTargetCardFromOpponentsGraveyardOnOwnersLibraryBottom() {
        int pilgrimIndex = addPilgrim();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Card target = new HolyDay();
        Card existing = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player2, List.of(existing));

        harness.activateAbilityWithGraveyardTargets(player1, pilgrimIndex, 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(existing, target);
    }

    @Test
    @DisplayName("Can put a target card from its controller's graveyard on the bottom of their library")
    void putsTargetCardFromOwnGraveyardOnOwnLibraryBottom() {
        int pilgrimIndex = addPilgrim();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Card target = new GrizzlyBears();
        Card existing = new HolyDay();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(existing));

        harness.activateAbilityWithGraveyardTargets(player1, pilgrimIndex, 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(existing, target);
    }

    @Test
    @DisplayName("Cannot activate without paying the generic cost")
    void cannotActivateWithoutMana() {
        int pilgrimIndex = addPilgrim();
        Card target = new HolyDay();
        harness.setGraveyard(player1, List.of(target));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, pilgrimIndex, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private int addPilgrim() {
        Permanent pilgrim = addCreatureReady(player1, new HoverstonePilgrim());
        return gd.playerBattlefields.get(player1.getId()).indexOf(pilgrim);
    }
}
