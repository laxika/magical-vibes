package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.CharcoalDiamond;
import com.github.laxika.magicalvibes.cards.d.Disfigure;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WarlordsElite;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeroOfTheDunes.class, CharcoalDiamond.class, AirElemental.class,
        GrizzlyBears.class, WarlordsElite.class, Disfigure.class})
class HeroOfTheDunesTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a qualifying artifact from the graveyard to the battlefield")
    void returnsQualifyingArtifact() {
        Card artifact = new CharcoalDiamond();
        Card expensiveCreature = new AirElemental();
        harness.setGraveyard(player1, List.of(artifact, expensiveCreature));
        castHero();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(artifact.getId());
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hero of the Dunes");
        harness.assertOnBattlefield(player1, "Charcoal Diamond");
        harness.assertNotInGraveyard(player1, "Charcoal Diamond");
        harness.assertInGraveyard(player1, "Air Elemental");
    }

    @Test
    @DisplayName("Does not offer a card with mana value greater than three")
    void doesNotOfferHighManaValueCard() {
        Card creature = new AirElemental();
        harness.setGraveyard(player1, List.of(creature));
        castHero();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Air Elemental");
    }

    @Test
    @DisplayName("Boosts only your creatures with mana value three or less")
    void boostsOnlyCheapOwnCreatures() {
        Permanent cheapCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent expensiveCreature = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new HeroOfTheDunes());

        assertThat(gqs.getEffectivePower(gd, cheapCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, expensiveCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Returns a three-mana creature and immediately boosts only its power")
    void returnsCreatureAtManaValueLimit() {
        Card creature = new WarlordsElite();
        Card instant = new Disfigure();
        Card opponentCreature = new WarlordsElite();
        harness.setGraveyard(player1, List.of(creature, instant));
        harness.setGraveyard(player2, List.of(opponentCreature));
        castHero();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Warlord's Elite");
        harness.assertNotInGraveyard(player1, "Warlord's Elite");
        harness.assertInGraveyard(player1, "Disfigure");
        harness.assertInGraveyard(player2, "Warlord's Elite");
        Permanent returned = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Warlord's Elite"));
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot return a creature from an opponent's graveyard")
    void doesNotOfferOpponentsGraveyard() {
        harness.setGraveyard(player2, List.of(new WarlordsElite()));
        castHero();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Hero of the Dunes");
        harness.assertInGraveyard(player2, "Warlord's Elite");
        harness.assertNotOnBattlefield(player1, "Warlord's Elite");
    }

    @Test
    @DisplayName("Does not return a target that leaves the graveyard before resolution")
    void targetLeavingGraveyardIsNotReturned() {
        Card creature = new WarlordsElite();
        harness.setGraveyard(player1, List.of(creature));
        castHero();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hero of the Dunes");
        harness.assertNotOnBattlefield(player1, "Warlord's Elite");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Boosts from multiple Heroes stack and end when each Hero leaves")
    void boostsStackAndEndWhenSourceLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WarlordsElite());
        Permanent firstHero = harness.addToBattlefieldAndReturn(player1, new HeroOfTheDunes());
        Permanent secondHero = harness.addToBattlefieldAndReturn(player1, new HeroOfTheDunes());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, firstHero)).isEqualTo(3);

        harness.setHand(player1, List.of(new Disfigure(), new Disfigure()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, firstHero.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);

        harness.castAndResolveInstant(player1, 0, secondHero.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        harness.assertNotOnBattlefield(player1, "Hero of the Dunes");
    }

    private void castHero() {
        harness.setHand(player1, List.of(new HeroOfTheDunes()));
        addHeroMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void addHeroMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
