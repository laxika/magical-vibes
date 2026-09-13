package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Dromosaur;
import com.github.laxika.magicalvibes.cards.e.ExtractorDemon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Curfew")
@CardUsed({Curfew.class, CoralMerfolk.class, Dromosaur.class, Forest.class})
class CurfewTest extends BaseCardTest {

    @Test
    @DisplayName("Each player chooses one creature before the chosen creatures return")
    void eachPlayerChoosesBeforeReturns() {
        Permanent player1Merfolk = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());
        Permanent player1Dromosaur = harness.addToBattlefieldAndReturn(player1, new Dromosaur());
        Permanent player2Merfolk = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());
        Permanent player2Dromosaur = harness.addToBattlefieldAndReturn(player2, new Dromosaur());

        castCurfew();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        assertThat(firstChoice.validIds()).containsExactly(player1Merfolk.getId(), player1Dromosaur.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(player1Merfolk.getId()));

        PendingInteraction.MultiPermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(secondChoice.playerId()).isEqualTo(player2.getId());
        assertThat(secondChoice.validIds()).containsExactly(player2Merfolk.getId(), player2Dromosaur.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(player1Merfolk, player1Dromosaur);

        harness.handleMultiplePermanentsChosen(player2, List.of(player2Merfolk.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(player1Dromosaur);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(player2Dromosaur);
        assertThat(gd.playerHands.get(player1.getId())).contains(player1Merfolk.getCard());
        assertThat(gd.playerHands.get(player2.getId())).contains(player2Merfolk.getCard());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Returns the only creature each player controls without prompting")
    void returnsOnlyCreatureWithoutPrompt() {
        Permanent player1Creature = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());
        Permanent player2Creature = harness.addToBattlefieldAndReturn(player2, new Dromosaur());

        castCurfew();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(player1Creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(player2Creature);
        assertThat(gd.playerHands.get(player1.getId())).contains(player1Creature.getCard());
        assertThat(gd.playerHands.get(player2.getId())).contains(player2Creature.getCard());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed(ExtractorDemon.class)
    @DisplayName("A creature returned at the same time as Extractor Demon still triggers it")
    void creatureReturnedWithExtractorDemonTriggersIt() {
        harness.addToBattlefield(player1, new ExtractorDemon());
        harness.addToBattlefield(player2, new CoralMerfolk());

        castCurfew();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Skips players without creatures and leaves noncreatures alone")
    void skipsPlayersWithoutCreatures() {
        Permanent player1Forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent player2Creature = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());

        castCurfew();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(player1Forest);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).contains(player2Creature.getCard());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castCurfew() {
        harness.castFromHand(player1, new Curfew(), "{U}");
    }
}
