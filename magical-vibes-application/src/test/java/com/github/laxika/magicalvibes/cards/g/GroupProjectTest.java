package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TapUntappedPermanentsCost;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GroupProjectTest extends BaseCardTest {

    

    @Test
    @DisplayName("Resolving creates a 2/2 Spirit token")
    void createsSpiritToken() {
        harness.setHand(player1, List.of(new GroupProject()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Spirit")
                        && p.getEffectivePower() == 2
                        && p.getEffectiveToughness() == 2);
    }

    @Test
    @DisplayName("Flashback taps three creatures you control and creates another Spirit")
    void flashbackTapsThreeCreaturesAndCreatesToken() {
        Permanent c1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent c2 = addCreatureReady(player1, new GrizzlyBears());
        Permanent c3 = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GroupProject()));

        harness.castFlashbackWithTapCost(player1, 0, List.of(c1.getId(), c2.getId(), c3.getId()));
        harness.passBothPriorities();

        assertThat(c1.isTapped()).isTrue();
        assertThat(c2.isTapped()).isTrue();
        assertThat(c3.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Spirit"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Group Project"));
    }
}
