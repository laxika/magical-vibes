package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.e.EmbodimentOfFlame;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlameChanneler.class, EmbodimentOfFlame.class, LightningBolt.class})
class FlameChannelerTest extends BaseCardTest {

    @Test
    void frontFaceTransformsWhenYourSpellDealsDamage() {
        Permanent channeler = harness.addToBattlefieldAndReturn(player1, new FlameChanneler());

        castLightningBolt();

        assertThat(channeler.isTransformed()).isTrue();
    }

    @Test
    void backFaceGetsAFlameCounterWhenYourSpellDealsDamage() {
        Permanent channeler = addTransformedChanneler();

        castLightningBolt();

        assertThat(channeler.getCounterCount(CounterType.FLAME)).isEqualTo(1);
    }

    @Test
    void backFaceAbilityExilesTopCardAndConsumesFlameCounter() {
        Permanent channeler = addTransformedChanneler();
        channeler.setCounterCount(CounterType.FLAME, 1);
        Card topCard = new LightningBolt();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(channeler), 0, null, null);
        harness.passBothPriorities();

        assertThat(channeler.getCounterCount(CounterType.FLAME)).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(topCard.getId()));
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
    }

    private Permanent addTransformedChanneler() {
        FlameChanneler card = new FlameChanneler();
        Permanent channeler = new Permanent(card);
        channeler.setSummoningSick(false);
        channeler.setCard(card.getBackFaceCard());
        channeler.setTransformed(true);
        gd.playerBattlefields.get(player1.getId()).add(channeler);
        return channeler;
    }

    private void castLightningBolt() {
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
