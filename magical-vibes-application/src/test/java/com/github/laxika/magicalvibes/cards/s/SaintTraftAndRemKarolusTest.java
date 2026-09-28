package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SaintTraftAndRemKarolus.class, StokeTheFlames.class})
class SaintTraftAndRemKarolusTest extends BaseCardTest {

    @Test
    @DisplayName("Its first three tap-trigger resolutions create the Human, Spirit, and Angel tokens")
    void tapTriggerCreatesProgressiveTokens() {
        Permanent saintTraftAndRemKarolus = harness.addToBattlefieldAndReturn(
                player1, new SaintTraftAndRemKarolus());

        tap(saintTraftAndRemKarolus);
        resolveAllTriggers();
        Permanent human = findPermanent(player1, "Human");
        assertThat(human.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(human.getCard().getSubtypes()).containsExactly(CardSubtype.HUMAN);
        assertThat(human.getEffectivePower()).isEqualTo(1);
        assertThat(human.getEffectiveToughness()).isEqualTo(1);

        saintTraftAndRemKarolus.untap();
        tap(saintTraftAndRemKarolus);
        resolveAllTriggers();
        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(spirit.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
        assertThat(spirit.hasKeyword(Keyword.FLYING)).isTrue();

        saintTraftAndRemKarolus.untap();
        tap(saintTraftAndRemKarolus);
        resolveAllTriggers();
        Permanent angel = findPermanent(player1, "Angel");
        assertThat(angel.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(angel.getCard().getSubtypes()).containsExactly(CardSubtype.ANGEL);
        assertThat(angel.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(angel.getEffectivePower()).isEqualTo(4);
        assertThat(angel.getEffectiveToughness()).isEqualTo(4);

        saintTraftAndRemKarolus.untap();
        tap(saintTraftAndRemKarolus);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Human")).hasSize(1);
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        assertThat(findPermanents(player1, "Angel")).hasSize(1);
    }

    @Test
    @DisplayName("Casting a convoke spell untaps it")
    void convokeSpellUntapsSource() {
        Permanent saintTraftAndRemKarolus = harness.addToBattlefieldAndReturn(
                player1, new SaintTraftAndRemKarolus());
        harness.setHand(player1, List.of(new StokeTheFlames()));
        harness.addMana(player1, ManaColor.RED, 3);

        gs.playCard(gd, player1, 0, 0, player2.getId(), null, List.of(),
                List.of(saintTraftAndRemKarolus.getId()));
        assertThat(saintTraftAndRemKarolus.isTapped()).isTrue();

        resolveAllTriggers();

        assertThat(saintTraftAndRemKarolus.isTapped()).isFalse();
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
