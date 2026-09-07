package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BasalThrull;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EndrekSahrMasterBreeder.class, BasalThrull.class, GrizzlyBears.class, Shock.class})
class EndrekSahrMasterBreederTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a creature creates one Thrull token per mana value")
    void creatureSpellCreatesThrullsEqualToManaValue() {
        harness.addToBattlefield(player1, new EndrekSahrMasterBreeder());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> thrulls = findPermanents(player1, "Thrull");
        assertThat(thrulls).hasSize(2);
        assertThat(thrulls).allSatisfy(thrull -> {
            assertThat(thrull.getCard().isToken()).isTrue();
            assertThat(gqs.getEffectivePower(gd, thrull)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, thrull)).isEqualTo(1);
        });
        harness.assertOnBattlefield(player1, "Endrek Sahr, Master Breeder");
    }

    @Test
    @DisplayName("Casting a noncreature spell does not create Thrulls")
    void noncreatureSpellDoesNotCreateThrulls() {
        harness.addToBattlefield(player1, new EndrekSahrMasterBreeder());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Thrull")).isEmpty();
        harness.assertOnBattlefield(player1, "Endrek Sahr, Master Breeder");
    }

    @Test
    @DisplayName("Sacrifices when its controller controls seven Thrulls")
    void sacrificesAtSevenThrulls() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new BasalThrull());
        }
        harness.addToBattlefield(player1, new EndrekSahrMasterBreeder());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.THRULL)))
                .hasSize(8);
        harness.assertOnBattlefield(player1, "Endrek Sahr, Master Breeder");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Endrek Sahr, Master Breeder");
        harness.assertInGraveyard(player1, "Endrek Sahr, Master Breeder");
    }
}
