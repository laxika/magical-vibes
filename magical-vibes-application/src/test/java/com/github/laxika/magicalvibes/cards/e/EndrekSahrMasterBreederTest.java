package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AngelsGrace;
import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.g.Greenseeker;
import com.github.laxika.magicalvibes.cards.h.HavenwoodWurm;
import com.github.laxika.magicalvibes.cards.k.KaervekTheSpiteful;
import com.github.laxika.magicalvibes.cards.s.SuddenDeath;
import com.github.laxika.magicalvibes.cards.w.WalkingBallista;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EndrekSahrMasterBreeder.class, AngelsGrace.class, AshcoatBear.class,
        Greenseeker.class, HavenwoodWurm.class, SuddenDeath.class})
class EndrekSahrMasterBreederTest extends BaseCardTest {

    @Test
    @CardUsed({WalkingBallista.class})
    @DisplayName("A creature with zero mana value creates no Thrulls")
    void zeroManaValueCreatesNoThrulls() {
        harness.addToBattlefield(player1, new EndrekSahrMasterBreeder());
        harness.setHand(player1, List.of(new WalkingBallista()));

        harness.castArtifact(player1, 0, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Thrull")).isEmpty();
        harness.assertOnBattlefield(player1, "Endrek Sahr, Master Breeder");
        harness.assertInGraveyard(player1, "Walking Ballista");
    }

    @Test
    @DisplayName("The token trigger resolves after Endrek leaves the battlefield")
    void tokenTriggerSurvivesSourceRemoval() {
        harness.addToBattlefield(player1, new EndrekSahrMasterBreeder());
        harness.setHand(player1, List.of(new AshcoatBear()));
        harness.setHand(player2, List.of(new SuddenDeath()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Endrek Sahr, Master Breeder"));
        harness.assertInGraveyard(player1, "Endrek Sahr, Master Breeder");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Thrull")).hasSize(2);
        harness.assertOnBattlefield(player1, "Ashcoat Bear");
    }

    @Test
    @DisplayName("Endrek is sacrificed even if the Thrull count drops before resolution")
    void sacrificeDoesNotRecheckThrullCount() {
        harness.addToBattlefield(player1, new EndrekSahrMasterBreeder());
        harness.setHand(player1, List.of(new HavenwoodWurm()));
        harness.setHand(player2, List.of(new SuddenDeath()));
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Thrull")).hasSize(7);
        harness.assertNotOnBattlefield(player1, "Havenwood Wurm");

        harness.castAndResolveInstant(player2, 0, findPermanent(player1, "Thrull").getId());
        assertThat(findPermanents(player1, "Thrull")).hasSize(6);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Endrek Sahr, Master Breeder");
        harness.assertOnBattlefield(player1, "Havenwood Wurm");
    }

    @Test
    @CardUsed({KaervekTheSpiteful.class})
    @DisplayName("Seven Thrulls trigger the sacrifice even when they immediately die")
    void transientSevenThrullsStillTriggerSacrifice() {
        harness.addToBattlefield(player1, new EndrekSahrMasterBreeder());
        harness.addToBattlefield(player2, new KaervekTheSpiteful());
        harness.setHand(player1, List.of(new HavenwoodWurm()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Thrull")).isEmpty();
        harness.assertNotOnBattlefield(player1, "Endrek Sahr, Master Breeder");
        harness.assertInGraveyard(player1, "Endrek Sahr, Master Breeder");
        harness.assertOnBattlefield(player1, "Havenwood Wurm");
    }

    @Test
    @DisplayName("Casting a creature creates one Thrull token per mana value")
    void creatureSpellCreatesThrullsEqualToManaValue() {
        harness.addToBattlefield(player1, new EndrekSahrMasterBreeder());
        harness.setHand(player1, List.of(new AshcoatBear()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> thrulls = findPermanents(player1, "Thrull");
        assertThat(thrulls).hasSize(2);
        assertThat(thrulls).allSatisfy(thrull -> {
            assertThat(thrull.getCard().isToken()).isTrue();
            assertThat(thrull.getCard().getColors()).containsExactly(CardColor.BLACK);
            assertThat(thrull.getCard().getSubtypes()).containsExactly(CardSubtype.THRULL);
            assertThat(gqs.getEffectivePower(gd, thrull)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, thrull)).isEqualTo(1);
        });
        harness.assertOnBattlefield(player1, "Endrek Sahr, Master Breeder");
    }

    @Test
    @DisplayName("Casting a noncreature spell does not create Thrulls")
    void noncreatureSpellDoesNotCreateThrulls() {
        harness.addToBattlefield(player1, new EndrekSahrMasterBreeder());
        harness.setHand(player1, List.of(new AngelsGrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(findPermanents(player1, "Thrull")).isEmpty();
        harness.assertOnBattlefield(player1, "Endrek Sahr, Master Breeder");
    }

    @Test
    @DisplayName("An opponent's creature spell does not create Thrulls")
    void opponentCreatureSpellDoesNotCreateThrulls() {
        harness.addToBattlefield(player1, new EndrekSahrMasterBreeder());
        harness.setHand(player2, List.of(new AshcoatBear()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Thrull")).isEmpty();
        harness.assertOnBattlefield(player1, "Endrek Sahr, Master Breeder");
    }

    @Test
    @CardUsed({WalkingBallista.class})
    @DisplayName("Uses the full mana value of an X-cost creature spell")
    void xCreatureSpellCreatesThrullsEqualToFullManaValue() {
        harness.addToBattlefield(player1, new EndrekSahrMasterBreeder());
        harness.setHand(player1, List.of(new WalkingBallista()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0, 3);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Thrull")).hasSize(6);
        harness.assertOnBattlefield(player1, "Walking Ballista");
        harness.assertOnBattlefield(player1, "Endrek Sahr, Master Breeder");
    }

    @Test
    @DisplayName("Sacrifices when its controller controls seven or more Thrulls")
    void sacrificesAtSevenThrulls() {
        harness.addToBattlefield(player1, new EndrekSahrMasterBreeder());
        harness.setHand(player1, List.of(
                new AshcoatBear(), new AshcoatBear(), new AshcoatBear(), new Greenseeker()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        for (int i = 0; i < 3; i++) {
            harness.castCreature(player1, 0);
            resolveAllTriggers();
        }

        assertThat(findPermanents(player1, "Thrull")).hasSize(6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Thrull")).hasSize(7);
        harness.assertOnBattlefield(player1, "Endrek Sahr, Master Breeder");

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Endrek Sahr, Master Breeder");
        harness.assertInGraveyard(player1, "Endrek Sahr, Master Breeder");
    }
}
