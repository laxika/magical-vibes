package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AdamantWill;
import com.github.laxika.magicalvibes.cards.a.AkkiRonin;
import com.github.laxika.magicalvibes.cards.a.AncestralKatana;
import com.github.laxika.magicalvibes.cards.a.AsariCaptain;
import com.github.laxika.magicalvibes.cards.e.EaterOfVirtue;
import com.github.laxika.magicalvibes.cards.e.EiganjoExemplar;
import com.github.laxika.magicalvibes.cards.e.EiganjoUprising;
import com.github.laxika.magicalvibes.cards.u.UnstoppableOgre;
import com.github.laxika.magicalvibes.cards.p.PapercraftDecoy;
import com.github.laxika.magicalvibes.cards.h.HeikoYamazakiTheGeneral;
import com.github.laxika.magicalvibes.cards.i.ImperialSubduer;
import com.github.laxika.magicalvibes.cards.n.NorikaYamazakiThePoet;
import com.github.laxika.magicalvibes.cards.p.PeerlessSamurai;
import com.github.laxika.magicalvibes.cards.r.ReinforcedRonin;
import com.github.laxika.magicalvibes.cards.s.SelflessSamurai;
import com.github.laxika.magicalvibes.cards.s.SunbladeSamurai;
import com.github.laxika.magicalvibes.cards.t.TemperedInSolitude;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImperialBlademaster.class, AdamantWill.class, AkkiRonin.class, AncestralKatana.class,
        AsariCaptain.class, EaterOfVirtue.class, EiganjoExemplar.class, EiganjoUprising.class,
        HeikoYamazakiTheGeneral.class, ImperialSubduer.class, NorikaYamazakiThePoet.class,
        PeerlessSamurai.class, ReinforcedRonin.class, SelflessSamurai.class, SunbladeSamurai.class,
        TemperedInSolitude.class, UnstoppableOgre.class, PapercraftDecoy.class})
class ImperialBlademasterTest extends BaseCardTest {

    @Test
    void samuraiAttackingAloneOffersThreeSpellbookCards() {
        addCreatureReady(player1, new ImperialBlademaster());
        addCreatureReady(player1, new SunbladeSamurai());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice.cards()).hasSize(3);
        assertThat(choice.cards()).extracting(Card::getName).allMatch(Set.of(
                "Adamant Will", "Akki Ronin", "Ancestral Katana", "Asari Captain", "Eater of Virtue",
                "Eiganjo Exemplar", "Eiganjo Uprising", "Heiko Yamazaki, the General", "Imperial Subduer",
                "Norika Yamazaki, the Poet", "Peerless Samurai", "Reinforced Ronin", "Selfless Samurai",
                "Sunblade Samurai", "Tempered in Solitude")::contains);
    }

    @Test
    void warriorAttackingAloneOffersThreeSpellbookCards() {
        addCreatureReady(player1, new ImperialBlademaster());
        addCreatureReady(player1, new UnstoppableOgre());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice.cards()).hasSize(3);
    }

    @Test
    void nonSamuraiOrWarriorAttackingAloneDoesNotTrigger() {
        addCreatureReady(player1, new ImperialBlademaster());
        addCreatureReady(player1, new PapercraftDecoy());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class)).isNull();
    }

    @Test
    void multipleEligibleAttackersDoNotTrigger() {
        addCreatureReady(player1, new ImperialBlademaster());
        addCreatureReady(player1, new SunbladeSamurai());
        addCreatureReady(player1, new UnstoppableOgre());

        declareAttackers(player1, List.of(1, 2));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class)).isNull();
    }

    @Test
    void attackingAloneItselfDraftsExactlyOneChosenCardIntoControllersHand() {
        addCreatureReady(player1, new ImperialBlademaster());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        PendingInteraction.SpellbookDraftChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.cards()).extracting(Card::getName).doesNotHaveDuplicates();
        Card chosen = choice.cards().getFirst();

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class)).isNull();
    }

    @Test
    void opponentsEligibleAttackerDoesNotTrigger() {
        addCreatureReady(player1, new ImperialBlademaster());
        addCreatureReady(player2, new SunbladeSamurai());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class)).isNull();
    }

    @Test
    void eligibleAttackerWithIneligibleCompanionDoesNotTrigger() {
        addCreatureReady(player1, new ImperialBlademaster());
        addCreatureReady(player1, new SunbladeSamurai());
        addCreatureReady(player1, new PapercraftDecoy());

        declareAttackers(player1, List.of(1, 2));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class)).isNull();
    }
}
