package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.e.EmbodimentOfFlame;
import com.github.laxika.magicalvibes.cards.m.MoonragersSlash;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlameChanneler.class, EmbodimentOfFlame.class, MoonragersSlash.class, Mountain.class})
class FlameChannelerTest extends BaseCardTest {

    @Test
    void frontFaceTransformsWhenYourSpellDealsDamage() {
        Permanent channeler = harness.addToBattlefieldAndReturn(player1, new FlameChanneler());

        castMoonragersSlash();

        assertThat(channeler.isTransformed()).isTrue();
    }

    @Test
    void backFaceGetsAFlameCounterWhenYourSpellDealsDamage() {
        Permanent channeler = addTransformedChanneler();

        castMoonragersSlash();

        assertThat(channeler.getCounterCount(CounterType.FLAME)).isEqualTo(1);
    }

    @Test
    void backFaceAbilityExilesTopCardAndConsumesFlameCounter() {
        Permanent channeler = addTransformedChanneler();
        channeler.setCounterCount(CounterType.FLAME, 1);
        Card topCard = new MoonragersSlash();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(channeler), 0, null, null);
        harness.passBothPriorities();

        assertThat(channeler.getCounterCount(CounterType.FLAME)).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(topCard.getId()));
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
    }

    @Test
    void twoPendingTransformTriggersDoNotTransformBack() {
        Permanent channeler = harness.addToBattlefieldAndReturn(player1, new FlameChanneler());
        harness.setHand(player1, List.of(new MoonragersSlash(), new MoonragersSlash()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(channeler.isTransformed()).isFalse();
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(channeler.isTransformed()).isTrue();
        harness.passBothPriorities();

        assertThat(channeler.isTransformed()).isTrue();
        assertThat(channeler.getCounterCount(CounterType.FLAME)).isZero();
    }

    @Test
    void opponentsSpellDoesNotTransformOrAddCounters() {
        Permanent back = addTransformedChanneler();
        Permanent front = harness.addToBattlefieldAndReturn(player1, new FlameChanneler());
        harness.setHand(player2, List.of(new MoonragersSlash()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(front.isTransformed()).isFalse();
        assertThat(back.getCounterCount(CounterType.FLAME)).isZero();
    }

    @Test
    void activationRequiresFlameCounter() {
        Permanent channeler = addTransformedChanneler();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(channeler), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(channeler.getCounterCount(CounterType.FLAME)).isZero();
    }

    @Test
    void exiledSpellMustBePaidForAndCanGenerateAnotherCounter() {
        Permanent channeler = addTransformedChanneler();
        channeler.setCounterCount(CounterType.FLAME, 1);
        Card topCard = new MoonragersSlash();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, indexOf(channeler), 0, null, null);
        assertThat(channeler.getCounterCount(CounterType.FLAME)).isZero();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castFromExile(player1, topCard.getId(), player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(channeler.getCounterCount(CounterType.FLAME)).isEqualTo(1);
    }
    @Test
    void playPermissionExpiresAtEndOfTurnWithoutReturningCard() {
        Permanent channeler = addTransformedChanneler();
        channeler.setCounterCount(CounterType.FLAME, 1);
        Card topCard = new MoonragersSlash();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, indexOf(channeler), 0, null, null);
        harness.passBothPriorities();

        harness.passUntil(player2, com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }

    @Test
    void emptyLibraryStillConsumesCounterWithoutDrawing() {
        Permanent channeler = addTransformedChanneler();
        channeler.setCounterCount(CounterType.FLAME, 1);
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, indexOf(channeler), 0, null, null);
        harness.passBothPriorities();

        assertThat(channeler.getCounterCount(CounterType.FLAME)).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void exiledLandCanBePlayed() {
        Permanent channeler = addTransformedChanneler();
        channeler.setCounterCount(CounterType.FLAME, 1);
        Card topCard = new Mountain();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, indexOf(channeler), 0, null, null);
        harness.passBothPriorities();

        harness.castFromExile(player1, topCard.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(topCard.getId()));
    }

    private Permanent addTransformedChanneler() {
        Permanent channeler = harness.addToBattlefieldAndReturn(player1, new FlameChanneler());
        castMoonragersSlash();
        return channeler;
    }

    private void castMoonragersSlash() {
        harness.setHand(player1, List.of(new MoonragersSlash()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
