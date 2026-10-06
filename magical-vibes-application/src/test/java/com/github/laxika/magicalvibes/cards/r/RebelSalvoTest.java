package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CopperLonglegs;
import com.github.laxika.magicalvibes.cards.d.DarksteelGargoyle;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HauntedPlateMail;
import com.github.laxika.magicalvibes.cards.h.HexgoldHalberd;
import com.github.laxika.magicalvibes.cards.l.LukkaBoundToRuin;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RebelSalvo.class, DarksteelGargoyle.class, GrizzlyBears.class, HauntedPlateMail.class,
        CopperLonglegs.class, HexgoldHalberd.class, LukkaBoundToRuin.class})
class RebelSalvoTest extends BaseCardTest {

    @Test
    void affinityForEquipmentReducesCostAndRemovesIndestructibleAfterDealingDamage() {
        harness.addToBattlefield(player1, new HauntedPlateMail());
        harness.addToBattlefield(player2, new DarksteelGargoyle());
        harness.setHand(player1, List.of(new RebelSalvo()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Darksteel Gargoyle"));
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Darksteel Gargoyle");
        harness.assertInGraveyard(player2, "Darksteel Gargoyle");
    }

    @Test
    void affinityCountsOnlyEquipmentControlledByTheSpellController() {
        harness.addToBattlefield(player2, new HauntedPlateMail());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RebelSalvo()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void multipleUnattachedEquipmentReduceOnlyTheGenericCost() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new HexgoldHalberd());
        }
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());
        harness.setHand(player1, List.of(new RebelSalvo()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player2, "Copper Longlegs");
    }

    @Test
    void equipmentCannotRemoveTheRedManaRequirement() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new HexgoldHalberd());
        }
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());
        harness.setHand(player1, List.of(new RebelSalvo()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Copper Longlegs");
        harness.assertInHand(player1, "Rebel Salvo");
    }

    @Test
    void nonEquipmentArtifactsDoNotReduceTheCost() {
        harness.addToBattlefield(player1, new DarksteelGargoyle());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());
        harness.setHand(player1, List.of(new RebelSalvo()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void dealsExactlyFiveDamageToAPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LukkaBoundToRuin());
        target.setCounterCount(CounterType.LOYALTY, 7);
        harness.setHand(player1, List.of(new RebelSalvo()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Lukka, Bound to Ruin");
        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void canTargetTheControllersOwnCreatureWithoutEquipment() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        harness.setHand(player1, List.of(new RebelSalvo()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player1, "Copper Longlegs");
    }

    @Test
    void cannotTargetAPlayerOrNonCreatureEquipment() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new HexgoldHalberd());
        harness.setHand(player1, List.of(new RebelSalvo()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, equipment.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Rebel Salvo");
    }

    @Test
    void removesIndestructibleEvenWhenDamageIsPreventedAndRestoresItAfterCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DarksteelGargoyle());
        target.setDamagePreventionShield(5);
        harness.setHand(player1, List.of(new RebelSalvo()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Darksteel Gargoyle");
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.assertOnBattlefield(player2, "Darksteel Gargoyle");
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
    }
}
