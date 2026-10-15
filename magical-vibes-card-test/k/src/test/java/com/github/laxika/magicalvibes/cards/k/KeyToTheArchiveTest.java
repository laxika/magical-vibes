package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.ApproachOfTheSecondSun;
import com.github.laxika.magicalvibes.cards.c.ClaimTheFirstborn;
import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.d.DayOfJudgment;
import com.github.laxika.magicalvibes.cards.d.DemonicTutor;
import com.github.laxika.magicalvibes.cards.d.Despark;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.e.Electrolyze;
import com.github.laxika.magicalvibes.cards.g.GrowthSpiral;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.l.LightningHelix;
import com.github.laxika.magicalvibes.cards.p.Putrefy;
import com.github.laxika.magicalvibes.cards.r.Regrowth;
import com.github.laxika.magicalvibes.cards.t.TimeWarp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KeyToTheArchive.class, ApproachOfTheSecondSun.class, ClaimTheFirstborn.class,
        Counterspell.class, DayOfJudgment.class, DemonicTutor.class, Despark.class,
        DoomBlade.class, Electrolyze.class, GrowthSpiral.class, KrosanGrip.class,
        LightningBolt.class, LightningHelix.class, Putrefy.class, Regrowth.class,
        TimeWarp.class, GrizzlyBears.class})
class KeyToTheArchiveTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped, drafts, then makes its controller discard")
    void entersDraftsThenDiscards() {
        Card discard = new GrizzlyBears();
        harness.setHand(player1, List.of(new KeyToTheArchive(), discard));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent key = findPermanent(player1, "Key to the Archive");
        assertThat(key.isTapped()).isTrue();

        PendingInteraction.SpellbookDraftChoice draft =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(draft).isNotNull();
        assertThat(draft.cards()).hasSize(3);

        Card drafted = draft.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(discard));

        assertThat(gd.playerHands.get(player1.getId())).contains(drafted);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Adds two mana with independently chosen colors")
    void addsTwoManaInAnyColorCombination() {
        Permanent key = harness.addToBattlefieldAndReturn(player1, new KeyToTheArchive());
        key.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(key.isTapped()).isTrue();
    }

    @Test
    @DisplayName("With no other cards in hand, the drafted card must be discarded")
    void discardsDraftedCardFromOtherwiseEmptyHand() {
        harness.setHand(player1, List.of(new KeyToTheArchive()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        PendingInteraction.SpellbookDraftChoice draft =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(draft).isNotNull();
        assertThat(draft.playerId()).isEqualTo(player1.getId());
        assertThat(draft.cards()).hasSize(3);
        assertThat(draft.cards()).extracting(Card::getName).doesNotHaveDuplicates();

        Card drafted = draft.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drafted);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drafted);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Both mana may have the same color and the mana ability resolves without the stack")
    void addsTwoManaOfTheSameColorImmediately() {
        Permanent key = harness.addToBattlefieldAndReturn(player1, new KeyToTheArchive());

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, ManaColor.GREEN.name());
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(key.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
