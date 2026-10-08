package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AuguryRaven;
import com.github.laxika.magicalvibes.cards.b.BaithookAngler;
import com.github.laxika.magicalvibes.cards.h.HookHauntDrifter;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.c.Consider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnblinkingObserver.class, AuguryRaven.class, BaithookAngler.class,
        HookHauntDrifter.class, Divination.class, Consider.class})
class UnblinkingObserverTest extends BaseCardTest {

    @Test
    void manaCanCastInstantOrSorcery() {
        addReadyObserver();
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void manaCannotPayForetellSpecialAction() {
        addReadyObserver();
        harness.setHand(player1, List.of(new AuguryRaven()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.foretell(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getDisturbOrInstantSorceryOnlyColored(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void manaCanPayDisturbCost() {
        addReadyObserver();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new BaithookAngler()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).getLast().isTransformed()).isTrue();
    }

    @Test
    void manaCannotCastCreatureEvenWhenTotalManaMatchesCost() {
        addReadyObserver();
        harness.setHand(player1, List.of(new BaithookAngler()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getDisturbOrInstantSorceryOnlyColored(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void restrictedBlueManaCanPayGenericPartOfSorceryCost() {
        addReadyObserver();
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getDisturbOrInstantSorceryOnlyColored(ManaColor.BLUE)).isZero();
    }

    @Test
    void manaCanCastInstant() {
        addReadyObserver();
        harness.setHand(player1, List.of(new Consider()));
        harness.activateAbility(player1, 0, 0, null, null);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getDisturbOrInstantSorceryOnlyColored(ManaColor.BLUE)).isZero();
    }

    @Test
    void manaAbilityResolvesImmediatelyAndRequiresUntappedObserver() {
        Permanent observer = addReadyObserver();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(observer.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId())
                .getDisturbOrInstantSorceryOnlyColored(ManaColor.BLUE)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getDisturbOrInstantSorceryOnlyColored(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void summoningSickObserverCannotActivateManaAbility() {
        Permanent observer = harness.addToBattlefieldAndReturn(player1, new UnblinkingObserver());
        observer.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(observer.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId())
                .getDisturbOrInstantSorceryOnlyColored(ManaColor.BLUE)).isZero();
    }

    private Permanent addReadyObserver() {
        Permanent observer = harness.addToBattlefieldAndReturn(player1, new UnblinkingObserver());
        observer.setSummoningSick(false);
        return observer;
    }
}
