package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(GhalmaTheShaper.class)
class GhalmaTheShaperTest extends BaseCardTest {

    @Test
    void attackingConjuresTemperedSteelAndCreatesMyrToken() {
        addCreatureReady(player1, new GhalmaTheShaper());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Tempered Steel"));

        Permanent myr = findPermanent(player1, "Myr");
        assertThat(myr.getCard().getPower()).isEqualTo(1);
        assertThat(myr.getCard().getToughness()).isEqualTo(1);
        assertThat(myr.getCard().getColor()).isNull();
        assertThat(myr.getCard().getSubtypes()).contains(CardSubtype.MYR);
        assertThat(myr.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(myr.getCard().isToken()).isTrue();
    }

    @Test
    void attackAbilityResolvesAfterGhalmaLeavesBattlefield() {
        Permanent ghalma = addCreatureReady(player1, new GhalmaTheShaper());

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(ghalma);
        gd.playerGraveyards.get(player1.getId()).add(ghalma.getCard());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Tempered Steel"))
                .hasSize(1);
        assertThat(countPermanents(player1, "Myr")).isEqualTo(1);
    }

    @Test
    void attackAbilityBenefitsItsControllerAndCreatesAnUntappedNonattackingMyr() {
        addCreatureReady(player2, new GhalmaTheShaper());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Tempered Steel"))
                .singleElement()
                .satisfies(card -> assertThat(card.getOwnerId()).isEqualTo(player2.getId()));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(countPermanents(player1, "Myr")).isZero();
        assertThat(countPermanents(player2, "Myr")).isEqualTo(1);
        Permanent myr = findPermanent(player2, "Myr");
        assertThat(myr.isTapped()).isFalse();
        assertThat(myr.isAttacking()).isFalse();
        assertThat(myr.isSummoningSick()).isTrue();
    }
}
