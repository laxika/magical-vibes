package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.ChainLightning;
import com.github.laxika.magicalvibes.cards.p.Pestilence;
import com.github.laxika.magicalvibes.cards.p.PsionicEntity;
import com.github.laxika.magicalvibes.cards.w.WallOfEarth;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NovaPentacle.class, ChainLightning.class, Pestilence.class, PsionicEntity.class,
        WallOfEarth.class, WhiteKnight.class})
class NovaPentacleTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent chooses the target creature during activation")
    void opponentChoosesTargetCreature() {
        Permanent pentacle = addCreatureReady(player1, new NovaPentacle());
        Permanent ownCreature = addCreatureReady(player1, new WallOfEarth());
        Permanent opponentCreature = addCreatureReady(player2, new WallOfEarth());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, indexOf(player1, pentacle), null, null);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validPermanentIds()).contains(ownCreature.getId(), opponentCreature.getId());

        harness.handlePermanentChosen(player2, ownCreature.getId());

        assertThat(pentacle.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Redirects the next damage from the chosen source to the creature chosen by the opponent")
    void redirectsNextDamageToOpponentChosenCreature() {
        Permanent pentacle = addCreatureReady(player1, new NovaPentacle());
        Permanent source = addCreatureReady(player1, new PsionicEntity());
        Permanent target = addCreatureReady(player1, new WallOfEarth());
        int lifeBefore = gd.getLife(player1.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, indexOf(player1, pentacle), null, null);
        harness.handlePermanentChosen(player2, target.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        harness.activateAbility(player1, indexOf(player1, source), null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can choose a damage-dealing spell on the stack as the source")
    void canChooseDamageDealingSpellOnStackAsSource() {
        Permanent pentacle = addCreatureReady(player1, new NovaPentacle());
        Permanent target = addCreatureReady(player1, new WallOfEarth());
        ChainLightning chainLightning = new ChainLightning();

        harness.setHand(player1, List.of(chainLightning));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, player1.getId());
        harness.activateAbility(player1, indexOf(player1, pentacle), null, null);
        harness.handlePermanentChosen(player2, target.getId());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice sourceChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(sourceChoice).isNotNull();
        assertThat(sourceChoice.playerId()).isEqualTo(player1.getId());
        assertThat(sourceChoice.validIds()).contains(chainLightning.getId());

        harness.handlePermanentChosen(player1, chainLightning.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Only the next damage to the controller is redirected, not damage to other recipients")
    void redirectsOnlyNextDamageToController() {
        Permanent pentacle = addCreatureReady(player1, new NovaPentacle());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Pestilence());
        Permanent target = addCreatureReady(player2, new WallOfEarth());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, indexOf(player1, pentacle), null, null);
        harness.handlePermanentChosen(player2, target.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        harness.activateAbility(player1, indexOf(player1, source), null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        assertThat(target.getMarkedDamage()).isEqualTo(2);

        harness.activateAbility(player1, indexOf(player1, source), null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 18);
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("An unused redirection shield expires at the end of the turn")
    void unusedShieldExpiresAtEndOfTurn() {
        Permanent pentacle = addCreatureReady(player1, new NovaPentacle());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Pestilence());
        Permanent target = addCreatureReady(player1, new WallOfEarth());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, indexOf(player1, pentacle), null, null);
        harness.handlePermanentChosen(player2, target.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, indexOf(player1, source), null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Protection prevents redirected damage from the original source")
    void protectionPreventsRedirectedDamage() {
        Permanent pentacle = addCreatureReady(player1, new NovaPentacle());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Pestilence());
        Permanent target = addCreatureReady(player1, new WhiteKnight());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, indexOf(player1, pentacle), null, null);
        harness.handlePermanentChosen(player2, target.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        harness.activateAbility(player1, indexOf(player1, source), null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "White Knight");
    }

    @Test
    @DisplayName("Damage is not redirected after the destination leaves the battlefield")
    void destinationLeavingBattlefieldStopsRedirection() {
        Permanent pentacle = addCreatureReady(player1, new NovaPentacle());
        Permanent source = addCreatureReady(player1, new PsionicEntity());
        Permanent target = addCreatureReady(player1, new WallOfEarth());
        harness.setHand(player1, List.of(new ChainLightning(), new ChainLightning()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, indexOf(player1, pentacle), null, null);
        harness.handlePermanentChosen(player2, target.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        harness.castAndResolveSorcery(player1, 0, target.getId());
        harness.assertInGraveyard(player1, "Wall of Earth");
        harness.activateAbility(player1, indexOf(player1, source), null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("An illegal creature target prevents the ability from resolving")
    void targetLeavingBeforeResolutionStopsAbility() {
        Permanent pentacle = addCreatureReady(player1, new NovaPentacle());
        Permanent target = addCreatureReady(player1, new PsionicEntity());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, indexOf(player1, pentacle), null, null);
        harness.handlePermanentChosen(player2, target.getId());
        harness.activateAbility(player1, indexOf(player1, target), null, target.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Psionic Entity");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
