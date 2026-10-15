package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElderAuntie.class})
class ElderAuntieTest extends BaseCardTest {

    @Test
    void enteringCreatesABlackAndRedGoblinToken() {
        harness.setHand(player1, List.of(new ElderAuntie()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> goblins = findPermanents(player1, "Goblin");
        assertThat(goblins).hasSize(1);
        Permanent goblin = goblins.getFirst();
        assertThat(goblin.getCard().getPower()).isEqualTo(1);
        assertThat(goblin.getCard().getToughness()).isEqualTo(1);
        assertThat(goblin.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLACK, CardColor.RED);
        assertThat(goblin.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(goblin.getCard().getSubtypes()).contains(CardSubtype.GOBLIN);
        assertThat(goblin.getCard().isToken()).isTrue();
    }

    @Test
    void enteringWithoutBeingCastCreatesATokenForItsController() {
        harness.enterBattlefieldAndReturn(player2, new ElderAuntie());

        assertThat(countPermanents(player2, "Goblin")).isZero();
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Goblin")).isEqualTo(1);
        assertThat(countPermanents(player1, "Goblin")).isZero();
        Permanent goblin = findPermanent(player2, "Goblin");
        assertThat(goblin.isTapped()).isFalse();
        assertThat(goblin.isSummoningSick()).isTrue();
    }

    @Test
    void triggerStillCreatesATokenAfterElderAuntieLeavesTheBattlefield() {
        Permanent auntie = harness.enterBattlefieldAndReturn(player1, new ElderAuntie());
        gd.playerBattlefields.get(player1.getId()).remove(auntie);
        harness.setGraveyard(player1, List.of(auntie.getCard()));

        resolveAllTriggers();

        assertThat(countPermanents(player1, "Elder Auntie")).isZero();
        assertThat(countPermanents(player1, "Goblin")).isEqualTo(1);
        assertThat(countPermanents(player2, "Goblin")).isZero();
    }
}
