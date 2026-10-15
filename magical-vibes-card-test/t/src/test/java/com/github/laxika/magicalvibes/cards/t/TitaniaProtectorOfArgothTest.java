package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.StoneRain;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.s.SongOfTheDryads;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TitaniaProtectorOfArgoth.class, Forest.class, Mountain.class, StoneRain.class, SolRing.class, SongOfTheDryads.class})
class TitaniaProtectorOfArgothTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target land from the graveyard when it enters")
    void returnsTargetLandOnEntry() {
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));
        harness.setHand(player1, List.of(new TitaniaProtectorOfArgoth()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Creates a 5/3 Elemental when a land you control dies")
    void createsElementalWhenControlledLandDies() {
        harness.addToBattlefield(player1, new TitaniaProtectorOfArgoth());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player2, List.of(new StoneRain()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castAndResolveSorcery(player2, 0, mountain.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        Permanent elemental = findPermanent(player1, "Elemental");
        assertThat(elemental.getEffectivePower()).isEqualTo(5);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not create a token when an opponent's land dies")
    void doesNotTriggerForOpponentsLand() {
        harness.addToBattlefield(player1, new TitaniaProtectorOfArgoth());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, mountain.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Elemental")).isZero();
    }

    @Test
    @DisplayName("Entry targets exactly one land from your graveyard, excluding nonlands and opposing lands")
    void entryTargetsOnlyOwnLandCards() {
        Forest forest = new Forest();
        SolRing ring = new SolRing();
        Mountain opposingLand = new Mountain();
        harness.setGraveyard(player1, List.of(forest, ring));
        harness.setGraveyard(player2, List.of(opposingLand));
        harness.setHand(player1, List.of(new TitaniaProtectorOfArgoth()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(forest.getId());
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Sol Ring");
        harness.assertInGraveyard(player2, "Mountain");
    }

    @Test
    @DisplayName("Titania enters normally when only the opponent has a land in their graveyard")
    void entersWithoutLegalGraveyardTarget() {
        harness.setGraveyard(player1, List.of(new SolRing()));
        harness.setGraveyard(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new TitaniaProtectorOfArgoth()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Titania, Protector of Argoth");
        harness.assertInGraveyard(player1, "Sol Ring");
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A permanent turned into a land by Song of the Dryads triggers Titania when destroyed")
    void triggersForPermanentThatBecameLand() {
        harness.addToBattlefield(player1, new TitaniaProtectorOfArgoth());
        Permanent ring = harness.addToBattlefieldAndReturn(player1, new SolRing());
        harness.setHand(player1, List.of(new SongOfTheDryads(), new StoneRain()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, ring.getId());
        harness.passBothPriorities();
        assertThat(gqs.isLand(gd, ring)).isTrue();
        harness.castAndResolveSorcery(player1, 0, ring.getId());

        harness.assertInGraveyard(player1, "Sol Ring");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elemental")).isEqualTo(1);
        Permanent elemental = findPermanent(player1, "Elemental");
        assertThat(elemental.getEffectivePower()).isEqualTo(5);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(3);
    }
}
