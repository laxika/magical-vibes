package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DalekDrone;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GlazeFiend;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Cybership.class, GrizzlyBears.class, Forest.class, ClockworkDroid.class, DalekDrone.class, GlazeFiend.class})
class CybershipTest extends BaseCardTest {

    @Test
    void combatDamagePutsTopTwoCardsOntoBattlefieldAsCybermen() {
        Permanent cybership = addCybershipReady();
        Card firstCard = new GrizzlyBears();
        Card secondCard = new Forest();
        Card remainingCard = new Forest();
        harness.setLibrary(player2, List.of(firstCard, secondCard, remainingCard));

        cybership.setAnimatedUntilEndOfTurn(true);
        cybership.setAnimatedPower(8);
        cybership.setAnimatedToughness(8);
        cybership.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        List<Permanent> cybermen = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.isFaceDown()
                        && (permanent.getCard().getId().equals(firstCard.getId())
                        || permanent.getCard().getId().equals(secondCard.getId())))
                .toList();
        assertThat(cybermen).hasSize(2);
        assertThat(cybermen).allSatisfy(cyberman -> {
            assertThat(cyberman.getFaceDownPower()).isEqualTo(2);
            assertThat(cyberman.getFaceDownToughness()).isEqualTo(2);
            assertThat(gqs.getEffectiveCardTypes(gd, cyberman))
                    .containsExactlyInAnyOrder(CardType.ARTIFACT, CardType.CREATURE);
            assertThat(gqs.effectiveCreatureSubtypes(gd, cyberman))
                    .containsExactly(CardSubtype.CYBERMAN);
        });
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remainingCard);
    }

    @Test
    void putsOnlyAvailableCardsOntoBattlefield() {
        Permanent cybership = addCybershipReady();
        Card onlyCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(onlyCard));

        cybership.setAnimatedUntilEndOfTurn(true);
        cybership.setAnimatedPower(8);
        cybership.setAnimatedToughness(8);
        cybership.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isFaceDown()
                        && permanent.getCard().getId().equals(onlyCard.getId()));
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void crewAcceptsSummoningSickCreaturesWithEnoughCombinedPower() {
        Permanent cybership = addCybershipReady();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ClockworkDroid());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ClockworkDroid());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, cybership)).isTrue();
        assertThat(gqs.getEffectivePower(gd, cybership)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, cybership)).isEqualTo(8);
    }

    @Test
    void crewRejectsLessThanFourPower() {
        Permanent cybership = addCybershipReady();
        Permanent droid = harness.addToBattlefieldAndReturn(player1, new ClockworkDroid());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(droid.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, cybership)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void faceDownCardsDoNotRetainPrintedAbilitiesOrTriggerTheirOwnEntryAbilities() {
        Permanent cybership = addCybershipReady();
        Card drone = new DalekDrone();
        Card forest = new Forest();
        harness.setLibrary(player2, List.of(drone, forest));
        Permanent opposingCreature = addCreatureReady(player2, new ClockworkDroid());
        int startingLife = gd.playerLifeTotals.get(player2.getId());

        dealCombatDamage(cybership);
        resolveAllTriggers();

        Permanent cyberman = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(drone.getId()))
                .findFirst().orElseThrow();
        assertThat(cyberman.isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, cyberman)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cyberman)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, cyberman, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, cyberman, Keyword.MENACE)).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingCreature);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(startingLife - 8);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void emptyLibraryDoesNotCreateCybermen() {
        Permanent cybership = addCybershipReady();
        harness.setLibrary(player2, List.of());

        dealCombatDamage(cybership);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(cybership);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void triggerStillResolvesAfterCybershipLeavesBattlefield() {
        Permanent cybership = addCybershipReady();
        Card first = new Forest();
        Card second = new Forest();
        harness.setLibrary(player2, List.of(first, second));

        dealCombatDamage(cybership);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(cybership);
        gd.playerGraveyards.get(player1.getId()).add(cybership.getCard());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .allSatisfy(permanent -> assertThat(permanent.isFaceDown()).isTrue());
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void stolenCybermenGoToTheirOwnersGraveyardWhenTheyDie() {
        Permanent cybership = addCybershipReady();
        Card first = new Forest();
        Card second = new Forest();
        harness.setLibrary(player2, List.of(first, second));

        dealCombatDamage(cybership);
        resolveAllTriggers();
        gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isFaceDown)
                .forEach(permanent -> permanent.setMarkedDamage(2));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(cybership);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void eachFaceDownCybermanTriggersArtifactEntryAbilities() {
        Permanent cybership = addCybershipReady();
        Permanent fiend = addCreatureReady(player1, new GlazeFiend());
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        dealCombatDamage(cybership);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, fiend)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, fiend)).isEqualTo(5);
    }

    private void dealCombatDamage(Permanent cybership) {
        cybership.setAnimatedUntilEndOfTurn(true);
        cybership.setAnimatedPower(8);
        cybership.setAnimatedToughness(8);
        cybership.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
    }

    private Permanent addCybershipReady() {
        return addCreatureReady(player1, new Cybership());
    }
}
