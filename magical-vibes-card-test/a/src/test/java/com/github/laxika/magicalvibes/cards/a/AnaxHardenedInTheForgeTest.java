package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.cards.s.StormsWrath;
import com.github.laxika.magicalvibes.cards.t.TectonicGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnaxHardenedInTheForge.class, DoomBlade.class, GrizzlyBears.class, ShivanDragon.class,
        StormsWrath.class, TectonicGiant.class})
class AnaxHardenedInTheForgeTest extends BaseCardTest {

    @Test
    @DisplayName("Anax's power equals red devotion")
    void powerEqualsRedDevotion() {
        Permanent anax = harness.addToBattlefieldAndReturn(player1, new AnaxHardenedInTheForge());

        assertThat(gqs.getEffectivePower(gd, anax)).isEqualTo(2);

        harness.addToBattlefield(player1, new ShivanDragon());

        assertThat(gqs.getEffectivePower(gd, anax)).isEqualTo(4);
    }

    @Test
    @DisplayName("A nontoken creature with power less than four creates one Satyr")
    void lowPowerNontokenCreatureCreatesOneSatyr() {
        harness.addToBattlefield(player1, new AnaxHardenedInTheForge());
        Permanent grizzlyBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        destroyWithDoomBlade(grizzlyBears);

        List<Permanent> satyrs = findPermanents(player1, "Satyr");
        assertThat(satyrs).hasSize(1);
        assertThat(gqs.hasActiveStaticEffect(gd, satyrs.getFirst(), CantBlockEffect.class)).isTrue();
    }

    @Test
    @DisplayName("A nontoken creature with power four or greater creates two Satyrs")
    void highPowerNontokenCreatureCreatesTwoSatyrs() {
        harness.addToBattlefield(player1, new AnaxHardenedInTheForge());
        Permanent shivanDragon = harness.addToBattlefieldAndReturn(player1, new ShivanDragon());

        destroyWithDoomBlade(shivanDragon);

        assertThat(findPermanents(player1, "Satyr")).hasSize(2);
    }

    @Test
    @DisplayName("Anax's own death trigger uses Anax's power before it dies")
    void selfDeathUsesLastKnownPower() {
        Permanent anax = harness.addToBattlefieldAndReturn(player1, new AnaxHardenedInTheForge());
        harness.addToBattlefield(player1, new ShivanDragon());

        destroyWithDoomBlade(anax);

        assertThat(findPermanents(player1, "Satyr")).hasSize(2);
    }

    @Test
    @DisplayName("Anax's own low power creates one Satyr")
    void lowPowerSelfDeathCreatesOneSatyr() {
        Permanent anax = harness.addToBattlefieldAndReturn(player1, new AnaxHardenedInTheForge());

        destroyWithDoomBlade(anax);

        assertThat(findPermanents(player1, "Satyr")).hasSize(1);
    }

    @Test
    @DisplayName("A token creature's death does not trigger Anax")
    void tokenDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new AnaxHardenedInTheForge());
        Permanent grizzlyBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        destroyWithDoomBlade(grizzlyBears);

        Permanent satyr = findPermanents(player1, "Satyr").getFirst();
        destroyWithDoomBlade(satyr);

        assertThat(findPermanents(player1, "Satyr")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Simultaneous deaths preserve Anax's devotion when the other creature is removed first")
    void simultaneousDeathsUseDevotionBeforeEitherCreatureDied() {
        harness.addToBattlefield(player1, new TectonicGiant());
        harness.addToBattlefield(player1, new AnaxHardenedInTheForge());

        harness.castFromHand(player1, new StormsWrath(), "{2}{R}{R}");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Tectonic Giant");
        harness.assertInGraveyard(player1, "Anax, Hardened in the Forge");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Satyr")).hasSize(3)
                .allSatisfy(satyr -> assertThat(bls.canBlock(gd, satyr)).isFalse());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Anax sees another creature die simultaneously even when Anax is removed first")
    void simultaneousDeathsTriggerAfterAnaxIsRemovedFirst() {
        harness.addToBattlefield(player1, new AnaxHardenedInTheForge());
        harness.addToBattlefield(player1, new TectonicGiant());

        harness.castFromHand(player1, new StormsWrath(), "{2}{R}{R}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Satyr")).hasSize(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's creature contributes neither devotion nor a death trigger")
    void opponentsCreatureDoesNotContributeDevotionOrTrigger() {
        Permanent anax = harness.addToBattlefieldAndReturn(player1, new AnaxHardenedInTheForge());
        harness.addToBattlefield(player2, new TectonicGiant());

        assertThat(gqs.getEffectivePower(gd, anax)).isEqualTo(2);
        harness.castFromHand(player1, new StormsWrath(), "{2}{R}{R}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Satyr")).hasSize(1);
        assertThat(findPermanents(player2, "Satyr")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Red cards outside the battlefield do not contribute devotion")
    void devotionIgnoresCardsInOtherZones() {
        Permanent anax = harness.addToBattlefieldAndReturn(player1, new AnaxHardenedInTheForge());
        harness.setHand(player1, List.of(new TectonicGiant()));
        harness.setGraveyard(player1, List.of(new TectonicGiant()));
        harness.setExile(player1, List.of(new TectonicGiant()));

        assertThat(gqs.getEffectivePower(gd, anax)).isEqualTo(2);
    }

    @Test
    @DisplayName("Anax's power-defining ability works in the graveyard")
    void graveyardPowerEqualsOwnersRedDevotion() {
        AnaxHardenedInTheForge anax = new AnaxHardenedInTheForge();
        harness.setGraveyard(player1, List.of(anax));
        harness.addToBattlefield(player1, new TectonicGiant());

        assertThat(gqs.getEffectiveCardPower(gd, anax)).isEqualTo(2);
    }

    private void destroyWithDoomBlade(Permanent target) {
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
