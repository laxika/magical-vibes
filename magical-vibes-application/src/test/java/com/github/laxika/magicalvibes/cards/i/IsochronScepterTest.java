package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AetherSpellbomb;
import com.github.laxika.magicalvibes.cards.g.GrabTheReins;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IsochronScepter.class, AetherSpellbomb.class, Shatter.class, GrabTheReins.class})
class IsochronScepterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB can imprint an instant with mana value 2 or less")
    void etbImprintsEligibleInstant() {
        harness.setHand(player1, List.of(
                new IsochronScepter(), new AetherSpellbomb(), new Shatter(), new GrabTheReins()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.ImprintFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ImprintFromHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(1);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Shatter"));
        harness.assertInHand(player1, "Aether Spellbomb");
        harness.assertInHand(player1, "Grab the Reins");
        Permanent scepter = findPermanent(player1, "Isochron Scepter");
        assertThat(gd.getImprintedCard(scepter.getCard()).getName()).isEqualTo("Shatter");
    }

    @Test
    @DisplayName("Declining the ETB choice leaves the eligible instant in hand")
    void decliningEtbLeavesInstantInHand() {
        harness.setHand(player1, List.of(new IsochronScepter(), new Shatter()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Shatter");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        Permanent scepter = findPermanent(player1, "Isochron Scepter");
        assertThat(gd.getImprintedCard(scepter.getCard())).isNull();
    }

    @Test
    @DisplayName("Activation casts a copy of the imprinted card for no mana")
    void activationCastsCopy() {
        Permanent scepter = imprintShatterOnScepter();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AetherSpellbomb());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, artifact.getId());

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Shatter") && entry.isCopy());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .filteredOn(card -> card.getName().equals("Shatter"))
                .hasSize(1);
        assertThat(gd.getImprintedCard(scepter.getCard())).isNotNull();
        assertThat(scepter.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertNotOnBattlefield(player1, "Aether Spellbomb");
    }

    @Test
    @DisplayName("Declining the activation's may-cast choice leaves the imprint available")
    void decliningActivationLeavesImprintAvailable() {
        Permanent scepter = imprintShatterOnScepter();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .filteredOn(card -> card.getName().equals("Shatter"))
                .hasSize(1);
        assertThat(gd.getImprintedCard(scepter.getCard())).isNotNull();
        assertThat(scepter.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Activation without an imprint resolves without creating a copy")
    void activationWithoutImprintDoesNothing() {
        IsochronScepter scepterCard = new IsochronScepter();
        harness.addToBattlefield(player1, scepterCard);
        Permanent scepter = findPermanent(player1, "Isochron Scepter");
        scepter.setSummoningSick(false);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(scepter.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private Permanent imprintShatterOnScepter() {
        IsochronScepter scepterCard = new IsochronScepter();
        Shatter shatterCard = new Shatter();
        gd.setImprintedCard(scepterCard, shatterCard);
        harness.addToBattlefield(player1, scepterCard);
        gd.exiledCards.add(new ExiledCardEntry(shatterCard, player1.getId(), scepterCard.getId()));
        Permanent scepter = findPermanent(player1, "Isochron Scepter");
        scepter.setSummoningSick(false);
        return scepter;
    }
}
