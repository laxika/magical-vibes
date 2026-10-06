package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({HowlOfTheNightPack.class, Forest.class, Island.class})
class HowlOfTheNightPackTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one 2/2 Wolf token per Forest controlled")
    void createsTokensPerForest() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        harness.setHand(player1, List.of(new HowlOfTheNightPack()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        List<Permanent> wolves = findPermanents(player1, "Wolf");
        assertThat(wolves).hasSize(3);
        for (Permanent wolf : wolves) {
            assertThat(wolf.getCard().getPower()).isEqualTo(2);
            assertThat(wolf.getCard().getToughness()).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("Creates no tokens when controller has no Forests")
    void noTokensWhenNoForests() {
        harness.addToBattlefield(player1, new Island());

        harness.setHand(player1, List.of(new HowlOfTheNightPack()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        List<Permanent> wolves = findPermanents(player1, "Wolf");
        assertThat(wolves).isEmpty();
    }

    @Test
    @DisplayName("Does not count opponent's Forests")
    void doesNotCountOpponentForests() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());

        harness.setHand(player1, List.of(new HowlOfTheNightPack()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        List<Permanent> wolves = findPermanents(player1, "Wolf");
        assertThat(wolves).hasSize(1);
    }

    @Test
    @DisplayName("Counts Forests when the spell resolves")
    void countsForestsAtResolution() {
        harness.addToBattlefield(player1, new Forest());

        harness.setHand(player1, List.of(new HowlOfTheNightPack()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castSorcery(player1, 0, 0);
        harness.addToBattlefield(player1, new Forest());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Wolf")).hasSize(2);
    }

    @Test
    @DisplayName("Creates green 2/2 Wolf creature tokens")
    void createsGreenWolfCreatureTokens() {
        harness.addToBattlefield(player1, new Forest());

        harness.setHand(player1, List.of(new HowlOfTheNightPack()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent wolf = findPermanent(player1, "Wolf");
        assertThat(wolf.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(wolf.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(wolf.getCard().getSubtypes()).containsExactly(CardSubtype.WOLF);
        assertThat(wolf.getEffectivePower()).isEqualTo(2);
        assertThat(wolf.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts tapped Forests but not Forest cards outside the battlefield")
    void countsOnlyBattlefieldForestsRegardlessOfTappedState() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new HowlOfTheNightPack(), new Forest()));
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setExile(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(findPermanents(player1, "Wolf")).hasSize(1);
        Permanent wolf = findPermanent(player1, "Wolf");
        assertThat(wolf.getCard().isToken()).isTrue();
        assertThat(wolf.isTapped()).isFalse();
        assertThat(wolf.isSummoningSick()).isTrue();
        assertThat(findPermanents(player2, "Wolf")).isEmpty();
    }
}
