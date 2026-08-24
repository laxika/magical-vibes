package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OverzealousMuscle;
import com.github.laxika.magicalvibes.cards.s.ServantOfTheStinger;
import com.github.laxika.magicalvibes.cards.v.VadmirNewBlood;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        HellspurBrute.class,
        OverzealousMuscle.class,
        ServantOfTheStinger.class,
        VadmirNewBlood.class,
        GrizzlyBears.class
})
class HellspurBruteTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for outlaws reduces its generic mana cost")
    void affinityForOutlawsReducesGenericManaCost() {
        harness.addToBattlefield(player1, new OverzealousMuscle());
        harness.addToBattlefield(player1, new OverzealousMuscle());
        harness.addToBattlefield(player1, new ServantOfTheStinger());
        harness.addToBattlefield(player1, new ServantOfTheStinger());
        harness.addToBattlefield(player1, new VadmirNewBlood());
        harness.setHand(player1, List.of(new HellspurBrute()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).hasSize(1);
    }

    @Test
    @DisplayName("Affinity counts only outlaws controlled by the spell's controller")
    void affinityCountsOnlyControlledOutlaws() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player2, new OverzealousMuscle());
        }
        harness.setHand(player1, List.of(new HellspurBrute()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Affinity ignores permanents without an outlaw creature type")
    void affinityIgnoresNonOutlaws() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new GrizzlyBears());
        }
        harness.setHand(player1, List.of(new HellspurBrute()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
