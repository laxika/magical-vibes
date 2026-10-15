package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WitchKingOfAngmar;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SauronTheDarkLord.class, GrizzlyBears.class, WitchKingOfAngmar.class, Forest.class, Shock.class})
class SauronTheDarkLordTest extends BaseCardTest {

    @Test
    void wardCanBePaidOnlyBySacrificingLegendaryArtifactOrCreature() {
        Permanent sauron = addCreatureReady(player1, new SauronTheDarkLord());
        Permanent invalidFodder = addCreatureReady(player2, new GrizzlyBears());
        Permanent validFodder = addCreatureReady(player2, new WitchKingOfAngmar());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, sauron.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(validFodder.getId());
        harness.handlePermanentChosen(player2, validFodder.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sauron);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(invalidFodder);
    }

    @Test
    void amassesAnOrcArmyWheneverAnOpponentCastsASpell() {
        harness.addToBattlefield(player1, new SauronTheDarkLord());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        Permanent army = findPermanent(player1, "Orc Army");
        assertThat(army.getCard().getSubtypes()).containsExactly(CardSubtype.ORC, CardSubtype.ARMY);
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    void armyCombatDamageTemptsTheRingAndOffersFourCards() {
        Permanent sauron = addCreatureReady(player1, new SauronTheDarkLord());
        Permanent army = addCreatureReady(player1, new GrizzlyBears());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);
        army.setAttacking(true);
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, sauron.getId());
        resolveAllTriggers();

        assertThat(gd.ringLevels).containsEntry(player1.getId(), 1);
        assertThat(gd.ringBearerIds).containsEntry(player1.getId(), sauron.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears", "Grizzly Bears");
    }

    @Test
    void decliningWardCountersTheSpellButStillAmasses() {
        Permanent sauron = addCreatureReady(player1, new SauronTheDarkLord());
        Permanent fodder = addCreatureReady(player2, new WitchKingOfAngmar());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, sauron.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        assertThat(sauron.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(fodder);
        harness.assertInGraveyard(player2, "Shock");
        assertThat(findPermanent(player1, "Orc Army").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    void repeatedOpponentSpellsGrowTheExistingArmy() {
        harness.addToBattlefield(player1, new SauronTheDarkLord());
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();
        Permanent army = findPermanent(player1, "Orc Army");
        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Orc Army")).containsExactly(army);
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void controllersOwnSpellDoesNotAmassOrTriggerWard() {
        Permanent sauron = addCreatureReady(player1, new SauronTheDarkLord());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, sauron.getId());
        resolveAllTriggers();

        assertThat(sauron.getMarkedDamage()).isEqualTo(2);
        assertThat(findPermanents(player1, "Orc Army")).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void nonArmyCombatDamageDoesNotTemptTheRing() {
        harness.addToBattlefield(player1, new SauronTheDarkLord());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player1);
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(gd.ringLevels.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void mayDiscardAnEmptyHandToDrawFour() {
        Permanent sauron = addCreatureReady(player1, new SauronTheDarkLord());
        Permanent army = addCreatureReady(player1, new GrizzlyBears());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);
        army.setAttacking(true);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        resolveCombat(player1);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, sauron.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void decliningDiscardKeepsTheHandAndDoesNotDraw() {
        Permanent sauron = addCreatureReady(player1, new SauronTheDarkLord());
        Permanent army = addCreatureReady(player1, new GrizzlyBears());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);
        army.setAttacking(true);
        GrizzlyBears heldCard = new GrizzlyBears();
        harness.setHand(player1, List.of(heldCard));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        resolveCombat(player1);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, sauron.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(heldCard);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.ringLevels).containsEntry(player1.getId(), 1);
    }
}
