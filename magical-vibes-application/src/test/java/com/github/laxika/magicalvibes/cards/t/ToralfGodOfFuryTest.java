package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.ExquisiteFirecraft;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.y.YomijiWhoBarsTheWay;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ToralfGodOfFury.class, ExquisiteFirecraft.class, HillGiant.class, YomijiWhoBarsTheWay.class})
class ToralfGodOfFuryTest extends BaseCardTest {

    @Test
    void dealsExcessNoncombatDamageToAnotherTarget() {
        harness.addToBattlefield(player1, new ToralfGodOfFury());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new ExquisiteFirecraft()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    void hammerDealsDamageReturnsToHandAndOnlyBoostsLegendaryCreature() {
        Permanent legendaryCreature = harness.addToBattlefieldAndReturn(player1, new YomijiWhoBarsTheWay());
        Permanent nonlegendaryCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        legendaryCreature.setSummoningSick(false);
        int legendaryPowerBefore = gqs.getEffectivePower(gd, legendaryCreature);
        int nonlegendaryPowerBefore = gqs.getEffectivePower(gd, nonlegendaryCreature);

        ToralfGodOfFury toralf = new ToralfGodOfFury();
        harness.setHand(player1, List.of(toralf));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();

        Permanent hammer = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof ToralfHammer)
                .findFirst()
                .orElseThrow();
        hammer.setAttachedTo(legendaryCreature.getId());
        harness.setLife(player2, 20);

        assertThat(gqs.getEffectivePower(gd, legendaryCreature)).isEqualTo(legendaryPowerBefore + 3);
        assertThat(gqs.getEffectivePower(gd, nonlegendaryCreature)).isEqualTo(nonlegendaryPowerBefore);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(toralf.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(hammer);
    }
}
