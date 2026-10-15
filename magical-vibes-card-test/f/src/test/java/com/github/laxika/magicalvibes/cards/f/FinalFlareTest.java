package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.n.NyxbornColossus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FinalFlare.class, GrizzlyBears.class, GloriousAnthem.class, Plains.class, NyxbornColossus.class})
class FinalFlareTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifice is paid before resolution and exactly five damage is dealt")
    void paysSacrificeBeforeResolutionAndDealsExactlyFive() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new NyxbornColossus());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyxbornColossus());
        harness.setHand(player1, List.of(new FinalFlare()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());

        harness.assertInGraveyard(player1, "Nyxborn Colossus");
        harness.assertNotOnBattlefield(player1, "Nyxborn Colossus");
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Nyxborn Colossus");
        assertThat(target.getMarkedDamage()).isEqualTo(5);
        harness.assertInGraveyard(player1, "Final Flare");
    }

    @Test
    @DisplayName("Can target the creature sacrificed to pay the additional cost")
    void canTargetSacrificedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NyxbornColossus());
        harness.setHand(player1, List.of(new FinalFlare()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstantWithSacrifice(player1, 0, creature.getId(), creature.getId());

        harness.assertInGraveyard(player1, "Nyxborn Colossus");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Final Flare");
        harness.assertNotOnBattlefield(player1, "Nyxborn Colossus");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature")
    void cannotSacrificeOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new NyxbornColossus());
        harness.addToBattlefield(player1, new NyxbornColossus());
        harness.setHand(player1, List.of(new FinalFlare()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0,
                opponentCreature.getId(), opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Nyxborn Colossus");
        harness.assertOnBattlefield(player1, "Nyxborn Colossus");
        harness.assertInHand(player1, "Final Flare");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrifices a creature and deals 5 damage to target creature")
    void sacrificesCreatureAndDealsFiveDamage() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FinalFlare()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrifices an enchantment and deals 5 damage to target creature")
    void sacrificesEnchantmentAndDealsFiveDamage() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FinalFlare()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Glorious Anthem");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot cast without a creature or enchantment to sacrifice")
    void cannotCastWithoutMatchingPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FinalFlare()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, target.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a land as the sacrifice and as the target")
    void rejectsLandAsSacrificeOrTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FinalFlare()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, creature.getId(), land.getId()))
                .isInstanceOf(IllegalStateException.class);

        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FinalFlare()));
        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, land.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
