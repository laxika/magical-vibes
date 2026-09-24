package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DanithaBenaliasHope.class, HolyStrength.class, LeoninScimitar.class})
class DanithaBenaliasHopeTest extends BaseCardTest {

    @Test
    void putsAuraFromHandOntoBattlefieldAttachedToDanitha() {
        HolyStrength aura = new HolyStrength();
        castDanitha(List.of(aura), List.of());

        PendingInteraction.AttachAurasChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.AttachAurasChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(aura.getId());

        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));

        Permanent danitha = findPermanent("Danitha, Benalia's Hope");
        Permanent attachedAura = findPermanent("Holy Strength");
        assertThat(attachedAura.getAttachedTo()).isEqualTo(danitha.getId());
        harness.assertNotInHand(player1, "Holy Strength");
    }

    @Test
    void putsEquipmentFromGraveyardOntoBattlefieldAttachedToDanitha() {
        LeoninScimitar equipment = new LeoninScimitar();
        castDanitha(List.of(), List.of(equipment));

        PendingInteraction.AttachAurasChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.AttachAurasChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(equipment.getId());

        harness.handleMultipleCardsChosen(player1, List.of(equipment.getId()));

        Permanent danitha = findPermanent("Danitha, Benalia's Hope");
        Permanent attachedEquipment = findPermanent("Leonin Scimitar");
        assertThat(attachedEquipment.getAttachedTo()).isEqualTo(danitha.getId());
        harness.assertNotInGraveyard(player1, "Leonin Scimitar");
    }

    @Test
    void canDeclineTheOptionalAttachment() {
        HolyStrength aura = new HolyStrength();
        castDanitha(List.of(aura), List.of());

        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertInHand(player1, "Holy Strength");
        harness.assertOnBattlefield(player1, "Danitha, Benalia's Hope");
    }

    private void castDanitha(List<Card> handAttachments, List<Card> graveyard) {
        List<Card> hand = new ArrayList<>();
        hand.add(new DanithaBenaliasHope());
        hand.addAll(handAttachments);
        harness.setHand(player1, hand);
        harness.setGraveyard(player1, graveyard);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent findPermanent(String name) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals(name))
                .findFirst()
                .orElseThrow();
    }
}
