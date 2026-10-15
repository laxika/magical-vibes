package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CunningManeuver;
import com.github.laxika.magicalvibes.cards.h.HypersonicDragon;
import com.github.laxika.magicalvibes.cards.s.SpiritWaterRevival;
import com.github.laxika.magicalvibes.cards.t.TurtleDuck;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FireLordAzula.class, CunningManeuver.class, TurtleDuck.class, HypersonicDragon.class,
        SpiritWaterRevival.class})
class FireLordAzulaTest extends BaseCardTest {

    @Test
    @DisplayName("Firebending adds red mana until end of combat")
    void firebendingAddsManaUntilEndOfCombat() {
        Permanent azula = addCreatureReady(player1, new FireLordAzula());

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(azula);
    }

    @Test
    @DisplayName("Copies an instant cast while Azula is attacking")
    void copiesInstantWhileAttacking() {
        addCreatureReady(player1, new FireLordAzula());
        Card spell = lifeGainInstant();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0));
        harness.castInstant(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Keeps the copy trigger after Azula stops attacking")
    void keepsCopyTriggerAfterAzulaStopsAttacking() {
        Permanent azula = addCreatureReady(player1, new FireLordAzula());
        Card spell = lifeGainInstant();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0));
        harness.castInstant(player1, 0);
        azula.setAttacking(false);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Does not copy a spell cast while Azula is not attacking")
    void doesNotCopySpellWhileNotAttacking() {
        addCreatureReady(player1, new FireLordAzula());
        Card spell = lifeGainInstant();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Copies a permanent spell as a token while Azula is attacking")
    void copiesPermanentSpellAsTokenWhileAttacking() {
        addCreatureReady(player1, new FireLordAzula());
        Card spell = flashCreature();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0));
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> copies = findPermanents(player1, "Flash Creature");
        assertThat(copies).hasSize(2);
        assertThat(copies).filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    void copyCanKeepTheOriginalTarget() {
        addCreatureReady(player1, new FireLordAzula());
        Permanent duck = harness.addToBattlefieldAndReturn(player1, new TurtleDuck());
        harness.setHand(player1, List.of(new CunningManeuver()));
        harness.addMana(player1, ManaColor.RED, 2);

        declareAttackers(List.of(0));
        harness.castInstant(player1, 0, duck.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(duck.getPowerModifier()).isEqualTo(6);
        assertThat(duck.getToughnessModifier()).isEqualTo(2);
        assertThat(findPermanents(player1, "Clue")).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copyCanChooseANewTargetWithoutChangingOriginal() {
        addCreatureReady(player1, new FireLordAzula());
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player1, new TurtleDuck());
        Permanent copyTarget = harness.addToBattlefieldAndReturn(player2, new TurtleDuck());
        harness.setHand(player1, List.of(new CunningManeuver()));
        harness.addMana(player1, ManaColor.RED, 2);

        declareAttackers(List.of(0));
        harness.castInstant(player1, 0, originalTarget.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, copyTarget.getId());
        resolveAllTriggers();

        assertThat(originalTarget.getPowerModifier()).isEqualTo(3);
        assertThat(originalTarget.getToughnessModifier()).isEqualTo(1);
        assertThat(copyTarget.getPowerModifier()).isEqualTo(3);
        assertThat(copyTarget.getToughnessModifier()).isEqualTo(1);
        assertThat(findPermanents(player1, "Clue")).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotCopyOpponentsSpellWhileAttacking() {
        addCreatureReady(player1, new FireLordAzula());
        Permanent duck = harness.addToBattlefieldAndReturn(player2, new TurtleDuck());
        harness.setHand(player2, List.of(new CunningManeuver()));
        harness.addMana(player2, ManaColor.RED, 2);

        declareAttackers(List.of(0));
        harness.castInstant(player2, 0, duck.getId());
        resolveAllTriggers();

        assertThat(duck.getPowerModifier()).isEqualTo(3);
        assertThat(duck.getToughnessModifier()).isEqualTo(1);
        assertThat(findPermanents(player2, "Clue")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copyPreservesPaidWaterbendAdditionalCost() {
        addCreatureReady(player1, new FireLordAzula());
        harness.addToBattlefield(player1, new HypersonicDragon());
        harness.setHand(player1, List.of(new SpiritWaterRevival()));
        List<Card> library = IntStream.range(0, 20)
                .mapToObj(i -> (Card) new TurtleDuck()).toList();
        harness.setLibrary(player1, library);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.ensurePriority(player1);
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null,
                List.of(), List.of(), false, null, null, List.of(), List.of(), null, null, true);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(14);
        assertThat(gd.playersWithNoMaximumHandSize).contains(player1.getId());
        assertThat(gd.stack).isEmpty();
    }

    private static Card lifeGainInstant() {
        Card card = new Card();
        card.setName("Life Gain");
        card.setType(CardType.INSTANT);
        card.setManaCost("{1}");
        card.addEffect(EffectSlot.SPELL, new GainLifeEffect(1));
        return card;
    }

    private static Card flashCreature() {
        Card card = new Card();
        card.setName("Flash Creature");
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setPower(1);
        card.setToughness(1);
        card.setKeywords(Set.of(Keyword.FLASH));
        return card;
    }
}
