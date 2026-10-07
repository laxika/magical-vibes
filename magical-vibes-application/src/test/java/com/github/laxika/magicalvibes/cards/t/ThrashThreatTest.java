package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DovinGrandArbiter;
import com.github.laxika.magicalvibes.cards.g.GruulGuildgate;
import com.github.laxika.magicalvibes.cards.l.LaviniaAzoriusRenegade;
import com.github.laxika.magicalvibes.cards.r.RampagingRendhorn;
import com.github.laxika.magicalvibes.cards.r.RubblebeltRunner;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({ThrashThreat.class, RubblebeltRunner.class, RampagingRendhorn.class,
        DovinGrandArbiter.class, TerritorialBoar.class, LaviniaAzoriusRenegade.class, GruulGuildgate.class})
class ThrashThreatTest extends BaseCardTest {

    private static final int THRASH = 0;
    private static final int THREAT = 1;
    private static final int FUSE = 2;

    @Test
    @DisplayName("Thrash deals damage equal to the source creature's power to an opposing creature")
    void thrashDealsPowerDamageToCreature() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new RubblebeltRunner());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RampagingRendhorn());

        harness.setHand(player1, List.of(new ThrashThreat()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castModalInstant(player1, 0, THRASH, List.of(source.getId(), target.getId()));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Thrash can target an opposing planeswalker")
    void thrashDealsPowerDamageToPlaneswalker() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new RubblebeltRunner());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new DovinGrandArbiter());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);

        harness.setHand(player1, List.of(new ThrashThreat()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castModalInstant(player1, 0, THRASH, List.of(source.getId(), planeswalker.getId()));
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Threat creates a 4/4 red and green Beast with trample")
    void threatCreatesBeastToken() {
        harness.setHand(player1, List.of(new ThrashThreat()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalSorcery(player1, 0, THREAT, List.of());
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Beast");
        assertThat(countPermanents(player1, "Beast")).isEqualTo(1);
        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(4);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.RED, CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.BEAST);
        assertThat(token.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Thrash // Threat cannot fuse because it has no fuse ability")
    void cannotCastBothHalves() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new RubblebeltRunner());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RampagingRendhorn());

        harness.setHand(player1, List.of(new ThrashThreat()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castModalInstant(
                player1, 0, FUSE, List.of(source.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Thrash cannot target a creature you control")
    void thrashCannotTargetOwnCreature() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new RubblebeltRunner());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TerritorialBoar());

        harness.setHand(player1, List.of(new ThrashThreat()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castModalInstant(
                player1, 0, THRASH, List.of(source.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void threatCannotBeCastDuringCombat() {
        harness.setHand(player1, List.of(new ThrashThreat()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, THREAT, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void thrashCanBeCastDuringCombatAndDoesNotDealDamageBack() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new RubblebeltRunner());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RampagingRendhorn());
        harness.setHand(player1, List.of(new ThrashThreat()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        harness.castModalInstant(player1, 0, THRASH, List.of(source.getId(), target.getId()));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(source.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Rubblebelt Runner");
    }

    @Test
    void thrashUsesPowerAtResolution() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new RubblebeltRunner());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RampagingRendhorn());
        harness.setHand(player1, List.of(new ThrashThreat()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castModalInstant(player1, 0, THRASH, List.of(source.getId(), target.getId()));
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Rampaging Rendhorn");
        harness.assertNotOnBattlefield(player2, "Rampaging Rendhorn");
    }

    @Test
    void thrashCannotUseAnOpposingCreatureAsDamageSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player2, new RubblebeltRunner());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RampagingRendhorn());
        harness.setHand(player1, List.of(new ThrashThreat()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castModalInstant(
                player1, 0, THRASH, List.of(source.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void thrashCannotTargetAPlayer() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new RubblebeltRunner());
        harness.setHand(player1, List.of(new ThrashThreat()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castModalInstant(
                player1, 0, THRASH, List.of(source.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void thrashDealsNoDamageIfSourceLeavesBattlefield() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new RubblebeltRunner());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RampagingRendhorn());
        harness.setHand(player1, List.of(new ThrashThreat()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castModalInstant(player1, 0, THRASH, List.of(source.getId(), target.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(source);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void thrashDealsNoDamageIfSourceChangesController() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new RubblebeltRunner());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RampagingRendhorn());
        harness.setHand(player1, List.of(new ThrashThreat()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castModalInstant(player1, 0, THRASH, List.of(source.getId(), target.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerBattlefields.get(player2.getId()).add(source);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void thrashHasManaValueTwoForLaviniasCastingRestriction() {
        harness.addToBattlefield(player2, new LaviniaAzoriusRenegade());
        harness.addToBattlefield(player1, new GruulGuildgate());
        harness.addToBattlefield(player1, new GruulGuildgate());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new RubblebeltRunner());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RampagingRendhorn());
        harness.setHand(player1, List.of(new ThrashThreat()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castModalInstant(player1, 0, THRASH, List.of(source.getId(), target.getId()));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void threatHasManaValueFourForLaviniasCastingRestriction() {
        harness.addToBattlefield(player2, new LaviniaAzoriusRenegade());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new GruulGuildgate());
        }
        harness.setHand(player1, List.of(new ThrashThreat()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalSorcery(player1, 0, THREAT, List.of());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Beast")).isEqualTo(1);
    }
}
