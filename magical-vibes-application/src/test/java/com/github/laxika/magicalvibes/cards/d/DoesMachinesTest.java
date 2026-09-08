package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoesMachines.class, DarksteelRelic.class, Forest.class, GrizzlyBears.class, Memnite.class})
class DoesMachinesTest extends BaseCardTest {

    @Test
    @DisplayName("Mills two, draws two, then discards two when it enters")
    void entersAndLoots() {
        Card milled1 = new GrizzlyBears();
        Card milled2 = new Forest();
        Card drawn1 = new Memnite();
        Card drawn2 = new DarksteelRelic();
        Card discarded1 = new GrizzlyBears();
        Card discarded2 = new Forest();

        harness.setHand(player1, new ArrayList<>(List.of(new DoesMachines(), discarded1, discarded2)));
        harness.setLibrary(player1, List.of(milled1, milled2, drawn1, drawn2));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(discarded1));
        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(discarded2));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(drawn1, drawn2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(milled1, milled2, discarded1, discarded2);
    }

    @Test
    @DisplayName("Level 2 returns up to two artifact cards from its controller's graveyard")
    void levelTwoReturnsArtifacts() {
        Card artifact1 = new DarksteelRelic();
        Card artifact2 = new Memnite();
        Card nonArtifact = new Forest();
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new DoesMachines());
        harness.setGraveyard(player1, List.of(artifact1, nonArtifact, artifact2));

        levelUp(talent, 0, 1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(artifact1.getId(), artifact2.getId());

        harness.handleMultipleCardsChosen(player1, List.of(artifact1.getId(), artifact2.getId()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(artifact1, artifact2);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonArtifact);
    }

    @Test
    @DisplayName("Level 3 puts counters on and animates a noncreature artifact you control")
    void levelThreeAnimatesNoncreatureArtifact() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new DoesMachines());
        Permanent relic = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        Permanent opposingRelic = harness.addToBattlefieldAndReturn(player2, new DarksteelRelic());
        levelUp(talent, 0, 1);
        levelUp(talent, 1, 4);

        prepareBeginningOfCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(relic.getId());
        harness.handlePermanentChosen(player1, relic.getId());
        harness.passBothPriorities();

        assertThat(relic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.isArtifact(gd, relic)).isTrue();
        assertThat(gqs.isCreature(gd, relic)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, relic, CardSubtype.ROBOT)).isTrue();
        assertThat(gqs.getEffectivePower(gd, relic)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, relic)).isEqualTo(3);
        assertThat(opposingRelic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Level 3 does not animate an artifact that is already a creature")
    void levelThreeDoesNotReanimateArtifactCreature() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new DoesMachines());
        Permanent memnite = harness.addToBattlefieldAndReturn(player1, new Memnite());
        levelUp(talent, 0, 1);
        levelUp(talent, 1, 4);

        prepareBeginningOfCombat();
        harness.handlePermanentChosen(player1, memnite.getId());
        harness.passBothPriorities();

        assertThat(memnite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.isArtifact(gd, memnite)).isTrue();
        assertThat(gqs.isCreature(gd, memnite)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, memnite, CardSubtype.ROBOT)).isFalse();
        assertThat(gqs.getEffectivePower(gd, memnite)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, memnite)).isEqualTo(4);
    }

    private void prepareBeginningOfCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
    }

    private void levelUp(Permanent talent, int abilityIndex, int genericMana) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, genericMana);
        int talentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(talent);
        harness.activateAbility(player1, talentIndex, abilityIndex, null, null);
        resolveAllTriggers();
    }
}
