package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DireFleetCaptain;
import com.github.laxika.magicalvibes.cards.i.IxallisKeeper;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FieryCannonade.class, DireFleetCaptain.class, IxallisKeeper.class, ColossalDreadmaw.class})
class FieryCannonadeTest extends BaseCardTest {

    @Test
    @DisplayName("Fiery Cannonade kills non-Pirate creatures with toughness 2 or less on both sides")
    void killsNonPirateCreatures() {
        harness.addToBattlefield(player1, new IxallisKeeper());
        harness.addToBattlefield(player2, new IxallisKeeper());
        harness.setHand(player1, List.of(new FieryCannonade()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0);

        harness.assertNotOnBattlefield(player1, "Ixalli's Keeper");
        harness.assertNotOnBattlefield(player2, "Ixalli's Keeper");
    }

    @Test
    @DisplayName("Fiery Cannonade does not damage Pirate creatures")
    void doesNotDamagePirateCreatures() {
        harness.addToBattlefield(player2, new DireFleetCaptain());
        harness.setHand(player1, List.of(new FieryCannonade()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0);

        harness.assertOnBattlefield(player2, "Dire Fleet Captain");
    }

    @Test
    @DisplayName("Fiery Cannonade damages non-Pirate creatures but leaves Pirate creatures unharmed")
    void selectivelyDamages() {
        harness.addToBattlefield(player2, new DireFleetCaptain());
        harness.addToBattlefield(player2, new IxallisKeeper());
        harness.setHand(player1, List.of(new FieryCannonade()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0);

        harness.assertOnBattlefield(player2, "Dire Fleet Captain");
        harness.assertNotOnBattlefield(player2, "Ixalli's Keeper");
    }

    @Test
    @DisplayName("Fiery Cannonade does not deal damage to players")
    void doesNotDamagePlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new FieryCannonade()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Surviving non-Pirates take exactly 2 damage and Pirates on both sides take none")
    void marksDamageOnlyOnNonPirates() {
        Permanent ownDinosaur = harness.addToBattlefieldAndReturn(player1, new ColossalDreadmaw());
        Permanent opposingDinosaur = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        Permanent ownPirate = harness.addToBattlefieldAndReturn(player1, new DireFleetCaptain());
        Permanent opposingPirate = harness.addToBattlefieldAndReturn(player2, new DireFleetCaptain());
        harness.setHand(player1, List.of(new FieryCannonade()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0);

        harness.assertOnBattlefield(player1, "Colossal Dreadmaw");
        harness.assertOnBattlefield(player2, "Colossal Dreadmaw");
        harness.assertOnBattlefield(player1, "Dire Fleet Captain");
        harness.assertOnBattlefield(player2, "Dire Fleet Captain");
        assertThat(ownDinosaur.getMarkedDamage()).isEqualTo(2);
        assertThat(opposingDinosaur.getMarkedDamage()).isEqualTo(2);
        assertThat(ownPirate.getMarkedDamage()).isZero();
        assertThat(opposingPirate.getMarkedDamage()).isZero();
    }
}

