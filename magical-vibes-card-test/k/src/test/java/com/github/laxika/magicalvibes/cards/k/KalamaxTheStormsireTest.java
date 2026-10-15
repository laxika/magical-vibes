package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.i.IntoTheFloodMaw;
import com.github.laxika.magicalvibes.cards.t.Twincast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KalamaxTheStormsire.class, LightningBolt.class, Twincast.class, IntoTheFloodMaw.class})
class KalamaxTheStormsireTest extends BaseCardTest {

    @Test
    @DisplayName("Copies the first instant each turn while Kalamax is tapped and gets a counter")
    void copiesFirstInstantAndGetsCounter() {
        Permanent kalamax = addKalamax(true);
        harness.setHand(player1, List.of(lifeGainInstant()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(kalamax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Copies only the first instant each turn")
    void copiesOnlyFirstInstantEachTurn() {
        Permanent kalamax = addKalamax(true);
        harness.setHand(player1, List.of(lifeGainInstant(), lifeGainInstant()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.castInstant(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(kalamax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not copy an instant while Kalamax is untapped")
    void doesNotCopyWhileUntapped() {
        Permanent kalamax = addKalamax(false);
        harness.setHand(player1, List.of(lifeGainInstant()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(kalamax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotCopyIfUntappedBeforeTriggerResolves() {
        Permanent kalamax = addKalamax(true);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        kalamax.untap();
        resolveAllTriggers();

        harness.assertLife(player2, 17);
        assertThat(kalamax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void firstInstantWhileUntappedStillPreventsCopyingSecondInstant() {
        Permanent kalamax = addKalamax(false);
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        kalamax.tap();
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 14);
        assertThat(kalamax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void canChooseNewTargetForCopyWithoutChangingOriginal() {
        Permanent kalamax = addKalamax(true);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
        assertThat(kalamax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void opponentCastingInstantDoesNotTriggerKalamax() {
        Permanent kalamax = addKalamax(true);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 17);
        assertThat(kalamax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void copyingOpponentsInstantWithAnotherSpellAddsCounterWhileUntapped() {
        Permanent kalamax = harness.addToBattlefieldAndReturn(player2, new KalamaxTheStormsire());
        kalamax.enterUntapped();
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(bolt));
        harness.setHand(player2, List.of(new Twincast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, player1.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bolt.getId());
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        harness.assertLife(player1, 14);
        assertThat(kalamax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void copyPreservesPromisedGift() {
        Permanent kalamax = addKalamax(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KalamaxTheStormsire());
        harness.setHand(player1, List.of(new IntoTheFloodMaw()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstantWithGift(player1, 0, target.getId(), true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertInHand(player2, "Kalamax, the Stormsire");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Fish"))
                .hasSize(1);
        assertThat(kalamax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addKalamax(boolean tapped) {
        Permanent kalamax = harness.addToBattlefieldAndReturn(player1, new KalamaxTheStormsire());
        if (tapped) {
            kalamax.tap();
        } else {
            kalamax.enterUntapped();
        }
        return kalamax;
    }

    private static Card lifeGainInstant() {
        Card card = new Card();
        card.setName("Life Gain");
        card.setType(CardType.INSTANT);
        card.setManaCost("{1}");
        card.addEffect(EffectSlot.SPELL, new GainLifeEffect(1));
        return card;
    }
}
