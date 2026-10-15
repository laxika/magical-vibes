package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.r.RestInPeace;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlamdringFoeHammer.class, GleamOfDeath.class, GrizzlyBears.class, LavaAxe.class, Shock.class, RestInPeace.class, Cancel.class})
class GlamdringFoeHammerTest extends BaseCardTest {

    @Test
    void reducesInstantAndSorceryCostsByEquippedCreaturePower() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent glamdring = addReadyGlamdring(player1);
        glamdring.setAttachedTo(creature.getId());

        harness.setHand(player1, List.of(new LavaAxe()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void adventureMillsSixAndReturnsAllInstantAndSorceryCards() {
        GlamdringFoeHammer card = new GlamdringFoeHammer();
        Card instantOne = new Shock();
        Card creatureOne = new GrizzlyBears();
        Card sorcery = new LavaAxe();
        Card creatureTwo = new GrizzlyBears();
        Card instantTwo = new Shock();
        Card creatureThree = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(card)));
        harness.setLibrary(player1, List.of(
                instantOne, creatureOne, sorcery, creatureTwo, instantTwo, creatureThree));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(instantOne, sorcery, instantTwo);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(creatureOne, creatureTwo, creatureThree);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureCardCanBeCastFromExileAfterResolving() {
        GlamdringFoeHammer card = new GlamdringFoeHammer();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard() == card);
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void equipCostsTwoAndEnablesReduction() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent glamdring = addReadyGlamdring(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(glamdring.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.setHand(player1, List.of(new LavaAxe()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, player2.getId());
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void unattachedEquipmentDoesNotReduceCosts() {
        addReadyGlamdring(player1);
        harness.setHand(player1, List.of(new LavaAxe()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void opponentsEquipmentDoesNotReduceYourCosts() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        addReadyGlamdring(player2).setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new LavaAxe()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void reductionDoesNotPayColoredMana() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addReadyGlamdring(player1).setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void instantReductionStopsAtZeroGenericMana() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addReadyGlamdring(player1).setAttachedTo(creature.getId());
        Card shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.setHand(player1, List.of(new Cancel()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, shock.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(shock);
    }

    @Test
    void artifactSpellsAreNotReduced() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addReadyGlamdring(player1).setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new GlamdringFoeHammer()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void adventureReceivesSorceryCostReduction() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addReadyGlamdring(player1).setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new GlamdringFoeHammer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void shortLibraryReturnsOnlyNewlyMilledSpells() {
        GlamdringFoeHammer card = new GlamdringFoeHammer();
        Card priorSpell = new Shock();
        Card milledSpell = new LavaAxe();
        Card milledCreature = new GrizzlyBears();
        harness.setHand(player1, List.of(card));
        harness.setGraveyard(player1, List.of(priorSpell));
        harness.setLibrary(player1, List.of(milledSpell, milledCreature));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(milledSpell);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(priorSpell, milledCreature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureReturnsMilledSpellsEvenWhenExiledByRestInPeace() {
        harness.addToBattlefield(player1, new RestInPeace());
        GlamdringFoeHammer card = new GlamdringFoeHammer();
        Card instant = new Shock();
        Card sorcery = new LavaAxe();
        Card creature = new GrizzlyBears();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(instant, sorcery, creature));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(instant, sorcery);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(instant.getId())).isNull();
        assertThat(gd.findExiledCard(sorcery.getId())).isNull();
        assertThat(gd.findExiledCard(creature.getId())).isNotNull();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    private Permanent addReadyGlamdring(com.github.laxika.magicalvibes.model.Player player) {
        Permanent glamdring = harness.addToBattlefieldAndReturn(player, new GlamdringFoeHammer());
        glamdring.setSummoningSick(false);
        return glamdring;
    }
}
