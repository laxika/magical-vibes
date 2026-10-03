package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BolracClanBasher.class, Shock.class})
class BolracClanBasherTest extends BaseCardTest {

    @Test
    void disguiseCastsAndTurnsBolracClanBasherFaceUp() {
        Permanent basher = castFaceDownBasher();
        assertThat(basher.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.RED, 5);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(basher));

        assertThat(basher.isFaceDown()).isFalse();
    }

    @Test
    void faceDownCreatureCountersOpponentsSpellWhenWardCannotBePaid() {
        Permanent basher = castFaceDownBasher();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, basher.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Bolrac-Clan Basher");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void disguiseCostRequiresTwoRedMana() {
        Permanent basher = castFaceDownBasher();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(basher.isFaceDown()).isTrue();
    }

    @Test
    void faceDownBasherDealsOnlyOneCombatDamageStep() {
        Permanent basher = castFaceDownBasher();
        basher.setSummoningSick(false);
        basher.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    void turnedFaceUpBasherDealsDoubleStrikeCombatDamage() {
        Permanent basher = castFaceDownBasher();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.turnFaceUp(player1, 0);
        basher.setSummoningSick(false);
        basher.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();

        harness.assertLife(player2, 14);
    }

    @Test
    void normalCastDealsDoubleStrikeCombatDamage() {
        harness.setHand(player1, List.of(new BolracClanBasher()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent basher = findPermanent(player1, "Bolrac-Clan Basher");
        basher.setSummoningSick(false);
        basher.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();

        harness.assertLife(player2, 14);
    }

    @Test
    void doubleStrikeTramplesOverBlockerKilledInFirstDamageStep() {
        Permanent basher = addCreatureReady(player1, new BolracClanBasher());
        basher.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BolracClanBasher());
        blocker.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLife(player2, 20);

        resolveCombat();

        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player2, "Bolrac-Clan Basher");
        harness.assertOnBattlefield(player1, "Bolrac-Clan Basher");
    }

    private Permanent castFaceDownBasher() {
        harness.setHand(player1, List.of(new BolracClanBasher()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();
        return findPermanent(player1, "Bolrac-Clan Basher");
    }
}
