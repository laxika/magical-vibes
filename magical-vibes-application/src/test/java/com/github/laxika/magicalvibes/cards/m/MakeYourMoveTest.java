package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MakeYourMove.class, AngelicChorus.class, CrawWurm.class, FountainOfYouth.class,
        GrizzlyBears.class, Ornithopter.class})
class MakeYourMoveTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target artifact regardless of power")
    void destroysArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        castOn("Fountain of Youth");

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Destroys a target enchantment regardless of power")
    void destroysEnchantment() {
        harness.addToBattlefield(player2, new AngelicChorus());
        castOn("Angelic Chorus");

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Destroys a target creature with power 4 or greater")
    void destroysLargeCreature() {
        harness.addToBattlefield(player2, new CrawWurm());
        castOn("Craw Wurm");

        harness.assertNotOnBattlefield(player2, "Craw Wurm");
        harness.assertInGraveyard(player2, "Craw Wurm");
    }

    @Test
    @DisplayName("Cannot target a creature with power less than 4")
    void cannotTargetSmallCreature() {
        harness.addToBattlefield(player1, new CrawWurm());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new MakeYourMove()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");
    }

    @Test
    @DisplayName("Destroys an artifact creature with power below four")
    void destroysSmallArtifactCreature() {
        harness.addToBattlefield(player2, new Ornithopter());

        castOn("Ornithopter");

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    @DisplayName("Uses effective power and includes exactly four")
    void destroysCreatureWithExactlyFourPowerFromCounters() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        castOn("Grizzly Bears");

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature with exactly three power")
    void cannotTargetCreatureWithThreePower() {
        harness.addToBattlefield(player1, new CrawWurm());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new MakeYourMove()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");
    }

    @Test
    @DisplayName("Does not destroy a creature whose power drops below four before resolution")
    void rechecksCreaturePowerOnResolution() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new MakeYourMove()));
        addMana();
        harness.castInstant(player1, 0, bears.getId());

        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Make Your Move");
    }

    @Test
    @DisplayName("Can destroy a permanent controlled by its caster")
    void canDestroyOwnPermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new MakeYourMove()));
        addMana();

        harness.castAndResolveInstant(player1, 0, artifact.getId());

        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
        harness.assertInGraveyard(player1, "Fountain of Youth");
    }

    private void castOn(String permanentName) {
        harness.setHand(player1, List.of(new MakeYourMove()));
        addMana();
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, permanentName));
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
