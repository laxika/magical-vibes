package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LilianasTalent.class, GrizzlyBears.class, HolyDay.class, ProdigalPyromancer.class, ChandraNalaar.class})
class LilianasTalentTest extends BaseCardTest {

    @Test
    void cannotEnchantNonPlaneswalker() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        LilianasTalent talent = new LilianasTalent();
        harness.setHand(player1, List.of(talent));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a planeswalker");
    }

    @Test
    void ultimateReturnsCreatureCardsFromAllGraveyardsUnderYourControl() {
        Permanent planeswalker = addPlaneswalker(player1, 8);
        attachTalent(planeswalker);
        Card ownCreature = new GrizzlyBears();
        Card opposingCreature = new GrizzlyBears();
        Card nonCreature = new HolyDay();
        harness.setGraveyard(player1, List.of(ownCreature, nonCreature));
        harness.setGraveyard(player2, List.of(opposingCreature));

        int planeswalkerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(planeswalker);
        harness.activateAbility(player1, planeswalkerIndex, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard())
                .contains(ownCreature, opposingCreature);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(nonCreature)
                .doesNotContain(ownCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void anyCreatureThatDamagesEnchantedPlaneswalkerIsDestroyed() {
        Permanent planeswalker = addPlaneswalker(player1, 4);
        attachTalent(planeswalker);
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(pyromancer), null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(pyromancer);
    }

    @Test
    void damageToAnotherPlaneswalkerDoesNotTriggerDestruction() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new ChandraNalaar());
        enchanted.setCounterCount(CounterType.LOYALTY, 6);
        attachTalent(enchanted);
        Permanent other = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        other.setCounterCount(CounterType.LOYALTY, 6);
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(pyromancer), null, other.getId());
        resolveAllTriggers();

        assertThat(other.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(pyromancer);
    }

    @Test
    void auraControllerControlsDestructionTriggerOnOpposingPlaneswalker() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        enchanted.setCounterCount(CounterType.LOYALTY, 6);
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new LilianasTalent());
        talent.setAttachedTo(enchanted.getId());
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(pyromancer), null, enchanted.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(talent.getId());
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(pyromancer);
    }

    @Test
    void lethalDamageStillTriggersDestruction() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new ChandraNalaar());
        enchanted.setCounterCount(CounterType.LOYALTY, 1);
        attachTalent(enchanted);
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(pyromancer), null, enchanted.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(enchanted);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(pyromancer);
    }
    private void attachTalent(Permanent planeswalker) {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new LilianasTalent());
        talent.setAttachedTo(planeswalker.getId());
    }

    private Permanent addPlaneswalker(Player player, int loyalty) {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        return permanent;
    }
}
