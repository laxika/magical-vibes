package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CompositeGolem;
import com.github.laxika.magicalvibes.cards.e.EngineeredExplosives;
import com.github.laxika.magicalvibes.cards.f.FurnaceWhelp;
import com.github.laxika.magicalvibes.cards.p.ParadiseMantle;
import com.github.laxika.magicalvibes.cards.r.RelicBarrier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OupheVandals.class, RelicBarrier.class, CompositeGolem.class, FurnaceWhelp.class,
        EngineeredExplosives.class, ParadiseMantle.class})
class OupheVandalsTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an artifact activated ability, destroys its source, and sacrifices itself")
    void countersArtifactActivatedAbilityAndDestroysSource() {
        harness.addToBattlefield(player1, new OupheVandals());
        RelicBarrier relicBarrier = new RelicBarrier();
        harness.addToBattlefield(player2, relicBarrier);
        var targetArtifact = harness.addToBattlefieldAndReturn(player2, new CompositeGolem());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, targetArtifact.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, relicBarrier.getId());
        harness.passBothPriorities();

        assertThat(targetArtifact.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Ouphe Vandals");
        harness.assertInGraveyard(player2, "Relic Barrier");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a mana ability")
    void cannotTargetManaAbility() {
        harness.addToBattlefield(player1, new OupheVandals());
        CompositeGolem compositeGolem = new CompositeGolem();
        harness.addToBattlefield(player2, compositeGolem);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passPriority(player2);

        harness.assertInGraveyard(player2, "Composite Golem");
        assertThat(gd.stack).isEmpty();
        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, compositeGolem.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an activated ability from a non-artifact source")
    void cannotTargetNonArtifactAbility() {
        harness.addToBattlefield(player1, new OupheVandals());
        FurnaceWhelp furnaceWhelp = new FurnaceWhelp();
        harness.addToBattlefield(player2, furnaceWhelp);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, null);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, furnaceWhelp.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, findPermanent(player2, "Furnace Whelp"))).isEqualTo(3);
    }

    @Test
    @DisplayName("Counters an artifact ability whose source is already off the battlefield")
    void countersArtifactAbilityAfterItsSourceLeavesBattlefield() {
        harness.addToBattlefield(player1, new OupheVandals());
        EngineeredExplosives explosives = new EngineeredExplosives();
        harness.addToBattlefield(player2, explosives);
        harness.addToBattlefield(player2, new ParadiseMantle());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, null);
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, explosives.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ouphe Vandals");
        harness.assertInGraveyard(player2, "Engineered Explosives");
        harness.assertOnBattlefield(player2, "Paradise Mantle");
        assertThat(gd.stack).isEmpty();
    }
}
