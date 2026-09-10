package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.b.BoggartBrute;
import com.github.laxika.magicalvibes.cards.f.FaerieMiscreant;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZagrasThiefOfHeartbeats.class, BoggartBrute.class, FaerieMiscreant.class,
        FugitiveWizard.class, GrizzlyBears.class, ProdigalPyromancer.class, SoulWarden.class})
class ZagrasThiefOfHeartbeatsTest extends BaseCardTest {

    @Test
    @DisplayName("A full party reduces Zagras's generic casting cost by four")
    void fullPartyReducesCastingCost() {
        addFullParty();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        ZagrasThiefOfHeartbeats zagras = new ZagrasThiefOfHeartbeats();
        harness.setHand(player1, List.of(zagras));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == zagras);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Zagras still requires its generic mana without a party")
    void requiresGenericManaWithoutParty() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ZagrasThiefOfHeartbeats()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void grantsDeathtouchToOtherCreaturesYouControl() {
        addCreatureReady(player1, new ZagrasThiefOfHeartbeats());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void combatDamageToPlaneswalkerDestroysIt() {
        addCreatureReady(player1, new ZagrasThiefOfHeartbeats());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent planeswalker = addPlaneswalker(player2, 4);

        declareAttackerAtPlaneswalker(attacker, planeswalker);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(planeswalker);
    }

    @Test
    void noncombatDamageToPlaneswalkerDoesNotTriggerDestruction() {
        addCreatureReady(player1, new ZagrasThiefOfHeartbeats());
        addCreatureReady(player1, new ProdigalPyromancer());
        Permanent planeswalker = addPlaneswalker(player2, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 1, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(planeswalker);
    }

    private void addFullParty() {
        harness.addToBattlefield(player1, new SoulWarden());
        harness.addToBattlefield(player1, new FaerieMiscreant());
        harness.addToBattlefield(player1, new BoggartBrute());
        harness.addToBattlefield(player1, new FugitiveWizard());
    }

    private void declareAttackerAtPlaneswalker(Permanent attacker, Permanent planeswalker) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareAttackers(gd, player1, List.of(attackerIndex), Map.of(attackerIndex, planeswalker.getId()));
    }

    private Permanent addPlaneswalker(Player player, int loyalty) {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        Permanent permanent = new Permanent(card);
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
