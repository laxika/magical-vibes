package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KneelBeforeMyLegions.class, GrizzlyBears.class})
class KneelBeforeMyLegionsTest extends BaseCardTest {

    @Test
    void createsVigilantScarecrowArtifactCreature() {
        resolveScheme("Create a 4/4 colorless Scarecrow artifact creature token with vigilance");

        Permanent token = findPermanents(player1, "Scarecrow").getFirst();
        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(4);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SCARECROW);
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void boostsOwnCreaturesWithVigilanceAndTrampleUntilEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        resolveScheme("Creatures you control get +3/+3 and gain vigilance and trample until end of turn");

        assertThat(ownCreature.getEffectivePower()).isEqualTo(5);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(opposingCreature.getEffectivePower()).isEqualTo(2);
        assertThat(opposingCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.TRAMPLE)).isFalse();
    }

    private void resolveScheme(String mode) {
        Card sourceCard = new KneelBeforeMyLegions();
        gd.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                player1.getId(),
                sourceCard.getName() + "'s set-in-motion ability",
                sourceCard.getEffects(EffectSlot.SPELL)));
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode);
    }
}
