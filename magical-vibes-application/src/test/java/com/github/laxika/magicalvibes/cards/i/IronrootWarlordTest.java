package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IronrootWarlordTest extends BaseCardTest {

    @Test
    @DisplayName("Ironroot Warlord's power equals the number of creatures its controller controls")
    void powerEqualsControlledCreatures() {
        Permanent warlord = addWarlordReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, warlord)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warlord)).isEqualTo(5);
    }

    @Test
    @DisplayName("Ironroot Warlord creates a Soldier token without tapping")
    void createsSoldierToken() {
        Permanent warlord = addWarlordReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && "Soldier".equals(permanent.getCard().getName())
                        && permanent.getCard().getPower() == 1
                        && permanent.getCard().getToughness() == 1
                        && permanent.getCard().getColor() == CardColor.WHITE);
        assertThat(warlord.isTapped()).isFalse();
    }

    private Permanent addWarlordReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new IronrootWarlord());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
