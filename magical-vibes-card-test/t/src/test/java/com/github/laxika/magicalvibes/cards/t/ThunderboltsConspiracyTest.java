package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CrossbonesMaliciousMercenary;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.ShallowGrave;
import com.github.laxika.magicalvibes.cards.u.UnnaturalSelection;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Thunderbolts Conspiracy")
@CardUsed({ThunderboltsConspiracy.class, CrossbonesMaliciousMercenary.class,
        GrizzlyBears.class, Murder.class, ShallowGrave.class, UnnaturalSelection.class})
class ThunderboltsConspiracyTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a Villain with a finality counter and Hero subtype")
    void returnsVillainWithFinalityCounterAndHeroSubtype() {
        harness.addToBattlefield(player1, new ThunderboltsConspiracy());
        Permanent villain = addCreatureReady(player1, new CrossbonesMaliciousMercenary());

        destroyWithMurder(player2, villain.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanentByCardId(villain.getCard().getId());
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        assertThat(gqs.hasEffectiveSubtype(gd, returned, CardSubtype.VILLAIN)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, returned, CardSubtype.HERO)).isTrue();
        harness.assertNotInGraveyard(player1, "Crossbones, Malicious Mercenary");
    }

    @Test
    @DisplayName("Does not return a non-Villain creature")
    void doesNotReturnNonVillain() {
        harness.addToBattlefield(player1, new ThunderboltsConspiracy());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        destroyWithMurder(player2, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
    }

    @Test
    @DisplayName("A returned creature with a finality counter is exiled when it dies again")
    void finalityCounterExilesReturnedCreature() {
        harness.addToBattlefield(player1, new ThunderboltsConspiracy());
        Permanent villain = addCreatureReady(player1, new CrossbonesMaliciousMercenary());

        destroyWithMurder(player2, villain.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanentByCardId(villain.getCard().getId());
        destroyWithMurder(player2, returned.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Crossbones, Malicious Mercenary");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(villain.getCard().getId()));
    }

    @Test
    void returnsCreatureThatBecameAVillainBeforeDying() {
        harness.addToBattlefield(player1, new ThunderboltsConspiracy());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent selection = harness.addToBattlefieldAndReturn(player1, new UnnaturalSelection());
        changeCreatureType(selection, creature, CardSubtype.VILLAIN);

        destroyWithMurder(player2, creature.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanentByCardId(creature.getCard().getId());
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        assertThat(gqs.hasEffectiveSubtype(gd, returned, CardSubtype.HERO)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, returned, CardSubtype.BEAR)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, returned, CardSubtype.VILLAIN)).isFalse();
    }

    @Test
    void doesNotReturnPrintedVillainThatLostVillainTypeBeforeDying() {
        harness.addToBattlefield(player1, new ThunderboltsConspiracy());
        Permanent creature = addCreatureReady(player1, new CrossbonesMaliciousMercenary());
        Permanent selection = harness.addToBattlefieldAndReturn(player1, new UnnaturalSelection());
        changeCreatureType(selection, creature, CardSubtype.HUMAN);

        destroyWithMurder(player2, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Crossbones, Malicious Mercenary");
        harness.assertNotOnBattlefield(player1, "Crossbones, Malicious Mercenary");
    }

    @Test
    void oldTriggerCannotReturnCardThatLeftAndReenteredGraveyard() {
        harness.addToBattlefield(player1, new ThunderboltsConspiracy());
        Permanent creature = addCreatureReady(player1, new CrossbonesMaliciousMercenary());
        Permanent selection = harness.addToBattlefieldAndReturn(player1, new UnnaturalSelection());
        destroyWithMurder(player2, creature.getId());

        harness.castFromHand(player1, new ShallowGrave(), "{1}{B}");
        harness.passBothPriorities();
        Permanent returned = findPermanentByCardId(creature.getCard().getId());
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isZero();
        changeCreatureType(selection, returned, CardSubtype.HUMAN);
        destroyWithMurder(player2, returned.getId());
        harness.assertInGraveyard(player1, "Crossbones, Malicious Mercenary");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Crossbones, Malicious Mercenary");
        harness.assertNotOnBattlefield(player1, "Crossbones, Malicious Mercenary");
    }

    @Test
    void doesNotReturnOpponentsVillain() {
        harness.addToBattlefield(player1, new ThunderboltsConspiracy());
        Permanent creature = addCreatureReady(player2, new CrossbonesMaliciousMercenary());

        destroyWithMurder(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Crossbones, Malicious Mercenary");
        harness.assertNotOnBattlefield(player2, "Crossbones, Malicious Mercenary");
    }

    @Test
    void returnsBorrowedVillainUnderOwnersControl() {
        harness.addToBattlefield(player1, new ThunderboltsConspiracy());
        Permanent creature = addCreatureReady(player1, new CrossbonesMaliciousMercenary());
        gd.stolenCreatures.put(creature.getId(), player2.getId());

        destroyWithMurder(player2, creature.getId());
        harness.assertInGraveyard(player2, "Crossbones, Malicious Mercenary");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Crossbones, Malicious Mercenary");
        Permanent returned = findPermanent(player2, "Crossbones, Malicious Mercenary");
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        assertThat(gqs.hasEffectiveSubtype(gd, returned, CardSubtype.HERO)).isTrue();
    }

    @Test
    void canBeCastDuringOpponentsCombat() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.castFromHand(player1, new ThunderboltsConspiracy(), "{3}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thunderbolts Conspiracy");
    }

    private void changeCreatureType(Permanent selection, Permanent creature, CardSubtype subtype) {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(selection),
                null, creature.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, subtype.name());
        assertThat(gqs.hasEffectiveSubtype(gd, creature, subtype)).isTrue();
    }

    private void destroyWithMurder(Player caster, UUID targetId) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Murder()));
        harness.addMana(caster, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(caster, 0, targetId);
    }

    private Permanent findPermanentByCardId(UUID cardId) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(cardId))
                .findFirst()
                .orElseThrow();
    }
}
