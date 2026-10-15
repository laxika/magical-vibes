package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.e.ElvishVisionary;
import com.github.laxika.magicalvibes.cards.f.FirstTimeFlyer;
import com.github.laxika.magicalvibes.cards.f.FireNationPalace;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SouthPoleVoyager;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KataraTheFearless.class, SouthPoleVoyager.class, FirstTimeFlyer.class,
        ElvishVisionary.class, GrizzlyBears.class, FireNationPalace.class})
class KataraTheFearlessTest extends BaseCardTest {

    @Test
    @DisplayName("Katara doubles an Ally's triggered ability")
    void doublesAllyTriggeredAbility() {
        addCreatureReady(player1, new KataraTheFearless());
        addCreatureReady(player1, new SouthPoleVoyager());

        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        int lifeBefore = gd.getLife(player1.getId());

        harness.castFromHand(player1, new FirstTimeFlyer(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Katara does not double a non-Ally's triggered ability")
    void doesNotDoubleNonAllyTriggeredAbility() {
        addCreatureReady(player1, new KataraTheFearless());

        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.castFromHand(player1, new ElvishVisionary(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Katara doubles a triggered ability granted to herself")
    void doublesGrantedTriggerOnKatara() {
        Permanent katara = addCreatureReady(player1, new KataraTheFearless());
        harness.addToBattlefield(player1, new FireNationPalace());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 1, null, katara.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(8);
    }

    @Test
    @DisplayName("Katara doubles a triggered ability granted to another Ally")
    void doublesGrantedTriggerOnAnotherAlly() {
        addCreatureReady(player1, new KataraTheFearless());
        Permanent ally = addCreatureReady(player1, new FirstTimeFlyer());
        harness.addToBattlefield(player1, new FireNationPalace());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 2, 1, null, ally.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(1));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(8);
    }

    @Test
    @DisplayName("Katara does not double an opponent's Ally trigger")
    void doesNotDoubleOpponentsAllyTrigger() {
        addCreatureReady(player1, new KataraTheFearless());
        addCreatureReady(player2, new SouthPoleVoyager());
        int lifeBefore = gd.getLife(player2.getId());
        harness.setLibrary(player2, List.of(new FirstTimeFlyer(), new FirstTimeFlyer()));
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new FirstTimeFlyer(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Katara doubles an Ally trigger caused by her own entry")
    void doublesAllyTriggerWhenKataraEnters() {
        addCreatureReady(player1, new SouthPoleVoyager());
        FirstTimeFlyer drawnCard = new FirstTimeFlyer();
        harness.setLibrary(player1, List.of(drawnCard));
        int lifeBefore = gd.getLife(player1.getId());

        harness.castFromHand(player1, new KataraTheFearless(), "{G}{W}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }
}
