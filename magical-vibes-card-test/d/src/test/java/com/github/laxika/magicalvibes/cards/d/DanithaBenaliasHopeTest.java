package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DanithaBenaliasHope.class, HolyStrength.class, LeoninScimitar.class, Unsummon.class})
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

        Permanent danitha = findPermanent(player1, "Danitha, Benalia's Hope");
        Permanent attachedAura = findPermanent(player1, "Holy Strength");
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

        Permanent danitha = findPermanent(player1, "Danitha, Benalia's Hope");
        Permanent attachedEquipment = findPermanent(player1, "Leonin Scimitar");
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

    @Test
    void putsAuraFromGraveyardOntoBattlefieldAttachedToDanitha() {
        HolyStrength aura = new HolyStrength();
        castDanitha(List.of(), List.of(aura));

        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));

        assertThat(findPermanent(player1, "Holy Strength").getAttachedTo())
                .isEqualTo(findPermanent(player1, "Danitha, Benalia's Hope").getId());
        harness.assertNotInGraveyard(player1, "Holy Strength");
    }

    @Test
    void putsEquipmentFromHandOntoBattlefieldAttachedToDanitha() {
        LeoninScimitar equipment = new LeoninScimitar();
        castDanitha(List.of(equipment), List.of());

        harness.handleMultipleCardsChosen(player1, List.of(equipment.getId()));

        assertThat(findPermanent(player1, "Leonin Scimitar").getAttachedTo())
                .isEqualTo(findPermanent(player1, "Danitha, Benalia's Hope").getId());
        harness.assertNotInHand(player1, "Leonin Scimitar");
    }

    @Test
    void canChooseOnlyOneCardAcrossHandAndGraveyard() {
        HolyStrength aura = new HolyStrength();
        LeoninScimitar equipment = new LeoninScimitar();
        castDanitha(List.of(aura), List.of(equipment));

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(aura.getId(), equipment.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(equipment.getId()));

        harness.assertInHand(player1, "Holy Strength");
        harness.assertNotOnBattlefield(player1, "Holy Strength");
        harness.assertOnBattlefield(player1, "Leonin Scimitar");
    }

    @Test
    void resolvesWithoutAChoiceWhenNoAttachmentsAreAvailable() {
        castDanitha(List.of(), List.of());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Danitha, Benalia's Hope");
    }

    @Test
    void equipmentCanEnterUnattachedAfterDanithaLeaves() {
        LeoninScimitar equipment = new LeoninScimitar();
        castDanithaBeforeTrigger(List.of(equipment), List.of());
        returnDanithaToHand();
        resolveAllTriggers();

        PendingInteraction.AttachAurasChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.AttachAurasChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(equipment.getId());
        harness.handleMultipleCardsChosen(player1, List.of(equipment.getId()));

        assertThat(findPermanent(player1, "Leonin Scimitar").getAttachedTo()).isNull();
        harness.assertNotInHand(player1, "Leonin Scimitar");
    }

    @Test
    void auraStaysInGraveyardAfterDanithaLeaves() {
        HolyStrength aura = new HolyStrength();
        castDanithaBeforeTrigger(List.of(), List.of(aura));
        returnDanithaToHand();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Holy Strength");
        harness.assertNotOnBattlefield(player1, "Holy Strength");
        assertThat(gd.stack).isEmpty();
    }

    private void returnDanithaToHand() {
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0,
                findPermanent(player1, "Danitha, Benalia's Hope").getId());
        harness.assertInHand(player1, "Danitha, Benalia's Hope");
    }

    private void castDanitha(List<Card> handAttachments, List<Card> graveyard) {
        castDanithaBeforeTrigger(handAttachments, graveyard);
        resolveAllTriggers();
    }

    private void castDanithaBeforeTrigger(List<Card> handAttachments, List<Card> graveyard) {
        List<Card> hand = new ArrayList<>();
        hand.add(new DanithaBenaliasHope());
        hand.addAll(handAttachments);
        harness.setHand(player1, hand);
        harness.setGraveyard(player1, graveyard);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
