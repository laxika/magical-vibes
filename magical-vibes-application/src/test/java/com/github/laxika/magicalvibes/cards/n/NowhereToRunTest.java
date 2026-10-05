package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.CarnageTyrant;
import com.github.laxika.magicalvibes.cards.f.FrostTitan;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KnightOfGrace;
import com.github.laxika.magicalvibes.cards.r.RoyalTreatment;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SireOfSevenDeaths;
import com.github.laxika.magicalvibes.cards.w.WitheringTorment;
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

@CardUsed({NowhereToRun.class, GrizzlyBears.class, CarnageTyrant.class, Shock.class,
        SireOfSevenDeaths.class, FrostTitan.class, WitheringTorment.class, KnightOfGrace.class,
        RoyalTreatment.class})
class NowhereToRunTest extends BaseCardTest {

    @Test
    @DisplayName("Its enters-the-battlefield ability gives an opponent's creature -3/-3")
    void weakensTargetCreatureWhenItEnters() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NowhereToRun()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("Its controller can target an opponent's hexproof creature")
    void controllerCanTargetOpponentHexproofCreature() {
        harness.addToBattlefield(player1, new NowhereToRun());
        Permanent tyrant = harness.addToBattlefieldAndReturn(player2, new CarnageTyrant());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, tyrant.getId());

        assertThat(tyrant.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("It prevents ward from triggering on an opponent's creature")
    void preventsOpponentCreatureWardTrigger() {
        harness.addToBattlefield(player1, new NowhereToRun());
        Permanent sire = harness.addToBattlefieldAndReturn(player2, new SireOfSevenDeaths());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, sire.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(sire.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("It does not suppress unrelated counter-unless target triggers")
    void doesNotSuppressNonWardCounterTrigger() {
        harness.addToBattlefield(player1, new NowhereToRun());
        Permanent titan = harness.addToBattlefieldAndReturn(player2, new FrostTitan());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, titan.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shock");
        assertThat(titan.getMarkedDamage()).isZero();
    }

    @Test
    void canEnterAtInstantSpeedAndWeakenHexproofCreatureUntilEndOfTurn() {
        Permanent tyrant = harness.addToBattlefieldAndReturn(player2, new CarnageTyrant());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new NowhereToRun()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, tyrant.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, tyrant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, tyrant)).isEqualTo(3);
        harness.passUntil(TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, tyrant)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, tyrant)).isEqualTo(6);
    }

    @Test
    void ownEnterTriggerDoesNotTriggerWard() {
        Permanent sire = harness.addToBattlefieldAndReturn(player2, new SireOfSevenDeaths());
        harness.setHand(player1, List.of(new NowhereToRun()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, sire.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gqs.getEffectiveToughness(gd, sire)).isEqualTo(4);
        harness.assertLife(player1, 20);
    }

    @Test
    void allowsOwnEnterTriggerToTargetHexproofFromBlack() {
        Permanent knight = harness.addToBattlefieldAndReturn(player2, new KnightOfGrace());
        harness.setHand(player1, List.of(new NowhereToRun()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, knight.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Knight of Grace");
    }

    @Test
    void allowsBlackSpellToTargetHexproofFromBlack() {
        harness.addToBattlefield(player1, new NowhereToRun());
        Permanent knight = harness.addToBattlefieldAndReturn(player2, new KnightOfGrace());
        harness.setHand(player1, List.of(new WitheringTorment()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, knight.getId());

        harness.assertInGraveyard(player2, "Knight of Grace");
        harness.assertLife(player1, 18);
    }

    @Test
    void canResolveWithoutAnOpposingCreature() {
        harness.setHand(player1, List.of(new NowhereToRun()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Nowhere to Run");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotSuppressNonWardCounterTriggerWhenCreatureAlsoHasWard() {
        Permanent titan = harness.addToBattlefieldAndReturn(player2, new FrostTitan());
        harness.setHand(player2, List.of(new RoyalTreatment()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0, titan.getId());
        harness.addToBattlefield(player1, new NowhereToRun());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, titan.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Shock");
        assertThat(titan.getMarkedDamage()).isZero();
    }

    @Test
    void doesNotLetOpponentTargetItsControllersHexproofCreature() {
        harness.addToBattlefield(player1, new NowhereToRun());
        Permanent tyrant = harness.addToBattlefieldAndReturn(player1, new CarnageTyrant());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, tyrant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void hexproofMakesTargetIllegalWhenNowhereToRunLeavesBeforeResolution() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new NowhereToRun());
        Permanent tyrant = harness.addToBattlefieldAndReturn(player2, new CarnageTyrant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, tyrant.getId());
        harness.setHand(player2, List.of(new WitheringTorment()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, enchantment.getId());
        resolveAllTriggers();

        assertThat(tyrant.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player1, "Nowhere to Run");
    }

    @Test
    void wardDoesNotTriggerRetroactivelyWhenNowhereToRunLeaves() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new NowhereToRun());
        Permanent sire = harness.addToBattlefieldAndReturn(player2, new SireOfSevenDeaths());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, sire.getId());
        harness.setHand(player2, List.of(new WitheringTorment()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, enchantment.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(sire.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player1, 20);
    }
}
