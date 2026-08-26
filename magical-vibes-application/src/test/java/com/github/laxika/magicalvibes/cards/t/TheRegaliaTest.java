package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheRegalia.class, Forest.class, GrizzlyBears.class, Shock.class})
class TheRegaliaTest extends BaseCardTest {

    @Test
    void crewAnimatesTheRegaliaAndTapsTheCrew() {
        Permanent regalia = addRegaliaReady(player1);
        Permanent crew = addCreatureReady(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, regalia)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void attackingRevealsLandIntoBattlefieldTappedAndRandomizesTheRestToBottom() {
        addRegaliaReady(player1);
        addCreatureReady(player1);
        Card firstNonland = new Shock();
        Card secondNonland = new GrizzlyBears();
        Card land = new Forest();
        Card topAfterReveal = new Forest();
        harness.setLibrary(player1, List.of(firstNonland, secondNonland, land, topAfterReveal));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == land && permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topAfterReveal);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(firstNonland, secondNonland, topAfterReveal);
    }

    private Permanent addRegaliaReady(Player player) {
        Permanent regalia = new Permanent(new TheRegalia());
        regalia.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(regalia);
        return regalia;
    }

    private Permanent addCreatureReady(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }
}
