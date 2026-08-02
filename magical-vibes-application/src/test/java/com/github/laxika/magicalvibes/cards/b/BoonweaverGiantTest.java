package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.s.SpiritLink;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BoonweaverGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and offers one Aura from the graveyard, hand, or library")
    void offersOneAuraFromSearchableZones() {
        BoonweaverGiant giant = new BoonweaverGiant();
        HolyStrength handAura = new HolyStrength();
        HolyStrength graveyardAura = new HolyStrength();
        SpiritLink libraryAura = new SpiritLink();
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
        SpiritLink aura = new SpiritLink();
        harness.setHand(player1, List.of(giant));
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(aura));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));

        Permanent giantPermanent = findPermanent(player1, "Boonweaver Giant");
        Permanent auraPermanent = findPermanent(player1, "Spirit Link");
        assertThat(auraPermanent.getAttachedTo()).isEqualTo(giantPermanent.getId());
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(aura);
    }

    @Test
    @DisplayName("Does not offer an Aura already on the battlefield")
    void doesNotOfferBattlefieldAura() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);
        Permanent aura = new Permanent(new HolyStrength());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
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
        HolyStrength aura = new HolyStrength();
        harness.setHand(player1, List.of(giant, aura));
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(aura);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Holy Strength"));
    }
}
