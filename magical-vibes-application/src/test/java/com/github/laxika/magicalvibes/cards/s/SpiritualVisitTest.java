package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.IntoTheFray;
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

@CardUsed({SpiritualVisit.class, IntoTheFray.class, SakuraTribeScout.class})
class SpiritualVisitTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 1/1 colorless Spirit token")
    void createsColorlessSpiritToken() {
        harness.setHand(player1, List.of(new SpiritualVisit()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCard().getPower()).isEqualTo(1);
        assertThat(spirit.getCard().getToughness()).isEqualTo(1);
        assertThat(spirit.getCard().getColor()).isNull();
        assertThat(spirit.getCard().getColors()).isEmpty();
        assertThat(spirit.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
        assertThat(gqs.hasKeyword(gd, spirit, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Splices onto an Arcane spell and stays in hand")
    void splicesOntoArcaneSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SakuraTribeScout());
        SpiritualVisit visit = new SpiritualVisit();
        harness.setHand(player1, List.of(new IntoTheFray(), visit));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castWithSplice(player1, 0, target.getId(), List.of(1));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(visit);
    }

    @Test
    @DisplayName("Splices onto another Spiritual Visit to create two tokens")
    void splicesOntoAnotherVisit() {
        SpiritualVisit host = new SpiritualVisit();
        SpiritualVisit spliced = new SpiritualVisit();
        harness.setHand(player1, List.of(spliced, host));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castWithSplice(player1, 1, null, List.of(0));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spliced);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(host);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0);

        assertThat(findPermanents(player1, "Spirit")).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(host, spliced);
    }

    @Test
    @DisplayName("May cast an Arcane spell without splicing Spiritual Visit")
    void spliceIsOptional() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SakuraTribeScout());
        SpiritualVisit visit = new SpiritualVisit();
        harness.setHand(player1, List.of(new IntoTheFray(), visit));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(findPermanents(player1, "Spirit")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(visit);
    }

    @Test
    @DisplayName("Each distinct Spiritual Visit can be spliced onto the same spell")
    void splicesMultipleVisits() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SakuraTribeScout());
        SpiritualVisit first = new SpiritualVisit();
        SpiritualVisit second = new SpiritualVisit();
        harness.setHand(player1, List.of(new IntoTheFray(), first, second));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castWithSplice(player1, 0, target.getId(), List.of(1, 2));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
    }
}
