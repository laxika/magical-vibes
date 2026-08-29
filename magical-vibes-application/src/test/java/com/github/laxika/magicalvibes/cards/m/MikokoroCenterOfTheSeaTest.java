package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MikokoroCenterOfTheSeaTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: Add {C} produces colorless mana")
    void tapForColorlessMana() {
        addReadyMikokoro(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("{2}, {T}: Each player draws a card")
    void eachPlayerDrawsCard() {
        addReadyMikokoro(player1);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int p1DeckBefore = gd.playerDecks.get(player1.getId()).size();
        int p2DeckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(p1DeckBefore - 1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(p2DeckBefore - 1);
    }

    private Permanent addReadyMikokoro(Player player) {
        Permanent perm = new Permanent(new MikokoroCenterOfTheSea());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
