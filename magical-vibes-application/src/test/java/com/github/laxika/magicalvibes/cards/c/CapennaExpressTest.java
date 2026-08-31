package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CapennaExpress.class, GrizzlyBears.class})
class CapennaExpressTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Treasure animates Capenna Express")
    void sacrificingTreasureAnimatesExpress() {
        Permanent express = addExpressReady(player1);
        Permanent treasure = harness.addToBattlefieldAndReturn(player1, createTreasureToken());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, express)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(treasure);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(treasure.getCard());
    }

    @Test
    @DisplayName("The sacrifice ability cannot use a non-Treasure artifact")
    void cannotSacrificeNonTreasureArtifact() {
        addExpressReady(player1);
        addArtifact(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Crew 3 animates Capenna Express and taps enough creatures")
    void crewAnimatesExpress() {
        Permanent express = addExpressReady(player1);
        Permanent firstCrew = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCrew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, express)).isTrue();
        assertThat(firstCrew.isTapped()).isTrue();
        assertThat(secondCrew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Capenna Express stops being a creature at end of turn")
    void animationResetsAtEndOfTurn() {
        Permanent express = addExpressReady(player1);
        harness.addToBattlefield(player1, createTreasureToken());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, express)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, express)).isFalse();
    }

    private Permanent addExpressReady(Player player) {
        Permanent permanent = new Permanent(new CapennaExpress());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addArtifact(Player player) {
        Card card = new Card();
        card.setName("Artifact");
        card.setType(CardType.ARTIFACT);
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
    }

    private Card createTreasureToken() {
        Card card = new Card();
        card.setName("Treasure");
        card.setType(CardType.ARTIFACT);
        card.setSubtypes(List.of(CardSubtype.TREASURE));
        card.setToken(true);
        return card;
    }
}
