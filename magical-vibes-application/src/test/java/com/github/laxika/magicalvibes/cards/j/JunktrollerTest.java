package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JunktrollerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a target card from an opponent's graveyard on the bottom of its owner's library")
    void putsTargetCardFromOpponentsGraveyardOnOwnersLibraryBottom() {
        Permanent junktroller = addReadyJunktroller(player1);
        Card target = new HolyDay();
        Card oldTop = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player2, List.of(oldTop));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(oldTop, target);
        assertThat(junktroller.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does nothing if the target card leaves the graveyard before resolution")
    void doesNothingIfTargetLeavesGraveyard() {
        addReadyJunktroller(player1);
        Card target = new HolyDay();
        harness.setGraveyard(player2, List.of(target));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        gd.playerGraveyards.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(target);
    }

    private Permanent addReadyJunktroller(Player player) {
        Permanent permanent = new Permanent(new Junktroller());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
