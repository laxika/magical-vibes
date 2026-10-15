package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.b.BoggartShenanigans;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OakgnarlWarrior;
import com.github.laxika.magicalvibes.cards.s.SkirkProspector;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FodderLaunch.class, AvatarOfMight.class, GrizzlyBears.class, SkirkProspector.class,
        BoggartShenanigans.class, OakgnarlWarrior.class, WoodlandChangeling.class})
class FodderLaunchTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature -5/-5 and deals 5 to its controller")
    void minusFiveAndDamageToController() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new SkirkProspector());

        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new FodderLaunch()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), goblin.getId());
        harness.passBothPriorities();

        // -5/-5 kills the 2/2
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        // Sacrificed Goblin went to its controller's graveyard
        harness.assertInGraveyard(player1, "Skirk Prospector");
        // Controller of the targeted creature takes 5 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("The -5/-5 wears off at end of turn but the damage remains")
    void minusFiveWearsOff() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new SkirkProspector());

        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());

        harness.setHand(player1, List.of(new FodderLaunch()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), goblin.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-5);
        assertThat(target.getToughnessModifier()).isEqualTo(-5);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot cast without a Goblin to sacrifice")
    void cannotCastWithoutGoblin() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new FodderLaunch()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, target.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("A noncreature Goblin permanent can pay the sacrifice cost")
    void canSacrificeKindredGoblinEnchantment() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new BoggartShenanigans());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OakgnarlWarrior());
        harness.setHand(player1, List.of(new FodderLaunch()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), goblin.getId());

        harness.assertNotOnBattlefield(player1, "Boggart Shenanigans");
        harness.assertInGraveyard(player1, "Boggart Shenanigans");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Oakgnarl Warrior");
        assertThat(target.getPowerModifier()).isEqualTo(-5);
        assertThat(target.getToughnessModifier()).isEqualTo(-5);
        harness.assertLife(player2, 15);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("A changeling can pay the Goblin sacrifice cost and your own creature can be targeted")
    void canSacrificeChangelingAndDamageOwnController() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OakgnarlWarrior());
        harness.setHand(player1, List.of(new FodderLaunch()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), goblin.getId());
        harness.assertInGraveyard(player1, "Woodland Changeling");
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-5);
        assertThat(target.getToughnessModifier()).isEqualTo(-5);
        harness.assertLife(player1, 15);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Sacrificing the targeted Goblin makes the spell fail to resolve without dealing damage")
    void sacrificedTargetPreventsAllEffects() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling());
        harness.setHand(player1, List.of(new FodderLaunch()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorceryWithSacrifice(player1, 0, goblin.getId(), goblin.getId());
        harness.assertInGraveyard(player1, "Woodland Changeling");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fodder Launch");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A non-Goblin creature cannot pay the sacrifice cost")
    void cannotSacrificeNonGoblin() {
        Permanent nonGoblin = harness.addToBattlefieldAndReturn(player1, new OakgnarlWarrior());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WoodlandChangeling());
        harness.setHand(player1, List.of(new FodderLaunch()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(
                player1, 0, target.getId(), nonGoblin.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Oakgnarl Warrior");
        harness.assertInHand(player1, "Fodder Launch");
        assertThat(gd.stack).isEmpty();
    }
}
