package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FreedomFighterRecruit.class, GrizzlyBears.class, Mountain.class})
class FreedomFighterRecruitTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of creatures its controller controls")
    void powerEqualsControlledCreatures() {
        Permanent recruit = addRecruit(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, recruit)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, recruit)).isEqualTo(2);
    }

    @Test
    @DisplayName("Noncreature permanents do not increase its power")
    void doesNotCountNoncreatures() {
        Permanent recruit = addRecruit(player1);
        harness.addToBattlefield(player1, new Mountain());

        assertThat(gqs.getEffectivePower(gd, recruit)).isEqualTo(1);
    }

    @Test
    @DisplayName("Power updates when controlled creatures change")
    void powerUpdatesWhenCreaturesChange() {
        Permanent recruit = addRecruit(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, recruit)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent ->
                permanent.getCard().hasType(CardType.CREATURE)
                        && permanent != recruit);

        assertThat(gqs.getEffectivePower(gd, recruit)).isEqualTo(1);
    }

    private Permanent addRecruit(Player player) {
        Permanent recruit = new Permanent(new FreedomFighterRecruit());
        recruit.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(recruit);
        return recruit;
    }
}
