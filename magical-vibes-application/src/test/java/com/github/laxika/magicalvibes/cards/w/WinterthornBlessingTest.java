package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WinterthornBlessing.class, GrizzlyBears.class})
class WinterthornBlessingTest extends BaseCardTest {

    @Test
    void putsCounterOnYourCreatureAndLocksAnOpponentsCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castFromHand(List.of(ownCreature.getId(), opposingCreature.getId()));

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingCreature.isTapped()).isTrue();
        assertThat(opposingCreature.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    void mayChooseNoTargets() {
        castFromHand(List.of());

        harness.assertInGraveyard(player1, "Winterthorn Blessing");
    }

    @Test
    void enforcesTargetControllers() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WinterthornBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(opposingCreature.getId(), ownCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void flashbackExilesTheSpellAfterResolution() {
        WinterthornBlessing spell = new WinterthornBlessing();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    void mayChooseOnlyYourCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castFromHand(List.of(ownCreature.getId()));

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(ownCreature.getSkipUntapCount()).isZero();
    }

    @Test
    void mayChooseOnlyACreatureYouDoNotControl() {
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castFromHand(List.of(opposingCreature.getId()));

        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposingCreature.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(opposingCreature.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(opposingCreature.isTapped()).isFalse();
    }

    @Test
    void alreadyTappedCreatureSkipsOnlyItsControllersNextUntap() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opposingCreature.tap();
        castFromHand(List.of(ownCreature.getId(), opposingCreature.getId()));

        harness.performUntapStep(player1);
        assertThat(opposingCreature.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(opposingCreature.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(opposingCreature.isTapped()).isFalse();
    }

    @Test
    void opponentCreatureBecomingIllegalDoesNotPreventTheCounter() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WinterthornBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, List.of(ownCreature.getId(), opposingCreature.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(opposingCreature);
        gd.playerBattlefields.get(player1.getId()).add(opposingCreature);
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingCreature.isTapped()).isFalse();
        assertThat(opposingCreature.getSkipUntapCount()).isZero();
    }

    @Test
    void flashbackResolvesBothEffectsAndExilesTheSpell() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        WinterthornBlessing spell = new WinterthornBlessing();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.castFlashback(player1, 0, List.of(ownCreature.getId(), opposingCreature.getId()));
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingCreature.isTapped()).isTrue();
        assertThat(opposingCreature.getSkipUntapCount()).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        harness.assertNotInGraveyard(player1, "Winterthorn Blessing");
    }

    private void castFromHand(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new WinterthornBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveSorcery(player1, 0, targetIds);
    }
}
