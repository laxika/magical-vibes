package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FiligreeRacer.class, GrizzlyBears.class, Plains.class, Shock.class})
class FiligreeRacerTest extends BaseCardTest {

    @Test
    void entersWithFourEnergyCounters() {
        harness.setHand(player1, List.of(new FiligreeRacer()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(4);
    }

    @Test
    void paysEnergyToGrantJumpStartAndCastTargetedSpell() {
        Shock spell = new Shock();
        Plains discarded = new Plains();
        Permanent racer = addReadyRacer();
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(discarded));
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(racer), 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(racer)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(spell.getId());
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        harness.passBothPriorities();

        assertThat(gd.cardsGrantedJumpStartUntilEndOfTurn).contains(spell.getId());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castJumpStart(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(card -> card.getId().equals(spell.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void decliningEnergyPaymentDoesNotGrantJumpStart() {
        Permanent racer = addReadyRacer();
        addCreatureReady(player1, new GrizzlyBears());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(racer), 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(racer)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.cardsGrantedJumpStartUntilEndOfTurn).isEmpty();
    }

    private Permanent addReadyRacer() {
        Permanent racer = new Permanent(new FiligreeRacer());
        racer.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(racer);
        return racer;
    }
}
