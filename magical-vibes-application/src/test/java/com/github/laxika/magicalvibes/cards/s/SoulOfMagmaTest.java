package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DampenThought;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoulOfMagma.class, DampenThought.class, IsamaruHoundOfKonda.class})
class SoulOfMagmaTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a Spirit spell deals 1 damage to target creature")
    void spiritCastDealsDamage() {
        harness.addToBattlefield(player1, new SoulOfMagma());
        Permanent target = addCreatureReady(player2, new IsamaruHoundOfKonda());
        harness.castFromHand(player1, new SoulOfMagma(), "{3}{R}{R}");

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting an Arcane spell deals 1 damage to target creature")
    void arcaneCastDealsDamage() {
        harness.addToBattlefield(player1, new SoulOfMagma());
        Permanent target = addCreatureReady(player2, new IsamaruHoundOfKonda());
        harness.setHand(player1, List.of(new DampenThought()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a spell that is neither Spirit nor Arcane does not trigger")
    void unrelatedSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new SoulOfMagma());
        Permanent target = addCreatureReady(player2, new IsamaruHoundOfKonda());
        harness.castFromHand(player1, new IsamaruHoundOfKonda(), "{W}");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("An opponent's Spirit or Arcane spell does not trigger")
    void opponentCastDoesNotTrigger() {
        harness.addToBattlefield(player1, new SoulOfMagma());
        Permanent target = addCreatureReady(player2, new IsamaruHoundOfKonda());
        harness.setHand(player2, List.of(new DampenThought()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
    }
}
