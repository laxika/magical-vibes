package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DwarvenHammer;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.t.TheOmenkeel;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RecklessCrewTest extends BaseCardTest {

    @Test
    void createsTokensWithoutEquipmentWithoutOpeningAnAttachmentChoice() {
        harness.addToBattlefield(player1, new TheOmenkeel());

        castRecklessCrew();

        assertThat(findPermanents(player1, "Dwarf Berserker")).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void createsOneTokenPerVehicleOrEquipmentAndAttachesDistinctEquipment() {
        Permanent hammer = harness.addToBattlefieldAndReturn(player1, new DwarvenHammer());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new TheOmenkeel());

        castRecklessCrew();
        List<Permanent> tokens = findPermanents(player1, "Dwarf Berserker");

        assertThat(tokens).hasSize(3);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.DWARF, CardSubtype.BERSERKER);
            assertThat(token.getEffectivePower()).isEqualTo(2);
            assertThat(token.getEffectiveToughness()).isEqualTo(1);
        });
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, hammer.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, scimitar.getId());

        assertThat(hammer.getAttachedTo()).isEqualTo(tokens.get(0).getId());
        assertThat(scimitar.getAttachedTo()).isEqualTo(tokens.get(1).getId());
    }

    @Test
    void maySkipAPlacementAndAttachTheEquipmentToALaterToken() {
        Permanent hammer = harness.addToBattlefieldAndReturn(player1, new DwarvenHammer());
        harness.addToBattlefield(player1, new TheOmenkeel());

        castRecklessCrew();
        List<Permanent> tokens = findPermanents(player1, "Dwarf Berserker");
        assertThat(tokens).hasSize(2);

        harness.handlePermanentChosen(player1, player1.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, hammer.getId());

        assertThat(hammer.getAttachedTo()).isEqualTo(tokens.get(1).getId());
    }

    private void castRecklessCrew() {
        harness.setHand(player1, List.of(new RecklessCrew()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
