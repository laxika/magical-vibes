package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnlicensedHearse.class, GrizzlyBears.class, LightningBolt.class})
class UnlicensedHearseTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles up to two cards from one graveyard and uses them for its power and toughness")
    void exilesCardsAndGetsTheirCountAsPowerAndToughness() {
        Permanent hearse = addReadyHearse(player1);
        Card card1 = new GrizzlyBears();
        Card card2 = new LightningBolt();
        Card card3 = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(card1, card2, card3)));

        harness.activateAbilityWithGraveyardTargets(player1, hearseIndex(hearse), 0,
                List.of(card1.getId(), card2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId).containsExactly(card3.getId());
        assertThat(gd.getCardsExiledByPermanent(hearse.getId()))
                .extracting(Card::getId).containsExactly(card1.getId(), card2.getId());
        assertThat(gqs.getEffectivePower(gd, hearse)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hearse)).isEqualTo(2);
    }

    @Test
    @DisplayName("Targets must all come from a single graveyard")
    void targetsMustShareOneGraveyard() {
        Permanent hearse = addReadyHearse(player1);
        Card mine = new GrizzlyBears();
        Card theirs = new LightningBolt();
        harness.setGraveyard(player1, List.of(mine));
        harness.setGraveyard(player2, List.of(theirs));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, hearseIndex(hearse), 0,
                List.of(mine.getId(), theirs.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single graveyard");
    }

    @Test
    @DisplayName("Crew 2 animates the Hearse and taps the crew")
    void crewAnimatesHearse() {
        Permanent hearse = addReadyHearse(player1);
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, hearseIndex(hearse), 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, hearse)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    private int hearseIndex(Permanent hearse) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(hearse);
    }

    private Permanent addReadyHearse(Player player) {
        Permanent permanent = new Permanent(new UnlicensedHearse());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
