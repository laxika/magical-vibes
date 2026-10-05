package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DeadlyDispute;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantOctopus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IntelligenceBobblehead;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PerceptionBobblehead.class, Forest.class, GiantOctopus.class, GrizzlyBears.class,
        LlanowarElves.class, Mountain.class, IntelligenceBobblehead.class, DeadlyDispute.class})
class PerceptionBobbleheadTest extends BaseCardTest {

    @Test
    void manaAbilityAddsChosenColor() {
        harness.addToBattlefield(player1, new PerceptionBobblehead());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).get(0).isTapped()).isTrue();
    }

    @Test
    void looksAtOneCardPerControlledBobbleheadAndCapsManaValueAtThree() {
        Card eligibleSpell = new GrizzlyBears();
        Card tooExpensive = new GiantOctopus();
        Card leftOnTop = new Mountain();
        harness.addToBattlefield(player1, new PerceptionBobblehead());
        harness.addToBattlefield(player1, new PerceptionBobblehead());
        harness.setLibrary(player1, List.of(eligibleSpell, tooExpensive, leftOnTop));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(eligibleSpell);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(leftOnTop);
    }

    @Test
    void chosenSpellIsCastForFreeAndUnchosenLookedAtCardsGoToBottom() {
        Card chosenSpell = new GrizzlyBears();
        Card secondSpell = new LlanowarElves();
        Card forest = new Forest();
        Card mountain = new Mountain();
        harness.addToBattlefield(player1, new PerceptionBobblehead());
        harness.addToBattlefield(player1, new PerceptionBobblehead());
        harness.setLibrary(player1, List.of(chosenSpell, secondSpell, forest, mountain));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.CREATURE_SPELL
                && entry.getCard() == chosenSpell);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(secondSpell, forest, mountain);
    }

    @Test
    void mayDeclineAndBottomsAllLookedAtCardsWithoutDisturbingTheRest() {
        Card eligible = new IntelligenceBobblehead();
        Card land = new Forest();
        Card untouched = new Mountain();
        harness.addToBattlefield(player1, new PerceptionBobblehead());
        harness.addToBattlefield(player1, new IntelligenceBobblehead());
        harness.setLibrary(player1, List.of(eligible, land, untouched));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(eligible, land);
    }

    @Test
    void castsManaValueThreeArtifactWithoutPayingAndResolvesIt() {
        Card chosen = new IntelligenceBobblehead();
        harness.addToBattlefield(player1, new PerceptionBobblehead());
        harness.setLibrary(player1, List.of(chosen));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(chosen);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Intelligence Bobblehead");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void landsCannotBeCastAndAreBottomedWhenNothingIsEligible() {
        Card land = new Forest();
        Card untouched = new Mountain();
        harness.addToBattlefield(player1, new PerceptionBobblehead());
        harness.setLibrary(player1, List.of(land, untouched));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, land);
    }

    @Test
    void countsDifferentBobbleheadsAtResolutionAndIgnoresOpponentsBobbleheads() {
        Card first = new Forest();
        Card second = new IntelligenceBobblehead();
        Card untouched = new Mountain();
        harness.addToBattlefield(player1, new PerceptionBobblehead());
        harness.addToBattlefield(player2, new PerceptionBobblehead());
        harness.setLibrary(player1, List.of(first, second, untouched));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.addToBattlefield(player1, new IntelligenceBobblehead());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
        harness.handleCardChosen(player1, -1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
    }

    @Test
    void freeSpellStillRequiresItsMandatorySacrificeCostBeforeCastingCompletes() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new PerceptionBobblehead());
        Card spell = new DeadlyDispute();
        harness.setLibrary(player1, List.of(spell, new Forest(), new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        if (gd.interaction.activeInteraction() != null) {
            harness.handlePermanentChosen(player1, source.getId());
        }

        harness.assertInGraveyard(player1, "Perception Bobblehead");
        harness.assertNotOnBattlefield(player1, "Perception Bobblehead");
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == spell);
    }
}
