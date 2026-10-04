package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AltarsReap;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.Fog;
import com.github.laxika.magicalvibes.cards.s.StrionicResonator;
import com.github.laxika.magicalvibes.cards.s.SavageBeating;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EliteArcanist.class, Fog.class, AltarsReap.class, DoomBlade.class, StrionicResonator.class, SavageBeating.class})
class EliteArcanistTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB choice exiles an instant from hand and imprints it")
    void etbImprintsInstantFromHand() {
        harness.setHand(player1, List.of(new EliteArcanist(), new Fog()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve Arcanist â†’ MayEffect on stack
        harness.passBothPriorities(); // Resolve MayEffect â†’ may prompt

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ImprintFromHandChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(c -> c.getName().equals("Fog"));
        harness.assertNotInHand(player1, "Fog");

        Permanent arcanist = findPermanent(player1, "Elite Arcanist");
        assertThat(gd.getImprintedCard(arcanist.getCard())).isNotNull();
        assertThat(gd.getImprintedCard(arcanist.getCard()).getName()).isEqualTo("Fog");
    }

    @Test
    @DisplayName("Declining the ETB choice leaves the instant in hand")
    void decliningEtbLeavesInstantInHand() {
        harness.setHand(player1, List.of(new EliteArcanist(), new Fog()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Fog");
        Permanent arcanist = findPermanent(player1, "Elite Arcanist");
        assertThat(gd.getImprintedCard(arcanist.getCard())).isNull();
    }

    @Test
    @DisplayName("Activating copies the exiled card and casting the copy resolves it, leaving the original exiled")
    void activateCastsCopyAndKeepsOriginalExiled() {
        Permanent arcanist = imprintFogOnArcanist();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null);
        harness.passBothPriorities(); // Resolve the ability â†’ may-cast prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        // The copy is on the stack as a copy, not as the exiled original.
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Fog") && entry.isCopy());

        harness.passBothPriorities(); // Resolve the copy

        // The copy ceased to exist and the imprinted original is still exiled and imprinted.
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .filteredOn(c -> c.getName().equals("Fog"))
                .hasSize(1);
        assertThat(gd.getImprintedCard(arcanist.getCard())).isNotNull();
    }

    @Test
    @DisplayName("Declining the may-cast makes the copy cease to exist")
    void decliningMayCastDiscardsCopy() {
        imprintFogOnArcanist();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .filteredOn(c -> c.getName().equals("Fog"))
                .hasSize(1);
        harness.assertNotInGraveyard(player1, "Fog");
    }

    @Test
    @DisplayName("With no exiled card, X is zero and the activated ability does nothing")
    void canActivateWithoutImprintForZero() {
        Permanent arcanist = harness.addToBattlefieldAndReturn(player1, new EliteArcanist());
        arcanist.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null);
        assertThat(arcanist.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("X must equal the mana value of the exiled card")
    void xMustEqualExiledManaValue() {
        imprintFogOnArcanist();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("X must equal the mana value of the imprinted card");
    }

    @Test
    @DisplayName("The enters ability may still exile an instant after Arcanist is destroyed")
    void etbStillExilesAfterSourceLeaves() {
        harness.setHand(player1, List.of(new EliteArcanist(), new Fog(), new DoomBlade()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.castAndResolveInstant(player1, 1, harness.getPermanentId(player1, "Elite Arcanist"));
        harness.assertInGraveyard(player1, "Elite Arcanist");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertNotInHand(player1, "Fog");
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(c -> c.getName().equals("Fog"));
    }

    @Test
    @DisplayName("An activated ability can still copy the exiled card after Arcanist is destroyed")
    void activatedAbilitySurvivesSourceRemoval() {
        Permanent arcanist = imprintFogOnArcanist();
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, 1, null);
        harness.castAndResolveInstant(player1, 0, arcanist.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Fog") && entry.isCopy());
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A copied enters ability links both exiled cards and adds their mana values")
    void copiedEtbUsesCombinedManaValue() {
        harness.addToBattlefield(player1, new StrionicResonator());
        harness.setHand(player1, List.of(new EliteArcanist(), new Fog(), new DoomBlade()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, gd.stack.getLast().getTargetableId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
        findPermanent(player1, "Elite Arcanist").setSummoningSick(false);
        harness.activateAbility(player1, 1, 3, null);
        assertThat(findPermanent(player1, "Elite Arcanist").isTapped()).isTrue();
    }

    @Test
    @DisplayName("A copy of Altar's Reap can be cast by paying its sacrifice cost")
    void canPayMandatoryAdditionalCostForCopy() {
        Permanent arcanist = harness.addToBattlefieldAndReturn(player1, new EliteArcanist());
        AltarsReap imprinted = new AltarsReap();
        gd.setImprintedCard(arcanist.getCard(), imprinted);
        gd.exiledCards.add(new ExiledCardEntry(imprinted, player1.getId(), arcanist.getCard().getId()));
        arcanist.setSummoningSick(false);
        harness.setLibrary(player1, List.of(new Fog(), new Fog()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, arcanist.getId());
        harness.assertInGraveyard(player1, "Elite Arcanist");
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Altar's Reap") && entry.isCopy());
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertNotInGraveyard(player1, "Altar's Reap");
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Copying an instant does not bypass its explicit casting timing restriction")
    void cannotCastCombatOnlyCopyDuringMainPhase() {
        Permanent arcanist = harness.addToBattlefieldAndReturn(player1, new EliteArcanist());
        SavageBeating imprinted = new SavageBeating();
        gd.setImprintedCard(arcanist.getCard(), imprinted);
        gd.exiledCards.add(new ExiledCardEntry(imprinted, player1.getId(), arcanist.getCard().getId()));
        arcanist.setSummoningSick(false);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 5, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        if (gd.interaction.isAwaitingInput()) {
            harness.handleListChoice(player1, "Creatures you control gain double strike until end of turn");
        }

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A summoning-sick Arcanist cannot pay its tap cost")
    void cannotActivateWhileSummoningSick() {
        imprintFogOnArcanist().setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }
    private Permanent imprintFogOnArcanist() {
        EliteArcanist arcanistCard = new EliteArcanist();
        Fog fogCard = new Fog();
        gd.setImprintedCard(arcanistCard, fogCard);
        Permanent arcanist = harness.addToBattlefieldAndReturn(player1, arcanistCard);
        gd.exiledCards.add(new ExiledCardEntry(fogCard, player1.getId(), arcanistCard.getId()));
        arcanist.setSummoningSick(false);
        return arcanist;
    }
}
