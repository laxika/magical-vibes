package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DuergarHedgeMage;
import com.github.laxika.magicalvibes.cards.h.HatchetBully;
import com.github.laxika.magicalvibes.cards.s.SmolderingButcher;
import com.github.laxika.magicalvibes.cards.s.StigmaLasher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FieryBombardment.class, HatchetBully.class, SmolderingButcher.class, StigmaLasher.class,
        DuergarHedgeMage.class})
class FieryBombardmentTest extends BaseCardTest {

    @Test
    void hybridRedSymbolCountsAsOne() {
        harness.addToBattlefield(player1, new FieryBombardment());
        harness.addToBattlefield(player1, new DuergarHedgeMage());
        harness.forceActivePlayer(player1);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.assertInGraveyard(player1, "Duergar Hedge-Mage");
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    void canTargetItsController() {
        harness.addToBattlefield(player1, new FieryBombardment());
        harness.addToBattlefield(player1, new StigmaLasher());
        harness.forceActivePlayer(player1);
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    void opponentsCreatureCannotPaySacrificeCost() {
        harness.addToBattlefield(player1, new FieryBombardment());
        harness.addToBattlefield(player2, new StigmaLasher());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Stigma Lasher");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Deals damage equal to the red mana symbols in the sacrificed creature's cost")
    void dealsDamageEqualToRedSymbols() {
        harness.addToBattlefield(player1, new FieryBombardment());
        harness.addToBattlefield(player1, new StigmaLasher()); // {R}{R} -> 2 red symbols
        harness.addToBattlefield(player1, new SmolderingButcher()); // second creature forces a choice
        UUID lasher = harness.getPermanentId(player1, "Stigma Lasher");
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, lasher);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player1, "Stigma Lasher"); // sacrificed as cost
    }

    @Test
    @DisplayName("A single red pip deals 1 damage")
    void singleRedPipDealsOne() {
        harness.addToBattlefield(player1, new FieryBombardment());
        harness.addToBattlefield(player1, new HatchetBully()); // {3}{R} -> 1 red symbol (only creature: auto-sacrificed)
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Sacrificing a creature with no red pips deals no damage")
    void noRedPipsDealsNoDamage() {
        harness.addToBattlefield(player1, new FieryBombardment());
        harness.addToBattlefield(player1, new SmolderingButcher()); // {3}{B} -> 0 red symbols (only creature: auto-sacrificed)
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Smoldering Butcher");
    }

    @Test
    @DisplayName("Can deal the counted damage to a creature")
    void canTargetCreature() {
        harness.addToBattlefield(player1, new FieryBombardment());
        harness.addToBattlefield(player1, new StigmaLasher());
        harness.addToBattlefield(player2, new StigmaLasher());
        UUID target = harness.getPermanentId(player2, "Stigma Lasher");
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Stigma Lasher");
        harness.assertNotOnBattlefield(player2, "Stigma Lasher");
    }

    @Test
    @DisplayName("Cannot activate without paying the {2} cost")
    void requiresMana() {
        harness.addToBattlefield(player1, new FieryBombardment());
        harness.addToBattlefield(player1, new HatchetBully());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1); // 1 short

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Hatchet Bully");
    }
}
