package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AuspiciousArrival;
import com.github.laxika.magicalvibes.cards.b.BolracClanBasher;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PyrotechnicPerformer.class, BolracClanBasher.class, AuspiciousArrival.class, Murder.class})
class PyrotechnicPerformerTest extends BaseCardTest {

    @Test
    void turningThisCreatureFaceUpDealsItsPowerToEachOpponent() {
        Permanent performer = castFaceDownPerformer(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.turnFaceUp(player1, 0);
        resolveAllTriggers();

        assertThat(performer.isFaceDown()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
    }

    @Test
    void anotherCreatureTurningFaceUpDealsThatCreaturesPowerToEachOpponent() {
        addCreatureReady(player1, new PyrotechnicPerformer());
        harness.setHand(player1, List.of(new BolracClanBasher()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent turnedCreature = findPermanent(player1, "Bolrac-Clan Basher");
        turnedCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(turnedCreature));
        resolveAllTriggers();

        assertThat(turnedCreature.isFaceDown()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 15);
    }

    @Test
    void damageUsesPowerAtResolution() {
        Permanent performer = castFaceDownPerformer(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.turnFaceUp(player1, 0);

        harness.setHand(player1, List.of(new AuspiciousArrival()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, performer.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 15);
    }

    @Test
    void damageUsesLastKnownPowerWhenTurnedCreatureDiesBeforeResolution() {
        Permanent performer = castFaceDownPerformer(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.turnFaceUp(player1, 0);
        harness.setHand(player1, List.of(new AuspiciousArrival()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, performer.getId());

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, performer.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Pyrotechnic Performer");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 15);
    }

    @Test
    void faceDownPerformerDoesNotWatchAnotherCreatureTurningFaceUp() {
        Permanent first = castFaceDownPerformer(player1);
        Permanent second = castFaceDownPerformer(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(second));
        resolveAllTriggers();

        assertThat(first.isFaceDown()).isTrue();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
    }

    @Test
    void opposingCreatureTurningFaceUpDoesNotTriggerYourPerformer() {
        addCreatureReady(player1, new PyrotechnicPerformer());
        Permanent opposingPerformer = castFaceDownPerformer(player2);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.turnFaceUp(player2, gd.playerBattlefields.get(player2.getId()).indexOf(opposingPerformer));
        resolveAllTriggers();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    void eachFaceUpPerformerTriggersWhenAnotherPerformerTurnsFaceUp() {
        addCreatureReady(player1, new PyrotechnicPerformer());
        Permanent turnedCreature = castFaceDownPerformer(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(turnedCreature));
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 14);
    }

    @Test
    void disguiseWardCountersOpponentsSpellWhenTheyCannotPay() {
        Permanent performer = castFaceDownPerformer(player1);
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, performer.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Pyrotechnic Performer");
        harness.assertInGraveyard(player2, "Murder");
        assertThat(performer.isFaceDown()).isTrue();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void turningFaceUpRequiresRedMana() {
        Permanent performer = castFaceDownPerformer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(performer.isFaceDown()).isTrue();
        harness.assertLife(player2, 20);
    }

    @Test
    void castingNormallyDoesNotTriggerFaceUpAbility() {
        harness.setHand(player1, List.of(new PyrotechnicPerformer()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Pyrotechnic Performer");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private Permanent castFaceDownPerformer(Player player) {
        harness.forceActivePlayer(player);
        harness.setHand(player, List.of(new PyrotechnicPerformer()));
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player, 0);
        resolveAllTriggers();
        return findPermanents(player, "Pyrotechnic Performer").getLast();
    }
}
