package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AustereCommand;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.d.DevourInFlames;
import com.github.laxika.magicalvibes.cards.p.PulseOfMurasa;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SlipThroughSpace;
import com.github.laxika.magicalvibes.cards.w.Wastes;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinDarkDwellers.class, Shock.class, CounselOfTheSoratami.class,
        AustereCommand.class, GrizzlyBears.class, DevourInFlames.class,
        SlipThroughSpace.class, Wastes.class, PulseOfMurasa.class})
class GoblinDarkDwellersTest extends BaseCardTest {

    @Test
    @DisplayName("ETB targets only your instant or sorcery cards with mana value 3 or less")
    void etbTargetsOnlyQualifyingCards() {
        Card shock = new Shock();
        Card counselOfTheSoratami = new CounselOfTheSoratami();
        Card austereCommand = new AustereCommand();
        Card creature = new GrizzlyBears();
        Card opponentShock = new Shock();
        harness.setGraveyard(player1, List.of(shock, counselOfTheSoratami, austereCommand, creature));
        harness.setGraveyard(player2, List.of(opponentShock));
        harness.setHand(player1, List.of(new GoblinDarkDwellers()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(shock.getId(), counselOfTheSoratami.getId());
    }

    @Test
    @DisplayName("ETB casts the chosen spell for free and exiles it after resolution")
    void castsChosenSpellForFreeAndExilesIt() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new GoblinDarkDwellers()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(shock.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(shock.getId()));
    }

    @Test
    void decliningCastLeavesCardInGraveyard() {
        SlipThroughSpace spell = new SlipThroughSpace();
        harness.setGraveyard(player1, List.of(spell));
        harness.enterBattlefieldAndReturn(player1, new GoblinDarkDwellers());
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Slip Through Space");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void castsSorceryDuringTriggerResolutionWithoutMana() {
        SlipThroughSpace spell = new SlipThroughSpace();
        Wastes drawnCard = new Wastes();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setGraveyard(player1, List.of(spell));
        Permanent goblin = harness.enterBattlefieldAndReturn(player1, new GoblinDarkDwellers());
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, goblin.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        harness.assertNotInGraveyard(player1, "Slip Through Space");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    void targetRemovedBeforeResolutionCannotBeCast() {
        SlipThroughSpace spell = new SlipThroughSpace();
        harness.setGraveyard(player1, List.of(spell));
        harness.enterBattlefieldAndReturn(player1, new GoblinDarkDwellers());
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(spell));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    void cannotPutSpellOnStackWithoutPayingMandatoryAdditionalCost() {
        DevourInFlames spell = new DevourInFlames();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Wastes());
        harness.setGraveyard(player1, List.of(spell));
        Permanent goblin = harness.enterBattlefieldAndReturn(player1, new GoblinDarkDwellers());
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, goblin.getId());

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(spell.getId())
                && gd.playerBattlefields.get(player1.getId()).contains(land));
    }

    @Test
    void castsSpellTargetingCardInGraveyard() {
        PulseOfMurasa spell = new PulseOfMurasa();
        Wastes land = new Wastes();
        harness.setLife(player1, 20);
        harness.setGraveyard(player1, List.of(spell, land));
        harness.enterBattlefieldAndReturn(player1, new GoblinDarkDwellers());
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 26);
        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    void entersNormallyWithoutLegalGraveyardTargets() {
        harness.setGraveyard(player1, List.of(new Wastes(), new GoblinDarkDwellers()));
        harness.enterBattlefieldAndReturn(player1, new GoblinDarkDwellers());

        harness.assertOnBattlefield(player1, "Goblin Dark-Dwellers");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    void menaceRequiresAtLeastTwoBlockers() {
        addCreatureReady(player1, new GoblinDarkDwellers());
        addCreatureReady(player2, new GoblinDarkDwellers());
        addCreatureReady(player2, new GoblinDarkDwellers());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
    }
}
