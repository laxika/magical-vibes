package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BeamingDefiance;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IntoTheVoid;
import com.github.laxika.magicalvibes.cards.l.LullmagesDomination;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.cards.p.PillardropWarden;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KillianInkDuelist.class, GrizzlyBears.class, IntoTheVoid.class, MindRot.class,
        LullmagesDomination.class, PillardropWarden.class, BeamingDefiance.class})
class KillianInkDuelistTest extends BaseCardTest {

    @Test
    @DisplayName("Reduces a spell targeting creatures by two generic mana, once for multiple targets")
    void reducesCreatureTargetingSpellOnce() {
        harness.addToBattlefield(player1, new KillianInkDuelist());
        Permanent firstBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IntoTheVoid()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, List.of(firstBear.getId(), secondBear.getId()));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Grizzly Bears"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Does not reduce a spell targeting a player")
    void doesNotReducePlayerTargetingSpell() {
        harness.addToBattlefield(player1, new KillianInkDuelist());
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reducesSpellTargetingOwnCreature() {
        Permanent killian = harness.addToBattlefieldAndReturn(player1, new KillianInkDuelist());
        harness.setHand(player1, List.of(new IntoTheVoid()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(killian.getId()));

        harness.assertInHand(player1, "Killian, Ink Duelist");
        harness.assertNotOnBattlefield(player1, "Killian, Ink Duelist");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void doesNotReduceOpponentsSpell() {
        harness.addToBattlefield(player2, new KillianInkDuelist());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IntoTheVoid()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotReduceSpellWithNoChosenTargets() {
        harness.addToBattlefield(player1, new KillianInkDuelist());
        harness.setHand(player1, List.of(new IntoTheVoid()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotReplaceColoredManaWithGenericMana() {
        Permanent killian = harness.addToBattlefieldAndReturn(player1, new KillianInkDuelist());
        harness.setHand(player1, List.of(new IntoTheVoid()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(killian.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reducesInstantWithoutReducingItsColoredRequirement() {
        Permanent killian = harness.addToBattlefieldAndReturn(player1, new KillianInkDuelist());
        harness.setHand(player1, List.of(new BeamingDefiance()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, killian.getId());

        assertThat(killian.getPowerModifier()).isEqualTo(2);
        assertThat(killian.getToughnessModifier()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player1, "Beaming Defiance");
    }

    @Test
    void combinesDiscountWithSpellsOwnTargetBasedDiscount() {
        harness.addToBattlefield(player1, new KillianInkDuelist());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PillardropWarden());
        harness.setGraveyard(player2, List.of(new PillardropWarden(), new PillardropWarden(),
                new PillardropWarden(), new PillardropWarden(), new PillardropWarden(),
                new PillardropWarden(), new PillardropWarden(), new PillardropWarden()));
        harness.setHand(player1, List.of(new LullmagesDomination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 4, target.getId());

        harness.assertOnBattlefield(player1, "Pillardrop Warden");
        harness.assertNotOnBattlefield(player2, "Pillardrop Warden");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
