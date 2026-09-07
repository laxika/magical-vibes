package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HomicideInvestigator.class, GrizzlyBears.class, Shock.class, WrathOfGod.class})
class HomicideInvestigatorTest extends BaseCardTest {

    @Test
    void investigatesWhenANontokenCreatureYouControlDies() {
        harness.addToBattlefield(player1, new HomicideInvestigator());
        harness.addToBattlefield(player1, new GrizzlyBears());

        killWithShock(player2, player1);

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void doesNotInvestigateWhenATokenCreatureYouControlDies() {
        harness.addToBattlefield(player1, new HomicideInvestigator());
        Card token = new GrizzlyBears();
        token.setToken(true);
        harness.addToBattlefield(player1, token);

        killWithShock(player2, player1);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void doesNotInvestigateWhenAnOpponentsCreatureDies() {
        harness.addToBattlefield(player1, new HomicideInvestigator());
        harness.addToBattlefield(player2, new GrizzlyBears());

        killWithShock(player1, player2);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void investigatesOnlyOnceForSimultaneousDeaths() {
        harness.addToBattlefield(player1, new HomicideInvestigator());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);

        harness.getGameService().playCard(harness.getGameData(), player2, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void doesNotInvestigateAgainLaterInTheSameTurn() {
        harness.addToBattlefield(player1, new HomicideInvestigator());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        killWithShock(player2, player1);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);

        UUID remainingBearId = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .map(Permanent::getId)
                .findFirst()
                .orElseThrow();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, remainingBearId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    private void killWithShock(com.github.laxika.magicalvibes.model.Player caster,
                               com.github.laxika.magicalvibes.model.Player targetController) {
        harness.forceActivePlayer(caster);
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        UUID targetId = harness.getPermanentId(targetController, "Grizzly Bears");
        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
