package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KamiOfTransmutation.class, GrizzlyBears.class, Forest.class, LightningBolt.class})
class KamiOfTransmutationTest extends BaseCardTest {

    private static final String ARTIFACT_MODE =
            "Each permanent card in your hand perpetually becomes an artifact in addition to its other types.";
    private static final String ENCHANTMENT_MODE =
            "Each permanent card in your hand perpetually becomes an enchantment in addition to its other types.";

    @Test
    void entersAndPerpetuallyGrantsArtifactToPermanentCardsInHand() {
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        Card bolt = new LightningBolt();
        castKami(List.of(bears, forest, bolt));

        harness.handleListChoice(player1, ARTIFACT_MODE);
        harness.passBothPriorities();

        assertThat(gqs.cardHasType(bears, CardType.ARTIFACT, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasType(forest, CardType.ARTIFACT, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasType(bolt, CardType.ARTIFACT, gd, player1.getId())).isFalse();

        Card later = new GrizzlyBears();
        harness.setHand(player1, List.of(later));
        assertThat(gqs.cardHasType(later, CardType.ARTIFACT, gd, player1.getId())).isFalse();
    }

    @Test
    void leavesAndCanGrantTheOtherTypeToTheSameCards() {
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        castKami(List.of(bears, forest));

        harness.handleListChoice(player1, ARTIFACT_MODE);
        harness.passBothPriorities();

        Permanent kami = findPermanents(player1, "Kami of Transmutation").getFirst();
        kami.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handleListChoice(player1, ENCHANTMENT_MODE);
        harness.passBothPriorities();

        assertThat(gqs.cardHasType(bears, CardType.ARTIFACT, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasType(bears, CardType.ENCHANTMENT, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasType(forest, CardType.ARTIFACT, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasType(forest, CardType.ENCHANTMENT, gd, player1.getId())).isTrue();
    }

    private void castKami(List<Card> handCards) {
        List<Card> hand = new ArrayList<>(handCards);
        hand.addFirst(new KamiOfTransmutation());
        harness.setHand(player1, hand);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
