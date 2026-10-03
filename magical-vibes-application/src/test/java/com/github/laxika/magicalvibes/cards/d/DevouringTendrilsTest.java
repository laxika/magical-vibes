package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SemestersEnd;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({DevouringTendrils.class, GarrukWildspeaker.class, GrizzlyBears.class, HillGiant.class,
        SemestersEnd.class, Shock.class, Unsummon.class})
class DevouringTendrilsTest extends BaseCardTest {

    @Test
    @DisplayName("Deals the source creature's power to an opposing creature and gains life when it dies")
    void damagesCreatureAndGainsLifeWhenItDies() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DevouringTendrils()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(source.getId(), victim.getId()));
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("The delayed trigger gains life when an opposing planeswalker dies")
    void gainsLifeWhenPlaneswalkerDies() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new DevouringTendrils()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(source.getId(), planeswalker.getId()));
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Garruk Wildspeaker");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Cannot target a permanent you control as the victim")
    void cannotTargetOwnPermanentAsVictim() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent victim = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DevouringTendrils()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(source.getId(), victim.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void gainsLifeOnlyAfterLaterDeathTriggerResolves() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new DevouringTendrils(), new Shock()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(source.getId(), victim.getId()));

        assertThat(victim.getMarkedDamage()).isEqualTo(2);
        assertThat(source.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, victim.getId());

        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.assertLife(player1, 22);
    }

    @Test
    void stillRegistersDeathTriggerWhenSourceLeavesBeforeResolution() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DevouringTendrils(), new Shock()));
        harness.setHand(player2, List.of(new Unsummon()));
        addMana();
        harness.castSorcery(player1, 0, List.of(source.getId(), victim.getId()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, source.getId());
        resolveAllTriggers();

        assertThat(victim.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, victim.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 22);
    }

    @Test
    void victimReturningToHandDoesNotGainLife() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DevouringTendrils()));
        harness.setHand(player2, List.of(new Unsummon()));
        addMana();
        harness.castSorcery(player1, 0, List.of(source.getId(), victim.getId()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, victim.getId());
        resolveAllTriggers();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertLife(player1, 20);
        assertThat(source.getMarkedDamage()).isZero();
    }

    @Test
    void cannotUseAnOpponentsCreatureAsDamageSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DevouringTendrils()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(source.getId(), victim.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void deathTriggerExpiresAtEndOfTurn() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new DevouringTendrils(), new Shock(), new Shock()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, List.of(source.getId(), victim.getId()));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, victim.getId());
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.castAndResolveInstant(player1, 0, victim.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertLife(player1, 20);
    }

    @Test
    void doesNotTrackVictimAfterExileAndReturn() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new DevouringTendrils(), new Shock(), new Shock()));
        harness.setHand(player2, List.of(new SemestersEnd()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, List.of(source.getId(), victim.getId()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player2, 0, List.of(victim.getId()));
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        Permanent returned = findPermanent(player2, "Hill Giant");
        assertThat(returned.getId()).isNotEqualTo(victim.getId());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, returned.getId());
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.castAndResolveInstant(player1, 0, returned.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertLife(player1, 20);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
