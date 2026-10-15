package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AvenMindcensor;
import com.github.laxika.magicalvibes.cards.c.CosisTrickster;
import com.github.laxika.magicalvibes.cards.e.EternalThirst;
import com.github.laxika.magicalvibes.cards.m.MarkedByHonor;
import com.github.laxika.magicalvibes.cards.o.ObNixilisUnshackled;
import com.github.laxika.magicalvibes.cards.p.PillarOfLight;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.v.VerdantHaven;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoonweaverGiant.class, MarkedByHonor.class, EternalThirst.class, RuneclawBear.class,
        AvenMindcensor.class, CosisTrickster.class, ObNixilisUnshackled.class,
        PillarOfLight.class, VerdantHaven.class})
class BoonweaverGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and offers one Aura from the graveyard, hand, or library")
    void offersOneAuraFromSearchableZones() {
        BoonweaverGiant giant = new BoonweaverGiant();
        MarkedByHonor handAura = new MarkedByHonor();
        MarkedByHonor graveyardAura = new MarkedByHonor();
        EternalThirst libraryAura = new EternalThirst();
        harness.setHand(player1, List.of(giant, handAura));
        harness.setGraveyard(player1, List.of(graveyardAura));
        harness.setLibrary(player1, List.of(libraryAura));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.AttachAurasChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.AttachAurasChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                handAura.getId(), graveyardAura.getId(), libraryAura.getId());
        assertThat(harness.getConn1().getMessagesContaining("\"type\":\"INTERACTION_PROMPT\""))
                .anySatisfy(message -> assertThat(message).contains("\"maxCount\":1"));
    }

    @Test
    @DisplayName("Puts a chosen Aura from the library onto the battlefield attached to itself")
    void attachesChosenLibraryAura() {
        BoonweaverGiant giant = new BoonweaverGiant();
        EternalThirst aura = new EternalThirst();
        harness.setHand(player1, List.of(giant));
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(aura));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));

        Permanent giantPermanent = findPermanent(player1, "Boonweaver Giant");
        Permanent auraPermanent = findPermanent(player1, "Eternal Thirst");
        assertThat(auraPermanent.getAttachedTo()).isEqualTo(giantPermanent.getId());
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(aura);
    }

    @Test
    @DisplayName("Does not offer an Aura already on the battlefield")
    void doesNotOfferBattlefieldAura() {
        Permanent bears = addCreatureReady(player1, new RuneclawBear());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MarkedByHonor());
        aura.setAttachedTo(bears.getId());
        harness.setHand(player1, List.of(new BoonweaverGiant()));
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.AttachAurasChoice.class)).isNull();
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Choosing no Aura leaves the available Aura where it was")
    void mayChooseNoAura() {
        BoonweaverGiant giant = new BoonweaverGiant();
        MarkedByHonor aura = new MarkedByHonor();
        harness.setHand(player1, List.of(giant, aura));
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(aura);
        harness.assertNotOnBattlefield(player1, "Marked by Honor");
    }

    @Test
    @DisplayName("Puts a chosen hand Aura onto the battlefield without paying its mana cost")
    void attachesChosenHandAura() {
        MarkedByHonor aura = new MarkedByHonor();
        harness.setHand(player1, List.of(new BoonweaverGiant(), aura));
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));

        assertThat(findPermanent(player1, "Marked by Honor").getAttachedTo())
                .isEqualTo(findPermanent(player1, "Boonweaver Giant").getId());
        harness.assertNotInHand(player1, "Marked by Honor");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Puts a chosen graveyard Aura onto the battlefield attached to itself")
    void attachesChosenGraveyardAura() {
        EternalThirst aura = new EternalThirst();
        harness.setHand(player1, List.of(new BoonweaverGiant()));
        harness.setGraveyard(player1, List.of(aura));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));

        assertThat(findPermanent(player1, "Eternal Thirst").getAttachedTo())
                .isEqualTo(findPermanent(player1, "Boonweaver Giant").getId());
        harness.assertNotInGraveyard(player1, "Eternal Thirst");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Excludes land Auras and Auras belonging to the opponent")
    void offersOnlyOwnedAurasThatCanEnchantTheGiant() {
        MarkedByHonor legalAura = new MarkedByHonor();
        VerdantHaven landAura = new VerdantHaven();
        MarkedByHonor opponentAura = new MarkedByHonor();
        harness.setHand(player1, List.of(new BoonweaverGiant(), legalAura, landAura));
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of());
        harness.setHand(player2, List.of(opponentAura));
        harness.setGraveyard(player2, List.of(new EternalThirst()));
        harness.setLibrary(player2, List.of(new MarkedByHonor()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.AttachAurasChoice.class)
                .validCardIds()).containsExactly(legalAura.getId());
        harness.handleMultipleCardsChosen(player1, List.of(legalAura.getId()));
        harness.assertInHand(player1, "Verdant Haven");
        harness.assertInHand(player2, "Marked by Honor");
        harness.assertInGraveyard(player2, "Eternal Thirst");
    }

    @Test
    @DisplayName("Does not put an Aura onto the battlefield if the Giant leaves before resolution")
    void cannotAttachAfterGiantLeavesBattlefield() {
        EternalThirst aura = new EternalThirst();
        harness.setHand(player1, List.of(new BoonweaverGiant(), new PillarOfLight()));
        harness.setGraveyard(player1, List.of(aura));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 10);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0,
                findPermanent(player1, "Boonweaver Giant").getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Boonweaver Giant");
        harness.assertNotOnBattlefield(player1, "Eternal Thirst");
        harness.assertInGraveyard(player1, "Eternal Thirst");
    }

    @Test
    @DisplayName("Aven Mindcensor prevents finding an Aura below the top four library cards")
    void respectsRestrictedLibrarySearch() {
        harness.addToBattlefield(player2, new AvenMindcensor());
        MarkedByHonor handAura = new MarkedByHonor();
        EternalThirst libraryAura = new EternalThirst();
        harness.setHand(player1, List.of(new BoonweaverGiant(), handAura));
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(new RuneclawBear(), new RuneclawBear(),
                new RuneclawBear(), new RuneclawBear(), libraryAura));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.AttachAurasChoice.class)
                .validCardIds()).containsExactly(handAura.getId());
    }

    @Test
    @DisplayName("Finding a library Aura triggers Ob Nixilis's library-search ability")
    void librarySearchTriggersOpponentAbilities() {
        harness.addToBattlefield(player2, new ObNixilisUnshackled());
        EternalThirst aura = new EternalThirst();
        harness.setHand(player1, List.of(new BoonweaverGiant()));
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(aura));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));

        assertThat(gd.stack).anyMatch(entry ->
                entry.getCard().getName().equals("Ob Nixilis, Unshackled"));
    }

    @Test
    @DisplayName("May decline searching rather than automatically shuffle an Aura-free library")
    void doesNotForceShuffleWhenNoAuraIsAvailable() {
        harness.addToBattlefield(player2, new CosisTrickster());
        harness.setHand(player1, List.of(new BoonweaverGiant()));
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(new RuneclawBear(), new RuneclawBear()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).noneMatch(entry ->
                entry.getCard().getName().equals("Cosi's Trickster"));
    }
}
