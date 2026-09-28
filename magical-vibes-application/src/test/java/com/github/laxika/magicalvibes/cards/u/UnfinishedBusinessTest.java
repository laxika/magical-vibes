package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AbundantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.g.GateSmasher;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnfinishedBusiness.class, AbundantGrowth.class, GrizzlyBears.class,
        GateSmasher.class, HolyStrength.class, LeoninScimitar.class})
class UnfinishedBusinessTest extends BaseCardTest {

    @Test
    void returnsCreatureAndAttachesSelectedAurasAndEquipment() {
        GrizzlyBears creature = new GrizzlyBears();
        HolyStrength aura = new HolyStrength();
        LeoninScimitar equipment = new LeoninScimitar();
        cast(List.of(creature, aura, equipment), List.of(creature, aura, equipment));

        Permanent returnedCreature = findPermanent(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Holy Strength").getAttachedTo())
                .isEqualTo(returnedCreature.getId());
        assertThat(findPermanent(player1, "Leonin Scimitar").getAttachedTo())
                .isEqualTo(returnedCreature.getId());
    }

    @Test
    void equipmentEntersUnattachedWhenItCannotAttach() {
        GrizzlyBears creature = new GrizzlyBears();
        GateSmasher equipment = new GateSmasher();
        cast(List.of(creature, equipment), List.of(creature, equipment));

        assertThat(findPermanent(player1, "Gate Smasher").getAttachedTo()).isNull();
    }

    @Test
    void auraThatCannotEnchantReturnedCreatureStaysInGraveyard() {
        GrizzlyBears creature = new GrizzlyBears();
        AbundantGrowth aura = new AbundantGrowth();
        cast(List.of(creature, aura), List.of(creature, aura));

        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(aura.getId());
        assertThat(findPermanents(player1, "Abundant Growth")).isEmpty();
    }

    @Test
    void rejectsNonAuraAndNonEquipmentAttachmentTarget() {
        GrizzlyBears creature = new GrizzlyBears();
        GrizzlyBears invalidAttachment = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature, invalidAttachment));
        harness.setHand(player1, List.of(new UnfinishedBusiness()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(creature.getId(), invalidAttachment.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(List<Card> graveyard, List<Card> targets) {
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new UnfinishedBusiness()));
        addMana();
        harness.castSorcery(player1, 0, targets.stream().map(Card::getId).toList());
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
