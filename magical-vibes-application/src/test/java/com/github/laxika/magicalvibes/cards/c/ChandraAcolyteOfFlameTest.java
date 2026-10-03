package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BoneSplinters;
import com.github.laxika.magicalvibes.cards.d.Disentomb;
import com.github.laxika.magicalvibes.cards.d.Disfigure;
import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.l.LavakinBrawler;
import com.github.laxika.magicalvibes.cards.r.ReduceToAshes;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChandraAcolyteOfFlame.class, ChandraNalaar.class, JaceBeleren.class, Shock.class,
        Disfigure.class, Disentomb.class, LavakinBrawler.class, Fireball.class,
        BoneSplinters.class, ReduceToAshes.class})
class ChandraAcolyteOfFlameTest extends BaseCardTest {

    @Test
    @DisplayName("0 puts loyalty counters on red planeswalkers you control")
    void zeroPutsCountersOnControlledRedPlaneswalkers() {
        Permanent chandra = addReadyChandra(player1, 3);
        Permanent otherRedPlaneswalker = addPlaneswalker(player1, new ChandraNalaar(), 3);
        Permanent bluePlaneswalker = addPlaneswalker(player1, new JaceBeleren(), 3);
        Permanent opposingRedPlaneswalker = addPlaneswalker(player2, new ChandraNalaar(), 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(otherRedPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(bluePlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(opposingRedPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("0 creates hasty Elementals that are sacrificed at the next end step")
    void zeroCreatesHastyElementalsUntilNextEndStep() {
        addReadyChandra(player1, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        List<Permanent> elementals = findPermanents(player1, "Elemental");
        assertThat(elementals).hasSize(2);
        assertThat(elementals).allSatisfy(elemental -> {
            assertThat(elemental.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(elemental.getEffectivePower()).isEqualTo(1);
            assertThat(elemental.getEffectiveToughness()).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, elemental, Keyword.HASTE)).isTrue();
        });

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Elemental")).isEmpty();
    }

    @Test
    @DisplayName("-2 casts a qualifying graveyard spell for its mana cost and exiles it afterward")
    void minusTwoCastsQualifyingGraveyardSpell() {
        addReadyChandra(player1, 5);
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 2, null, shock.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertNotInGraveyard(player1, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(shock.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("The two Elementals are sacrificed by one delayed triggered ability")
    void elementalsHaveOneDelayedSacrificeTrigger() {
        addReadyChandra(player1, 4);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(findPermanents(player1, "Elemental")).hasSize(2);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Elemental")).isEmpty();
    }

    @Test
    @DisplayName("Declining the graveyard cast leaves the card in the graveyard")
    void mayDeclineGraveyardCast() {
        Permanent chandra = addReadyChandra(player1, 4);
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));

        harness.activateAbility(player1, 0, 2, null, shock.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A targeted spell with no legal targets remains in the graveyard")
    void uncastableSpellIsNotExiled() {
        addReadyChandra(player1, 4);
        Card disfigure = new Disfigure();
        harness.setGraveyard(player1, List.of(disfigure));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 2, null, disfigure.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Disfigure");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("A graveyard spell can target a creature card in the graveyard")
    void castsSpellWithGraveyardTarget() {
        addReadyChandra(player1, 4);
        Card disentomb = new Disentomb();
        Card creature = new LavakinBrawler();
        harness.setGraveyard(player1, List.of(disentomb, creature));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 2, null, disentomb.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Lavakin Brawler");
        harness.assertNotInGraveyard(player1, "Lavakin Brawler");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(disentomb.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Casting a graveyard spell with X allows choosing a nonzero X")
    void allowsChoosingXForGraveyardSpell() {
        addReadyChandra(player1, 4);
        Card fireball = new Fireball();
        harness.setGraveyard(player1, List.of(fireball));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.activateAbility(player1, 0, 2, null, fireball.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);
        harness.handleXValueChosen(player1, 5);
    }

    @Test
    @DisplayName("A mandatory sacrifice cost cannot be bypassed when casting from the graveyard")
    void cannotCastWithoutMandatorySacrifice() {
        addReadyChandra(player1, 4);
        Card boneSplinters = new BoneSplinters();
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new LavakinBrawler());
        harness.setGraveyard(player1, List.of(boneSplinters));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 2, null, boneSplinters.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice) {
            harness.handlePermanentChosen(player1, opponentCreature.getId());
            harness.passBothPriorities();
        }

        harness.assertOnBattlefield(player2, "Lavakin Brawler");
        harness.assertInGraveyard(player1, "Bone Splinters");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The graveyard ability rejects expensive spells, creatures, and opponents' cards")
    void rejectsIllegalGraveyardTargets() {
        Permanent chandra = addReadyChandra(player1, 4);
        Card expensiveSpell = new ReduceToAshes();
        Card creature = new LavakinBrawler();
        Card opponentSpell = new Shock();
        harness.setGraveyard(player1, List.of(expensiveSpell, creature));
        harness.setGraveyard(player2, List.of(opponentSpell));

        for (Card illegalTarget : List.of(expensiveSpell, creature, opponentSpell)) {
            assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null,
                    illegalTarget.getId(), Zone.GRAVEYARD)).isInstanceOf(IllegalStateException.class);
            assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
            assertThat(gd.stack).isEmpty();
        }
    }

    private Permanent addReadyChandra(Player player, int loyalty) {
        Permanent chandra = addPlaneswalker(player, new ChandraAcolyteOfFlame(), loyalty);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return chandra;
    }

    private Permanent addPlaneswalker(Player player, Card card, int loyalty) {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player, card);
        planeswalker.setCounterCount(CounterType.LOYALTY, loyalty);
        planeswalker.setSummoningSick(false);
        return planeswalker;
    }
}
