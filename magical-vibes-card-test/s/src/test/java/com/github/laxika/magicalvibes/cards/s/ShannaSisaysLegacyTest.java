package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AcademyJourneymage;
import com.github.laxika.magicalvibes.cards.r.RoyalAssassin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShannaSisaysLegacy.class, GrizzlyBears.class, RoyalAssassin.class, Shock.class,
        AcademyJourneymage.class})
class ShannaSisaysLegacyTest extends BaseCardTest {

    @Test
    @DisplayName("Base 0/0 plus herself = 1/1 with only Shanna on the battlefield")
    void baseStatsWithOnlyShanna() {
        harness.addToBattlefield(player1, new ShannaSisaysLegacy());

        Permanent shanna = findPermanent(player1, "Shanna, Sisay's Legacy");
        // 0/0 base + 1/1 from herself = 1/1
        assertThat(gqs.getEffectivePower(gd, shanna)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, shanna)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets +1/+1 for each creature you control including herself")
    void boostsWithAdditionalCreatures() {
        harness.addToBattlefield(player1, new ShannaSisaysLegacy());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent shanna = findPermanent(player1, "Shanna, Sisay's Legacy");
        // 0/0 base + 2/2 from two creatures (herself + Grizzly Bears) = 2/2
        assertThat(gqs.getEffectivePower(gd, shanna)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, shanna)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets +3/+3 with three creatures you control")
    void boostsWithThreeCreatures() {
        harness.addToBattlefield(player1, new ShannaSisaysLegacy());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent shanna = findPermanent(player1, "Shanna, Sisay's Legacy");
        // 0/0 base + 3/3 from three creatures = 3/3
        assertThat(gqs.getEffectivePower(gd, shanna)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, shanna)).isEqualTo(3);
    }

    @Test
    @DisplayName("Opponent's creatures do not contribute to the bonus")
    void opponentCreaturesDontCount() {
        harness.addToBattlefield(player1, new ShannaSisaysLegacy());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent shanna = findPermanent(player1, "Shanna, Sisay's Legacy");
        // Only Shanna herself counts = 1/1
        assertThat(gqs.getEffectivePower(gd, shanna)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, shanna)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bonus is removed when a creature leaves the battlefield")
    void bonusRemovedWhenCreatureLeaves() {
        harness.addToBattlefield(player1, new ShannaSisaysLegacy());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent shanna = findPermanent(player1, "Shanna, Sisay's Legacy");
        assertThat(gqs.getEffectivePower(gd, shanna)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Grizzly Bears"));

        assertThat(gqs.getEffectivePower(gd, shanna)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, shanna)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent cannot target Shanna with activated abilities")
    void opponentCannotTargetWithAbilities() {
        harness.addToBattlefield(player1, new ShannaSisaysLegacy());
        // Tap Shanna so Royal Assassin can target her (Royal Assassin targets tapped creatures)
        Permanent shanna = findPermanent(player1, "Shanna, Sisay's Legacy");
        shanna.tap();

        // Give player2 a Royal Assassin (T: Destroy target tapped creature)
        Permanent assassin = harness.addToBattlefieldAndReturn(player2, new RoyalAssassin());
        assassin.setSummoningSick(false);

        // Also add a valid tapped target so the ability is activatable
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.tap();

        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, shanna.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Opponent can still target Shanna with spells")
    void opponentCanTargetWithSpells() {
        harness.addToBattlefield(player1, new ShannaSisaysLegacy());
        // Add another creature so Shanna is big enough to survive... actually Shock does 2 damage
        // and Shanna with just herself is 1/1, so she'll die, but the targeting should succeed
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        // Now Shanna is 3/3

        Permanent shanna = findPermanent(player1, "Shanna, Sisay's Legacy");

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.passPriority(player1);

        // This should NOT throw — spells can target Shanna
        harness.castAndResolveInstant(player2, 0, shanna.getId());

        // Shock dealt 2 damage to Shanna (3/3), she should have 2 damage
        assertThat(shanna.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Controller can target Shanna with their own abilities")
    void controllerCanTargetWithOwnAbilities() {
        harness.addToBattlefield(player1, new ShannaSisaysLegacy());
        Permanent shanna = findPermanent(player1, "Shanna, Sisay's Legacy");
        shanna.tap();

        // Give player1 a Royal Assassin (T: Destroy target tapped creature)
        Permanent assassin = harness.addToBattlefieldAndReturn(player1, new RoyalAssassin());
        assassin.setSummoningSick(false);

        // Controller should be able to target their own Shanna with abilities
        // Royal Assassin targets tapped creatures — Shanna is tapped
        // This should NOT throw
        harness.activateAbility(player1, 1, null, shanna.getId());
        harness.passBothPriorities();

        // Shanna should have been destroyed by Royal Assassin
        harness.assertNotOnBattlefield(player1, "Shanna, Sisay's Legacy");
    }

    @Test
    @DisplayName("Opponent's enter-the-battlefield ability has no legal target when only Shanna is available")
    void opponentTriggeredAbilityCannotTargetShanna() {
        harness.addToBattlefield(player2, new ShannaSisaysLegacy());
        harness.setHand(player1, List.of(new AcademyJourneymage()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Academy Journeymage");
        harness.assertOnBattlefield(player2, "Shanna, Sisay's Legacy");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Losing another creature can make marked damage lethal to Shanna")
    void shrinkingAfterAnotherCreatureDiesMakesDamageLethal() {
        Permanent shanna = harness.addToBattlefieldAndReturn(player1, new ShannaSisaysLegacy());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, shanna.getId());
        harness.assertOnBattlefield(player1, "Shanna, Sisay's Legacy");
        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertNotOnBattlefield(player1, "Shanna, Sisay's Legacy");
        harness.assertInGraveyard(player1, "Shanna, Sisay's Legacy");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }
}
