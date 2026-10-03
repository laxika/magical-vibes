package com.github.laxika.magicalvibes.cards.c;

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

@CardUsed({CapennaExpress.class, CivilServant.class, ChromeCat.class})
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
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(treasure.getCard());
    }

    @Test
    @DisplayName("The sacrifice ability cannot use a non-Treasure artifact")
    void cannotSacrificeNonTreasureArtifact() {
        addExpressReady(player1);
        harness.addToBattlefield(player1, new ChromeCat());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Crew 3 animates Capenna Express and taps enough creatures")
    void crewAnimatesExpress() {
        Permanent express = addExpressReady(player1);
        Permanent firstCrew = addCreatureReady(player1, new CivilServant());
        Permanent secondCrew = addCreatureReady(player1, new CivilServant());

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

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, express)).isFalse();
    }

    @Test
    @DisplayName("Treasure is sacrificed as a cost before the animation resolves")
    void treasureIsPaidBeforeResolution() {
        Permanent express = addExpressReady(player1);
        Permanent treasure = harness.addToBattlefieldAndReturn(player1, createTreasureToken());
        treasure.tap();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(treasure);
        assertThat(gqs.isCreature(gd, express)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.getEffectiveCardTypes(gd, express)).contains(CardType.ARTIFACT, CardType.CREATURE);
        assertThat(express.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's Treasure cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsTreasure() {
        Permanent express = addExpressReady(player1);
        Permanent treasure = harness.addToBattlefieldAndReturn(player2, createTreasureToken());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(treasure);
        assertThat(gqs.isCreature(gd, express)).isFalse();
    }

    @Test
    @DisplayName("Crew fails without three total power and leaves creatures untapped")
    void insufficientPowerCannotCrew() {
        Permanent express = addExpressReady(player1);
        Permanent crew = addCreatureReady(player1, new CivilServant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(crew.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, express)).isFalse();
    }

    @Test
    @DisplayName("A summoning-sick creature can crew, paying the cost before resolution")
    void summoningSickCreatureCanCrew() {
        Permanent express = harness.addToBattlefieldAndReturn(player1, new CapennaExpress());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new ChromeCat());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, express)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, express)).isTrue();
        assertThat(express.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Crew animation lasts through the end step and expires at cleanup")
    void crewAnimationExpiresAtCleanup() {
        Permanent express = addExpressReady(player1);
        harness.addToBattlefield(player1, new ChromeCat());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gqs.isCreature(gd, express)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, express)).isFalse();
    }

    private Permanent addExpressReady(Player player) {
        return addCreatureReady(player, new CapennaExpress());
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
