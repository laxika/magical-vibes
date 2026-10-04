package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FreedomFighterRecruit.class, GrizzlyBears.class, Mountain.class})
class FreedomFighterRecruitTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of creatures its controller controls")
    void powerEqualsControlledCreatures() {
        Permanent recruit = addCreatureReady(player1, new FreedomFighterRecruit());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, recruit)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, recruit)).isEqualTo(2);
    }

    @Test
    @DisplayName("Noncreature permanents do not increase its power")
    void doesNotCountNoncreatures() {
        Permanent recruit = addCreatureReady(player1, new FreedomFighterRecruit());
        harness.addToBattlefield(player1, new Mountain());

        assertThat(gqs.getEffectivePower(gd, recruit)).isEqualTo(1);
    }

    @Test
    @DisplayName("Power updates when controlled creatures change")
    void powerUpdatesWhenCreaturesChange() {
        Permanent recruit = addCreatureReady(player1, new FreedomFighterRecruit());
        harness.addToBattlefield(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, recruit)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent ->
                permanent.getCard().hasType(CardType.CREATURE)
                        && permanent != recruit);

        assertThat(gqs.getEffectivePower(gd, recruit)).isEqualTo(1);
    }

    @Test
    @DisplayName("In hand, power counts its owner's creatures without counting itself")
    void powerInHandCountsOwnersCreatures() {
        FreedomFighterRecruit recruit = new FreedomFighterRecruit();
        harness.setHand(player1, List.of(recruit));
        harness.addToBattlefield(player2, new FreedomFighterRecruit());

        assertThat(gqs.getEffectiveCardPower(gd, recruit)).isZero();

        harness.addToBattlefield(player1, new FreedomFighterRecruit());
        assertThat(gqs.getEffectiveCardPower(gd, recruit)).isEqualTo(1);
    }

    @Test
    @DisplayName("In the graveyard, power updates with its owner's creature count")
    void powerInGraveyardUpdates() {
        FreedomFighterRecruit recruit = new FreedomFighterRecruit();
        harness.setGraveyard(player1, List.of(recruit));
        harness.addToBattlefield(player1, new FreedomFighterRecruit());
        harness.addToBattlefield(player2, new FreedomFighterRecruit());

        assertThat(gqs.getEffectiveCardPower(gd, recruit)).isEqualTo(1);

        harness.addToBattlefield(player1, new FreedomFighterRecruit());
        assertThat(gqs.getEffectiveCardPower(gd, recruit)).isEqualTo(2);
    }

    @Test
    @DisplayName("After changing control, power counts the new controller's creatures")
    void powerUsesCurrentControllerRatherThanOwner() {
        FreedomFighterRecruit card = new FreedomFighterRecruit();
        card.setOwnerId(player1.getId());
        Permanent recruit = addCreatureReady(player1, card);
        harness.addToBattlefield(player1, new FreedomFighterRecruit());
        harness.addToBattlefield(player1, new FreedomFighterRecruit());
        harness.addToBattlefield(player2, new FreedomFighterRecruit());
        assertThat(gqs.getEffectivePower(gd, recruit)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(recruit);
        gd.playerBattlefields.get(player2.getId()).add(recruit);

        assertThat(gqs.getEffectivePower(gd, recruit)).isEqualTo(2);
    }
}
