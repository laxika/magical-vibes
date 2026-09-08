package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({USAgentJohnWalker.class, GrizzlyBears.class})
class USAgentJohnWalkerTest extends BaseCardTest {

    @Test
    void createsAndAttachesSturdyShield() {
        castAgent();

        Permanent agent = findPermanent(player1, "U.S.Agent, John Walker");
        Permanent shield = findPermanent(player1, "Sturdy Shield");

        assertThat(shield.getAttachedTo()).isEqualTo(agent.getId());
        assertThat(gqs.getEffectivePower(gd, agent)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, agent)).isEqualTo(4);
    }

    @Test
    void sturdyShieldCanBeEquippedToAnotherCreature() {
        castAgent();
        Permanent shield = findPermanent(player1, "Sturdy Shield");
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int shieldIndex = gd.playerBattlefields.get(player1.getId()).indexOf(shield);
        harness.activateAbility(player1, shieldIndex, null, bears.getId());
        harness.passBothPriorities();

        Permanent agent = findPermanent(player1, "U.S.Agent, John Walker");
        assertThat(shield.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, agent)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, agent)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    private void castAgent() {
        harness.setHand(player1, List.of(new USAgentJohnWalker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
