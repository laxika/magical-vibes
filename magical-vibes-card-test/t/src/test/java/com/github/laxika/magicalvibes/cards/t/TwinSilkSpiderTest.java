package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.ForceOfDespair;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TwinSilkSpider.class, ForceOfDespair.class})
class TwinSilkSpiderTest extends BaseCardTest {

    @Test
    void enteringTheBattlefieldCreatesAReachSpiderToken() {
        harness.setHand(player1, List.of(new TwinSilkSpider()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> tokens = findPermanents(player1, "Spider");
        assertThat(tokens).hasSize(1);

        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SPIDER);
        assertThat(token.getCard().getKeywords()).contains(Keyword.REACH);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    void tokenIsCreatedOnlyWhenTheEnterTriggerResolves() {
        harness.setHand(player1, List.of(new TwinSilkSpider()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        assertThat(findPermanents(player1, "Spider")).isEmpty();

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Twin-Silk Spider");
        assertThat(findPermanents(player1, "Spider")).isEmpty();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Spider")).hasSize(1);
        Permanent token = findPermanent(player1, "Spider");
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isAttacking()).isFalse();
        assertThat(token.isSummoningSick()).isTrue();
    }

    @Test
    void enterTriggerStillCreatesTokenAfterSpiderDies() {
        harness.setHand(player1, List.of(new TwinSilkSpider()));
        harness.setHand(player2, List.of(new ForceOfDespair()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player2, 0);

        harness.assertNotOnBattlefield(player1, "Twin-Silk Spider");
        harness.assertInGraveyard(player1, "Twin-Silk Spider");
        assertThat(findPermanents(player1, "Spider")).isEmpty();

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Spider")).hasSize(1);
        assertThat(findPermanents(player2, "Spider")).isEmpty();
    }

    @Test
    void otherPlayerCreatesTokenUnderTheirOwnControl() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new TwinSilkSpider()));
        harness.addMana(player2, ManaColor.GREEN, 3);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Spider")).hasSize(1);
        assertThat(findPermanents(player1, "Spider")).isEmpty();
        harness.assertOnBattlefield(player2, "Twin-Silk Spider");
    }
}
