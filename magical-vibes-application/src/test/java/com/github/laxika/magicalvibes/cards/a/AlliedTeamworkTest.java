package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.k.KomodoRhino;
import com.github.laxika.magicalvibes.cards.k.KyoshiWarriorGuard;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlliedTeamwork.class, KomodoRhino.class, KyoshiWarriorGuard.class})
class AlliedTeamworkTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a 1/1 white Ally token")
    void createsAllyToken() {
        harness.castFromHand(player1, new AlliedTeamwork(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent ally = findPermanent(player1, "Ally");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(ally.getCard().isToken()).isTrue();
        assertThat(ally.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(ally.getCard().getSubtypes()).containsExactly(CardSubtype.ALLY);
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost non-Allies on either battlefield")
    void doesNotBoostNonAllies() {
        harness.addToBattlefield(player1, new AlliedTeamwork());
        Permanent ownRhino = harness.addToBattlefieldAndReturn(player1, new KomodoRhino());
        Permanent opponentRhino = harness.addToBattlefieldAndReturn(player2, new KomodoRhino());

        assertThat(gqs.getEffectivePower(gd, ownRhino)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ownRhino)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentRhino)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, opponentRhino)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boosts only friendly Allies, including Allies already on the battlefield")
    void boostsOwnAlliesOnly() {
        Permanent ownAlly = harness.addToBattlefieldAndReturn(player1, new KyoshiWarriorGuard());
        Permanent opponentAlly = harness.addToBattlefieldAndReturn(player2, new KyoshiWarriorGuard());
        harness.castFromHand(player1, new AlliedTeamwork(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownAlly)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownAlly)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponentAlly)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentAlly)).isEqualTo(3);
    }

    @Test
    @DisplayName("Multiple copies each boost Allies")
    void multipleCopiesStack() {
        harness.addToBattlefield(player1, new AlliedTeamwork());
        harness.addToBattlefield(player1, new AlliedTeamwork());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new KyoshiWarriorGuard());

        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(5);
    }
}
