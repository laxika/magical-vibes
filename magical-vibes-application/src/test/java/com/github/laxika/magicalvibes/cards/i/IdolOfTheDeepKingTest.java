package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.SovereignsMacuahuitl;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IdolOfTheDeepKing.class, SovereignsMacuahuitl.class, GrizzlyBears.class, Millstone.class})
class IdolOfTheDeepKingTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Idol of the Deep King deals 2 damage to the chosen player")
    void enterTheBattlefieldDealsDamageToPlayer() {
        int lifeBefore = gd.getLife(player2.getId());
        harness.setHand(player1, List.of(new IdolOfTheDeepKing()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Craft returns Idol of the Deep King transformed and attaches the Equipment to a creature")
    void craftsIntoSovereignsMacuahuitlAndAttachesIt() {
        Permanent idol = harness.addToBattlefieldAndReturn(player1, new IdolOfTheDeepKing());
        harness.addToBattlefield(player1, new Millstone());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent equipment = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SovereignsMacuahuitl)
                .findFirst().orElseThrow();
        assertThat(idol.isTransformed()).isTrue();
        assertThat(equipment).isSameAs(idol);
        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
    }
}
