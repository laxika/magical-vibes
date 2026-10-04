package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SilverScrutiny;
import com.github.laxika.magicalvibes.cards.s.SummonersPact;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FoundingTheThirdPath.class, Divination.class, Forest.class, Shock.class,
        LightningStrike.class, SilverScrutiny.class, SummonersPact.class})
class FoundingTheThirdPathTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I offers only a low-mana instant or sorcery and casts it for free")
    void chapterICastsLowManaInstantOrSorceryForFree() {
        Shock shock = new Shock();
        Divination tooExpensive = new Divination();
        FoundingTheThirdPath sagaCard = new FoundingTheThirdPath();
        harness.setHand(player1, List.of(sagaCard, shock, tooExpensive));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "1");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).contains(tooExpensive);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Chapter II mills four cards from the chosen player")
    void chapterIIMillsTargetPlayer() {
        List<Card> milled = List.of(new Forest(), new Forest(), new Forest(), new Forest());
        harness.setLibrary(player2, milled);
        Permanent saga = addSagaWithLore(1);

        triggerNextChapter();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(milled);
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Chapter III exiles a graveyard spell and offers its copy for its normal cost")
    void chapterIIIExilesAndCastsGraveyardCopy() {
        Shock shock = new Shock();
        Forest invalidTypeForChapter = new Forest();
        harness.setGraveyard(player1, List.of(shock, invalidTypeForChapter));
        addSagaWithLore(2);

        triggerNextChapter();
        PendingInteraction.MultiGraveyardChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(shock.getId());
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(card -> card.getId().equals(shock.getId()));
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Read ahead can start at chapter II without triggering chapter I")
    void readAheadStartsAtChapterTwo() {
        LightningStrike spell = new LightningStrike();
        harness.setHand(player1, List.of(new FoundingTheThirdPath(), spell));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "2");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Chapter I excludes spells with mana value zero")
    void chapterIDoesNotOfferZeroManaSpell() {
        SummonersPact pact = new SummonersPact();
        harness.setHand(player1, List.of(pact));
        addSagaWithLore(0);

        triggerNextChapter();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(pact);
    }

    @Test
    @DisplayName("Chapter I casts a mana value two spell without paying its mana cost")
    void chapterICastsManaValueTwoSpell() {
        harness.setHand(player1, List.of(new LightningStrike()));
        addSagaWithLore(0);

        triggerNextChapter();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Declining chapter III still exiles the original and removes the uncast copy")
    void chapterIIIExilesOriginalWhenCopyDeclined() {
        LightningStrike spell = new LightningStrike();
        LightningStrike opponentsSpell = new LightningStrike();
        harness.setGraveyard(player1, List.of(spell));
        harness.setGraveyard(player2, List.of(opponentsSpell));
        addSagaWithLore(2);

        triggerNextChapter();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(spell.getId());
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(spell);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(spell.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsSpell);
        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player1, "Founding the Third Path");
    }

    @Test
    @DisplayName("Chapter III does nothing when its target leaves the graveyard before resolution")
    void chapterIIIFizzlesWhenTargetLeavesGraveyard() {
        LightningStrike spell = new LightningStrike();
        harness.setGraveyard(player1, List.of(spell));
        addSagaWithLore(2);

        triggerNextChapter();
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(spell));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.assertNotOnBattlefield(player1, "Founding the Third Path");
    }

    @Test
    @DisplayName("Chapter III allows choosing and paying a nonzero X for the copy")
    void chapterIIICastsCopyWithChosenX() {
        SilverScrutiny spell = new SilverScrutiny();
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        addSagaWithLore(2);

        triggerNextChapter();
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);
        harness.handleXValueChosen(player1, 3);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(spell);
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new FoundingTheThirdPath());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
