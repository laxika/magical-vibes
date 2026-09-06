package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfBlossoms;
import com.github.laxika.magicalvibes.cards.w.WallOfStone;
import com.github.laxika.magicalvibes.cards.y.YargleAndMultani;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChargeOfTheForeverBeast.class, Forest.class, GrizzlyBears.class, WallOfBlossoms.class,
        WallOfStone.class, YargleAndMultani.class})
class ChargeOfTheForeverBeastTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the revealed creature's power")
    void dealsRevealedPowerAsDamage() {
        Permanent target = addCreatureReady(player2, new WallOfStone());
        YargleAndMultani revealed = new YargleAndMultani();
        harness.setHand(player1, List.of(new ChargeOfTheForeverBeast(), revealed));
        addMana();

        castCharge(target.getId(), 1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(revealed);
    }

    @Test
    @DisplayName("Uses zero damage for a creature card with zero power")
    void zeroPowerDealsNoDamage() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        WallOfBlossoms revealed = new WallOfBlossoms();
        harness.setHand(player1, List.of(new ChargeOfTheForeverBeast(), revealed));
        addMana();

        castCharge(target.getId(), 1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot be cast without a creature card to reveal")
    void requiresCreatureCardToReveal() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ChargeOfTheForeverBeast(), new Forest()));
        addMana();

        assertThatThrownBy(() -> castCharge(target.getId(), 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
    }

    @Test
    @DisplayName("Rejects a non-creature and non-planeswalker target")
    void rejectsInvalidTarget() {
        harness.setHand(player1, List.of(new ChargeOfTheForeverBeast(), new WallOfBlossoms()));
        addMana();

        assertThatThrownBy(() -> castCharge(player2.getId(), 1))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castCharge(UUID targetId, int revealedHandCardIndex) {
        gs.playCard(gd, player1, 0, 0, targetId, null, List.of(), List.of(), false,
                null, null, List.of(), null, List.of(), false, revealedHandCardIndex);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
