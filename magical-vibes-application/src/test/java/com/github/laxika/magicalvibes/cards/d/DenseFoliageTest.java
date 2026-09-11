package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeavyBallista;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SamiteHealer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DenseFoliage.class, GrizzlyBears.class, Shock.class, HeavyBallista.class, SamiteHealer.class})
class DenseFoliageTest extends BaseCardTest {

    @Test
    @DisplayName("Spells cannot target a creature while Dense Foliage is out")
    void spellsCannotTargetCreatures() {
        harness.addToBattlefield(player1, new DenseFoliage());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be the target of spells");
    }

    @Test
    @DisplayName("Spells can still target players while Dense Foliage is out")
    void spellsCanStillTargetPlayers() {
        harness.addToBattlefield(player1, new DenseFoliage());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Without Dense Foliage a spell can target the creature")
    void spellsTargetCreaturesWithoutDenseFoliage() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Dense Foliage also prevents spells from targeting your own creatures")
    void spellsCannotTargetYourOwnCreatures() {
        harness.addToBattlefield(player1, new DenseFoliage());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be the target of spells");
    }

    @Test
    @CardUsed(Opalescence.class)
    @DisplayName("Dense Foliage still protects itself when it becomes a creature")
    void creatureVersionCannotBeTargetedBySpells() {
        Permanent denseFoliage = harness.addToBattlefieldAndReturn(player1, new DenseFoliage());
        harness.addToBattlefield(player1, new Opalescence());

        assertThat(gqs.isCreature(gd, denseFoliage)).isTrue();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, denseFoliage.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be the target of spells");
    }

    @Test
    @DisplayName("Activated abilities can still target creatures while Dense Foliage is out")
    void abilitiesCanStillTargetCreatures() {
        harness.addToBattlefield(player1, new DenseFoliage());

        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);

        addCreatureReady(player2, new HeavyBallista());
        addCreatureReady(player2, new SamiteHealer());

        harness.activateAbility(player2, 1, null, bears.getId());
        harness.passBothPriorities();
        harness.activateAbility(player2, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
