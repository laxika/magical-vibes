package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BishopOfWings;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.StarlitAngel;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ValkyriesCallTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a nontoken non-Angel creature with a counter, flying, and Angel subtype")
    void returnsNontokenNonAngelCreatureWithRiders() {
        harness.addToBattlefield(player1, new ValkyriesCall());
        harness.addToBattlefield(player1, new GrizzlyBears());

        destroyWithMurder(player2, player1, "Grizzly Bears");
        harness.passBothPriorities();

        Permanent returned = findPermanents(player1, "Grizzly Bears").getFirst();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FLYING)).isTrue();
        assertThat(returned.getGrantedSubtypes()).contains(CardSubtype.ANGEL);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not return an Angel or a token")
    void doesNotReturnAngelOrToken() {
        harness.addToBattlefield(player1, new ValkyriesCall());
        harness.addToBattlefield(player1, new BishopOfWings());
        harness.addToBattlefield(player1, new StarlitAngel());

        destroyWithMurder(player2, player1, "Starlit Angel");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Starlit Angel");
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        destroyWithMurder(player2, player1, "Spirit");

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    private void destroyWithMurder(Player caster, Player targetController, String targetName) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Murder()));
        harness.addMana(caster, ManaColor.BLACK, 3);

        UUID targetId = harness.getPermanentId(targetController, targetName);
        harness.getGameService().playCard(harness.getGameData(), caster, 0, 0, targetId, null);
        harness.passBothPriorities();
    }
}
