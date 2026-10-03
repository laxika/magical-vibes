package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DwarvenHammer;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoldwardensGambit.class, DwarvenHammer.class, LeoninScimitar.class})
class GoldwardensGambitTest extends BaseCardTest {

    @Test
    @DisplayName("Creates five hasty 2/2 red Rebel tokens")
    void createsFiveHastyRebels() {
        castGambit(8);

        List<Permanent> rebels = findPermanents(player1, "Rebel");
        assertThat(rebels).hasSize(5);
        assertThat(rebels).allSatisfy(rebel -> {
            assertThat(rebel.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(rebel.getCard().getSubtypes()).containsExactly(CardSubtype.REBEL);
            assertThat(gqs.getEffectivePower(gd, rebel)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, rebel)).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, rebel, Keyword.HASTE)).isTrue();
        });

        harness.forceStep(TurnStep.END_STEP);
        gs.advanceStep(gd);
        assertThat(rebels).allSatisfy(rebel ->
                assertThat(gqs.hasKeyword(gd, rebel, Keyword.HASTE)).isFalse());
    }

    @Test
    @DisplayName("Affinity for Equipment reduces the generic cost and offers distinct attachments")
    void affinityAndEquipmentAttachments() {
        Permanent hammer = harness.addToBattlefieldAndReturn(player1, new DwarvenHammer());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        castGambit(6);

        List<Permanent> rebels = findPermanents(player1, "Rebel");
        assertThat(rebels).hasSize(5);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, hammer.getId());
        harness.handlePermanentChosen(player1, scimitar.getId());

        assertThat(hammer.getAttachedTo()).isEqualTo(rebels.get(0).getId());
        assertThat(scimitar.getAttachedTo()).isEqualTo(rebels.get(1).getId());
    }

    private void castGambit(int genericMana) {
        harness.setHand(player1, List.of(new GoldwardensGambit()));
        harness.addMana(player1, ManaColor.COLORLESS, genericMana);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
