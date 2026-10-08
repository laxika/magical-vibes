package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SteadfastPaladin;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.y.YouComeToARiver;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ViconiaNightsingersDisciple.class, Forest.class, Plains.class, GrizzlyBears.class,
        Island.class, Mountain.class, SteadfastPaladin.class, Swamp.class, YouComeToARiver.class})
class ViconiaNightsingersDiscipleTest extends BaseCardTest {

    @Test
    void exileAbilityTracksTheCardWithViconia() {
        Permanent viconia = harness.addToBattlefieldAndReturn(player1, new ViconiaNightsingersDisciple());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(creature.getId())).isNotNull();
        assertThat(gd.findExiledCard(creature.getId()).sourcePermanentId()).isEqualTo(viconia.getId());
    }

    @Test
    void greenSpecializationConjuresABoostedDuplicateThatCanBeCastWithAnyColorMana() {
        Permanent viconia = harness.addToBattlefieldAndReturn(player1, new ViconiaNightsingersDisciple());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 5, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(viconia.getCard().getName()).isEqualTo("Viconia, Disciple of Strength");
        Card duplicate = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Grizzly Bears"))
                .findFirst()
                .orElseThrow();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent duplicatePermanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(duplicate.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, duplicatePermanent)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, duplicatePermanent)).isEqualTo(4);
    }

    @Test
    void whiteSpecializationCanPutTheConjuredCreatureOntoTheBattlefield() {
        Permanent viconia = harness.addToBattlefieldAndReturn(player1, new ViconiaNightsingersDisciple());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(viconia.getCard().getName()).isEqualTo("Viconia, Disciple of Rebirth");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .hasSize(1);
    }

    @Test
    void blackDuplicateDrainsWhenItEnters() {
        specializeWithCreature(new Swamp(), 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Steadfast Paladin");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void redDuplicateHasItsPowerBonusAndHaste() {
        specializeWithCreature(new Mountain(), 4);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent duplicate = findPermanent(player1, "Steadfast Paladin");
        assertThat(gqs.getEffectivePower(gd, duplicate)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, duplicate)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, duplicate, Keyword.HASTE)).isTrue();
    }

    @Test
    void blueSpecializationConjuresBothCardTypesAndLetsTheInstantUseAnyColorMana() {
        Permanent viconia = harness.addToBattlefieldAndReturn(player1, new ViconiaNightsingersDisciple());
        Card creature = new SteadfastPaladin();
        Card instant = new YouComeToARiver();
        harness.setGraveyard(player2, List.of(creature, instant));
        exileWithViconia(creature);
        exileWithViconia(instant);
        specialize(new Island(), 2);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.handlePermanentChosen(player1, instant.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Steadfast Paladin");
        harness.assertInHand(player1, "You Come to a River");
        assertThat(gd.findExiledCard(creature.getId())).isNotNull();
        assertThat(gd.findExiledCard(instant.getId())).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        int instantIndex = gd.playerHands.get(player1.getId()).get(0).getName()
                .equals("You Come to a River") ? 0 : 1;
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, instantIndex, 0, viconia.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Viconia, Disciple of Arcana");
        harness.assertNotOnBattlefield(player1, "Viconia, Disciple of Arcana");
    }

    @Test
    void whiteSpecializationCanLeaveTheDuplicateInHand() {
        harness.addToBattlefield(player1, new ViconiaNightsingersDisciple());
        Card creature = new SteadfastPaladin();
        harness.setGraveyard(player2, List.of(creature));
        exileWithViconia(creature);
        specialize(new Plains(), 1);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Steadfast Paladin");
        harness.assertNotOnBattlefield(player1, "Steadfast Paladin");
        harness.assertInGraveyard(player1, "Plains");
    }

    @Test
    void blueSpecializationCanDeclineBothAvailableTargets() {
        harness.addToBattlefield(player1, new ViconiaNightsingersDisciple());
        Card creature = new SteadfastPaladin();
        Card instant = new YouComeToARiver();
        harness.setGraveyard(player2, List.of(creature, instant));
        exileWithViconia(creature);
        exileWithViconia(instant);
        specialize(new Island(), 2);

        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Viconia, Disciple of Arcana");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void specializationCannotBeActivatedDuringTheOpponentsTurn() {
        harness.addToBattlefield(player1, new ViconiaNightsingersDisciple());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 5, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Forest");
        harness.assertOnBattlefield(player1, "Viconia, Nightsinger's Disciple");
    }

    @Test
    void specializationCannotChooseACardNotExiledWithThisViconia() {
        harness.addToBattlefield(player1, new ViconiaNightsingersDisciple());
        Card tracked = new SteadfastPaladin();
        Card unrelated = new SteadfastPaladin();
        harness.setGraveyard(player2, List.of(tracked));
        harness.setExile(player2, List.of(unrelated));
        exileWithViconia(tracked);
        specialize(new Forest(), 5);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, unrelated.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returningASpecializedViconiaToTheBattlefieldDoesNotTriggerConjuring() {
        Permanent viconia = specializeWithCreature(new Forest(), 5);
        harness.setHand(player1, List.of(new YouComeToARiver()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, 0, viconia.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Viconia, Disciple of Strength");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private Permanent specializeWithCreature(Card discard, int abilityIndex) {
        Permanent viconia = harness.addToBattlefieldAndReturn(player1, new ViconiaNightsingersDisciple());
        Card creature = new SteadfastPaladin();
        harness.setGraveyard(player2, List.of(creature));
        exileWithViconia(creature);
        specialize(discard, abilityIndex);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        return viconia;
    }

    private void exileWithViconia(Card card) {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, card.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
    }

    private void specialize(Card discard, int abilityIndex) {
        harness.setHand(player1, List.of(discard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, abilityIndex, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
    }
}
