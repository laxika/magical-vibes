package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IroassBlessing.class, ChandraNalaar.class, GrizzlyBears.class})
class IroassBlessingTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts the enchanted creature and deals 4 damage to an opposing creature")
    void boostsEnchantedCreatureAndDamagesOpposingCreature() {
        Permanent enchantedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castBlessing(enchantedCreature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, enchantedCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchantedCreature)).isEqualTo(3);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The ETB trigger deals 4 damage to an opposing planeswalker")
    void damagesOpposingPlaneswalker() {
        Permanent enchantedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);

        castBlessing(enchantedCreature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, enchantedCreature)).isEqualTo(3);
    }

    @Test
    @DisplayName("The ETB trigger cannot target a creature controlled by the blessing's controller")
    void rejectsOwnCreatureAsEtbTarget() {
        Permanent enchantedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castBlessing(enchantedCreature.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownTarget.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("Cannot enchant an opponent's creature")
    void cannotEnchantOpposingCreature() {
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> castBlessing(opposingCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertNotOnBattlefield(player1, "Iroas's Blessing");
        harness.assertInHand(player1, "Iroas's Blessing");
    }

    @Test
    @DisplayName("Enters and boosts its creature even when the trigger has no legal target")
    void entersWithoutAnOpposingDamageTarget() {
        Permanent enchantedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castBlessing(enchantedCreature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Iroas's Blessing");
        assertThat(gqs.getEffectivePower(gd, enchantedCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchantedCreature)).isEqualTo(3);
        assertThat(enchantedCreature.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Deals exactly four damage and does not damage the enchanted creature")
    void dealsExactlyFourDamageToASurvivingCreature() {
        Permanent enchantedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        castBlessing(enchantedCreature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(enchantedCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Damage still resolves after the Aura leaves the battlefield")
    void damageResolvesAfterAuraLeaves() {
        Permanent enchantedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castBlessing(enchantedCreature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        Permanent aura = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Iroas's Blessing"));
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, enchantedCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enchantedCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("An Aura whose enchant target disappears never enters or deals damage")
    void missingEnchantTargetPreventsEntryAndDamage() {
        Permanent enchantedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castBlessing(enchantedCreature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(enchantedCreature);
        gd.playerGraveyards.get(player1.getId()).add(enchantedCreature.getCard());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Iroas's Blessing");
        harness.assertInGraveyard(player1, "Iroas's Blessing");
        assertThat(opposingCreature.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castBlessing(java.util.UUID enchantTargetId) {
        harness.setHand(player1, List.of(new IroassBlessing()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, enchantTargetId);
    }
}
