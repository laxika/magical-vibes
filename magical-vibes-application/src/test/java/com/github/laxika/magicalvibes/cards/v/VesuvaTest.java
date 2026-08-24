package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Vesuva.class, Forest.class})
class VesuvaTest extends BaseCardTest {

    @Test
    @DisplayName("Vesuva can enter tapped as a copy of a land")
    void entersTappedAsCopyOfLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Vesuva()));

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, forest.getId());

        Permanent vesuva = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(vesuva.getCard().getName()).isEqualTo("Forest");
        assertThat(vesuva.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Vesuva enters untapped when its copy choice is declined")
    void entersUntappedWhenCopyIsDeclined() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new Vesuva()));

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        Permanent vesuva = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(vesuva.getCard().getName()).isEqualTo("Vesuva");
        assertThat(vesuva.isTapped()).isFalse();
    }
}
