package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.ThayanEvokers;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VodalianTideMage.class, GrizzlyBears.class, ShivanDragon.class, ThayanEvokers.class})
class VodalianTideMageTest extends BaseCardTest {

    @Test
    void conjuresDuplicateOfTheOnlyOtherNontokenCreature() {
        Permanent mage = addCreatureReady(player1, new VodalianTideMage());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(mage, attacker);
        assertThat(gd.playerHands.get(player1.getId()))
                .singleElement().satisfies(card -> {
                    assertThat(card.getName()).isEqualTo("Grizzly Bears");
                    assertThat(card.getId()).isNotEqualTo(attacker.getCard().getId());
                    assertThat(card.getOwnerId()).isEqualTo(player1.getId());
                });
    }

    @Test
    void choosesOneOfSeveralEligibleCombatDamageDealers() {
        addCreatureReady(player1, new VodalianTideMage());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent dragon = addCreatureReady(player1, new ShivanDragon());
        bears.setAttacking(true);
        dragon.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(bears.getId(), dragon.getId());

        harness.handlePermanentChosen(player1, dragon.getId());

        assertThat(gd.playerHands.get(player1.getId()))
                .singleElement().satisfies(card -> assertThat(card.getName()).isEqualTo("Shivan Dragon"));
    }

    @Test
    void ignoresTheMageItselfAndTokenCombatDamage() {
        Permanent mage = addCreatureReady(player1, new VodalianTideMage());
        mage.setAttacking(true);
        Card tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        tokenCard.setTokenCard(true);
        Permanent token = addCreatureReady(player1, tokenCard);
        token.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void canChooseADamageDealerThatChangedControllersBeforeResolution() {
        addCreatureReady(player1, new VodalianTideMage());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent dragon = addCreatureReady(player1, new ShivanDragon());
        bears.setAttacking(true);
        dragon.setAttacking(true);

        resolveCombat();
        gd.playerBattlefields.get(player1.getId()).remove(dragon);
        gd.playerBattlefields.get(player2.getId()).add(dragon);
        resolveAllTriggers();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(bears.getId(), dragon.getId());
        harness.handlePermanentChosen(player1, dragon.getId());

        assertThat(gd.playerHands.get(player1.getId()))
                .singleElement().satisfies(card -> assertThat(card.getName()).isEqualTo("Shivan Dragon"));
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void conjuredCreatureCanTriggerTheMageAfterBeingCast() {
        addCreatureReady(player1, new VodalianTideMage());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        Card duplicate = gd.playerHands.get(player1.getId()).getFirst();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.castFromHand(player1, duplicate, "{1}{G}");
        resolveAllTriggers();
        Permanent conjured = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(duplicate.getId()))
                .findFirst().orElseThrow();
        bears.setAttacking(false);
        conjured.setSummoningSick(false);
        conjured.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .singleElement().satisfies(card -> assertThat(card.getName()).isEqualTo("Grizzly Bears"));
    }

    @Test
    void duplicateConjureTriggersControlledEvokers() {
        addCreatureReady(player1, new VodalianTideMage());
        Permanent evokers = addCreatureReady(player1, new ThayanEvokers());
        Permanent opposingEvokers = addCreatureReady(player2, new ThayanEvokers());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .singleElement().satisfies(card -> assertThat(card.getName()).isEqualTo("Grizzly Bears"));
        assertThat(evokers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingEvokers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opposingCreaturesDoNotTriggerTheMage() {
        addCreatureReady(player1, new VodalianTideMage());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }
}
