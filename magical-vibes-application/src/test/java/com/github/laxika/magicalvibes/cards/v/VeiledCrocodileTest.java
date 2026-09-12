package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.cards.t.TolarianWinds;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VeiledCrocodile.class, GorillaWarrior.class, TolarianWinds.class})
class VeiledCrocodileTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes a 4/4 Crocodile creature when a player has no cards in hand")
    void becomesCreatureWhenAPlayerHasNoCardsInHand() {
        harness.setHand(player1, List.of(new GorillaWarrior()));
        harness.setHand(player2, List.of());
        Permanent crocodile = harness.addToBattlefieldAndReturn(player1, new VeiledCrocodile());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, crocodile)).isTrue();
        assertThat(gqs.isEnchantment(gd, crocodile)).isFalse();
        assertThat(crocodile.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(crocodile.getCard().getSubtypes()).containsExactly(CardSubtype.CROCODILE);
        assertThat(crocodile.getCard().getPower()).isEqualTo(4);
        assertThat(crocodile.getCard().getToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not transform while every player has a card in hand")
    void doesNotTransformWhileEveryPlayerHasCardsInHand() {
        harness.setHand(player1, List.of(new GorillaWarrior()));
        harness.setHand(player2, List.of(new GorillaWarrior()));
        Permanent crocodile = harness.addToBattlefieldAndReturn(player1, new VeiledCrocodile());

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, crocodile)).isFalse();
        assertThat(gqs.isEnchantment(gd, crocodile)).isTrue();
    }

    @Test
    @DisplayName("Triggers when a hand is empty momentarily during a spell's resolution")
    void triggersWhenHandIsMomentarilyEmptyDuringResolution() {
        Permanent crocodile = harness.addToBattlefieldAndReturn(player1, new VeiledCrocodile());
        harness.setHand(player1, List.of(new TolarianWinds(), new GorillaWarrior()));
        harness.setLibrary(player1, List.of(new GorillaWarrior()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, crocodile)).isTrue();
    }
}
