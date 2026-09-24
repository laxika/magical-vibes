package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.m.MyrRetriever;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.s.SquadronCarrier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NetworkMarauder.class, MyrRetriever.class, SolRing.class, SquadronCarrier.class, GrizzlyBears.class})
class NetworkMarauderTest extends BaseCardTest {

    @Test
    void entersAndPerpetuallyBoostsOwnedArtifactCreaturesAndSpacecraft() {
        Permanent myr = addOwnedPermanent(player1, new MyrRetriever());
        Permanent spacecraft = addOwnedPermanent(player1, new SquadronCarrier());
        Permanent bears = addOwnedPermanent(player1, new GrizzlyBears());
        int myrPower = gqs.getEffectivePower(gd, myr);
        int myrToughness = gqs.getEffectiveToughness(gd, myr);
        int spacecraftPower = gqs.getEffectivePower(gd, spacecraft);
        int spacecraftToughness = gqs.getEffectiveToughness(gd, spacecraft);

        harness.setHand(player1, List.of(new NetworkMarauder()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent marauder = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof NetworkMarauder)
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, marauder)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, marauder)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, myr)).isEqualTo(myrPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, myr)).isEqualTo(myrToughness + 1);
        assertThat(gqs.getEffectivePower(gd, spacecraft)).isEqualTo(spacecraftPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, spacecraft)).isEqualTo(spacecraftToughness + 1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void onlyAnotherArtifactWithManaValueAtLeastThreeTriggersTheAbility() {
        Permanent myr = addOwnedPermanent(player1, new MyrRetriever());
        harness.setHand(player1, List.of(new NetworkMarauder()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        int powerAfterFirstTrigger = gqs.getEffectivePower(gd, myr);
        harness.setHand(player1, List.of(new SolRing()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, myr)).isEqualTo(powerAfterFirstTrigger);

        harness.setHand(player1, List.of(new NetworkMarauder()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, myr)).isEqualTo(powerAfterFirstTrigger + 2);
    }

    private Permanent addOwnedPermanent(com.github.laxika.magicalvibes.model.Player player,
                                        com.github.laxika.magicalvibes.model.Card card) {
        card.setOwnerId(player.getId());
        return harness.addToBattlefieldAndReturn(player, card);
    }
}
