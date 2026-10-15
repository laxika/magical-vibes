package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MantleOfTheAncients.class, Forest.class, GrizzlyBears.class, HolyStrength.class,
        LeoninScimitar.class, Naturalize.class, Unsummon.class})
class MantleOfTheAncientsTest extends BaseCardTest {

    @Test
    @DisplayName("Returns selected Auras and Equipment attached to the enchanted creature")
    void returnsSelectedAttachmentsToEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        HolyStrength aura = new HolyStrength();
        LeoninScimitar equipment = new LeoninScimitar();
        Forest nonAttachment = new Forest();
        castMantle(creature, List.of(aura, equipment, nonAttachment));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).containsExactly(aura, equipment);

        harness.handleMultipleCardsChosen(player1, List.of(aura.getId(), equipment.getId()));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Holy Strength").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(findPermanent(player1, "Leonin Scimitar").getAttachedTo()).isEqualTo(creature.getId());
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(8);
    }

    @Test
    @DisplayName("Any number of targets can be declined")
    void canReturnNoAttachments() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        HolyStrength aura = new HolyStrength();
        castMantle(creature, List.of(aura));

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Holy Strength");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Attachments stay in the graveyard when the enchanted creature leaves before resolution")
    void attachmentsStayInGraveyardWhenCreatureLeaves() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        HolyStrength aura = new HolyStrength();
        LeoninScimitar equipment = new LeoninScimitar();
        castMantle(creature, List.of(aura, equipment));
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId(), equipment.getId()));

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Holy Strength");
        harness.assertInGraveyard(player1, "Leonin Scimitar");
        harness.assertNotOnBattlefield(player1, "Holy Strength");
        harness.assertNotOnBattlefield(player1, "Leonin Scimitar");
    }

    @Test
    @DisplayName("The trigger returns attachments to the last enchanted creature after Mantle is destroyed")
    void returnsAttachmentsAfterMantleLeaves() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        HolyStrength aura = new HolyStrength();
        LeoninScimitar equipment = new LeoninScimitar();
        castMantle(creature, List.of(aura, equipment));
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId(), equipment.getId()));

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, findPermanent(player1, "Mantle of the Ancients").getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mantle of the Ancients");
        assertThat(findPermanent(player1, "Holy Strength").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(findPermanent(player1, "Leonin Scimitar").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("An empty graveyard still gives the bonus for Mantle itself")
    void countsItselfWithEmptyGraveyard() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castMantle(creature, List.of());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Only selected cards return and the attachment bonus updates when Equipment is destroyed")
    void returnsOnlySelectedCardsAndUpdatesBonus() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        HolyStrength aura = new HolyStrength();
        LeoninScimitar equipment = new LeoninScimitar();
        castMantle(creature, List.of(aura, equipment));
        harness.handleMultipleCardsChosen(player1, List.of(equipment.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Holy Strength");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, findPermanent(player1, "Leonin Scimitar").getId());

        harness.assertInGraveyard(player1, "Leonin Scimitar");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    private void castMantle(Permanent creature, List<com.github.laxika.magicalvibes.model.Card> graveyard) {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new MantleOfTheAncients()));
        harness.setGraveyard(player1, graveyard);
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
    }
}
