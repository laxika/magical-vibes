package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnaxHardenedInTheForge.class, DoomBlade.class, GrizzlyBears.class, ShivanDragon.class})
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

    private void destroyWithDoomBlade(Permanent target) {
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
