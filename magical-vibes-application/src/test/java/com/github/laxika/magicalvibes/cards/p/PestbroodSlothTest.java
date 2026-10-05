package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PestbroodSloth.class, WrathOfGod.class})
class PestbroodSlothTest extends BaseCardTest {

    @Test
    @DisplayName("When Pestbrood Sloth dies, it creates two Pest tokens")
    void deathCreatesTwoPests() {
        harness.addToBattlefield(player1, new PestbroodSloth());
        destroySloth();

        assertThat(findPermanents(player1, "Pest")).hasSize(2);
    }

    @Test
    @DisplayName("A Pest created by Pestbrood Sloth gains 1 life when it attacks")
    void pestGainsLifeWhenAttacking() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new PestbroodSloth());
        destroySloth();

        Permanent pest = findPermanents(player1, "Pest").getFirst();
        pest.setSummoningSick(false);
        int pestIndex = gd.playerBattlefields.get(player1.getId()).indexOf(pest);

        declareAttackers(player1, List.of(pestIndex));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Each attacking Pest gains life independently")
    void bothPestsGainLifeWhenAttacking() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new PestbroodSloth());
        destroySloth();

        List<Permanent> pests = findPermanents(player1, "Pest");
        pests.forEach(pest -> pest.setSummoningSick(false));
        List<Integer> indices = pests.stream()
                .map(pest -> gd.playerBattlefields.get(player1.getId()).indexOf(pest))
                .toList();
        declareAttackers(player1, indices);
        resolveAllTriggers();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Pest deaths do not gain life")
    void pestDeathsDoNotGainLife() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new PestbroodSloth());
        destroySloth();
        assertThat(findPermanents(player1, "Pest")).hasSize(2);
    }

    @Test
    @DisplayName("The death trigger creates untapped black and green 1/1 Pest creatures")
    void deathTokensHaveCorrectCharacteristics() {
        harness.addToBattlefield(player1, new PestbroodSloth());
        destroySloth();

        assertThat(findPermanents(player1, "Pest")).hasSize(2);
        assertThat(findPermanents(player1, "Pest")).allSatisfy(pest -> {
            assertThat(pest.getCard().isToken()).isTrue();
            assertThat(pest.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(pest.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
            assertThat(pest.getCard().getSubtypes()).containsExactly(CardSubtype.PEST);
            assertThat(gqs.getEffectivePower(gd, pest)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, pest)).isEqualTo(1);
            assertThat(pest.isTapped()).isFalse();
        });

        destroySloth();

        assertThat(findPermanents(player1, "Pest")).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("An opponent's Sloth creates Pests for that opponent")
    void opponentGetsTheirDeathTokensAndAttackLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new PestbroodSloth());
        destroySloth();

        assertThat(findPermanents(player1, "Pest")).isEmpty();
        List<Permanent> pests = findPermanents(player2, "Pest");
        assertThat(pests).hasSize(2);
        Permanent pest = pests.getFirst();
        pest.setSummoningSick(false);
        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(pest)));
        resolveAllTriggers();

        harness.assertLife(player2, 21);
    }

    private void destroySloth() {
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");

        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
