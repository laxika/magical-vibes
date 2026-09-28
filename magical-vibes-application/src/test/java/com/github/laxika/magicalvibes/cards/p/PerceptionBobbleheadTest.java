package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantOctopus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PerceptionBobblehead.class, Forest.class, GiantOctopus.class, GrizzlyBears.class,
        LlanowarElves.class, Mountain.class})
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
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.CREATURE_SPELL
                && entry.getCard() == chosenSpell);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(secondSpell, forest, mountain);
    }
}
