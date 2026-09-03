package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GlazeFiend;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.m.MoxAmber;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TezzeretCruelCaptain.class, GlazeFiend.class, GrizzlyBears.class, MindStone.class, MoxAmber.class})
class TezzeretCruelCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("An artifact entering under your control adds a loyalty counter")
    void artifactEnteringUnderYourControlAddsLoyalty() {
        Permanent tezzeret = addReadyTezzeret(player1, 4);

        harness.enterBattlefieldAndReturn(player2, new MindStone());
        Permanent ownArtifact = harness.enterBattlefieldAndReturn(player1, new MindStone());
        resolveAllTriggers();

        assertThat(tezzeret.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(ownArtifact).isIn(gd.playerBattlefields.get(player1.getId()));
    }

    @Test
    @DisplayName("The zero ability untaps an artifact and adds a counter only to an artifact creature")
    void zeroAbilityUntapsAndCountersArtifactCreature() {
        addReadyTezzeret(player1, 4);
        Permanent artifactCreature = addReadyPermanent(player1, new GlazeFiend());
        Permanent creature = addReadyPermanent(player1, new GrizzlyBears());
        artifactCreature.tap();
        creature.tap();

        harness.activateAbility(player1, 0, 0, null, artifactCreature.getId());
        harness.passBothPriorities();

        assertThat(artifactCreature.isTapped()).isFalse();
        assertThat(artifactCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The minus-three ability searches for an artifact with mana value one or less")
    void minusThreeSearchesForCheapArtifact() {
        Permanent tezzeret = addReadyTezzeret(player1, 4);
        Card moxAmber = new MoxAmber();
        Card mindStone = new MindStone();
        harness.setHand(player1, List.of());
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(moxAmber, mindStone));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(moxAmber);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(moxAmber);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(mindStone);
        assertThat(tezzeret.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("The emblem buffs your artifact and animates it as a Robot creature")
    void minusSevenEmblemTargetsYourArtifact() {
        Permanent tezzeret = addReadyTezzeret(player1, 7);
        Permanent firstArtifact = addReadyPermanent(player1, new MindStone());
        Permanent secondArtifact = addReadyPermanent(player1, new MindStone());
        Permanent opponentArtifact = addReadyPermanent(player2, new MindStone());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(tezzeret.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(gd.emblems).hasSize(1);

        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstArtifact.getId(), secondArtifact.getId());
        assertThat(choice.validIds()).doesNotContain(opponentArtifact.getId());

        harness.handlePermanentChosen(player1, firstArtifact.getId());
        harness.passBothPriorities();

        assertThat(firstArtifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.isCreature(gd, firstArtifact)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, firstArtifact)).contains(CardSubtype.ROBOT);
        assertThat(gqs.getEffectivePower(gd, firstArtifact)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, firstArtifact)).isEqualTo(3);
    }

    private Permanent addReadyTezzeret(Player player, int loyalty) {
        Permanent permanent = new Permanent(new TezzeretCruelCaptain());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return permanent;
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
