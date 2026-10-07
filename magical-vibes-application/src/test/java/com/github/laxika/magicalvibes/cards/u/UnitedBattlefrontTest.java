package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.f.FreshStart;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LoxodonWarhammer;
import com.github.laxika.magicalvibes.cards.m.Mindslaver;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.StormbeaconBlade;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnitedBattlefront.class, DarksteelIngot.class, FreshStart.class, GrizzlyBears.class, LoxodonWarhammer.class, Mindslaver.class, MindStone.class, Plains.class, Shock.class, StormbeaconBlade.class})
class UnitedBattlefrontTest extends BaseCardTest {

    @Test
    @DisplayName("Offers up to two noncreature, nonland permanents with mana value 3 or less")
    void offersEligiblePermanentCards() {
        Card mindStone = new MindStone();
        Card darksteelIngot = new DarksteelIngot();
        Card warhammer = new LoxodonWarhammer();
        setLibrary(mindStone, darksteelIngot, warhammer,
                new Mindslaver(), new GrizzlyBears(), new Plains(), new Shock());

        castAndResolve();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                mindStone.getId(), darksteelIngot.getId(), warhammer.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.randomRemainingToBottom()).isTrue();
    }

    @Test
    @DisplayName("Puts the chosen cards onto the battlefield and bottoms the rest")
    void putsChosenCardsOntoBattlefield() {
        Card mindStone = new MindStone();
        Card darksteelIngot = new DarksteelIngot();
        Card warhammer = new LoxodonWarhammer();
        setLibrary(mindStone, darksteelIngot, warhammer,
                new Mindslaver(), new GrizzlyBears(), new Plains(), new Shock());

        castAndResolve();
        harness.handleMultipleCardsChosen(player1, List.of(mindStone.getId(), darksteelIngot.getId()));

        harness.assertOnBattlefield(player1, "Mind Stone");
        harness.assertOnBattlefield(player1, "Darksteel Ingot");
        harness.assertNotOnBattlefield(player1, "Loxodon Warhammer");
        harness.assertNotOnBattlefield(player1, "Mindslaver");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May put nothing onto the battlefield")
    void mayPutNothing() {
        Card mindStone = new MindStone();
        setLibrary(mindStone, new GrizzlyBears(), new Plains(), new Shock());

        castAndResolve();
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertNotOnBattlefield(player1, "Mind Stone");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayChooseOnlyOneCardFromShortLibrary() {
        Card blade = new StormbeaconBlade();
        Card otherBlade = new StormbeaconBlade();
        harness.setLibrary(player1, List.of(blade, otherBlade));

        castAndResolve();
        harness.handleMultipleCardsChosen(player1, List.of(blade.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(blade.getId()) && !p.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherBlade);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void leavesCardsBelowTopSevenUntouchedAndBottomsUnchosenCards() {
        Card chosen = new StormbeaconBlade();
        List<Card> unchosen = List.of(new Plains(), new Plains(), new Plains(),
                new Plains(), new Plains(), new Plains());
        Card eighth = new StormbeaconBlade();
        Card ninth = new Plains();
        harness.setLibrary(player1, List.of(chosen, unchosen.get(0), unchosen.get(1),
                unchosen.get(2), unchosen.get(3), unchosen.get(4), unchosen.get(5), eighth, ninth));

        castAndResolve();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(8).startsWith(eighth, ninth);
        assertThat(library.subList(2, 8)).containsExactlyInAnyOrderElementsOf(unchosen);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(p -> p.getCard().getId()).contains(chosen.getId()).doesNotContain(eighth.getId());
    }

    @Test
    void noEligibleCardsAreReturnedToLibraryWithoutAChoice() {
        Card first = new Plains();
        Card second = new Plains();
        harness.setLibrary(player1, List.of(first, second));

        castAndResolve();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void resolvesWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());

        castAndResolve();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "United Battlefront");
    }

    @Test
    void unchosenAuraWithNoLegalAttachmentIsBottomed() {
        Card aura = new FreshStart();
        Card blade = new StormbeaconBlade();
        harness.setLibrary(player1, List.of(aura, blade));

        castAndResolve();
        harness.handleMultipleCardsChosen(player1, List.of(blade.getId()));

        harness.assertOnBattlefield(player1, "Stormbeacon Blade");
        harness.assertNotOnBattlefield(player1, "Fresh Start");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(aura);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(aura);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void auraSelectedAlongsideArtifactEntersAttachedToExistingCreature() {
        var host = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card aura = new FreshStart();
        Card blade = new StormbeaconBlade();
        harness.setLibrary(player1, List.of(aura, blade));

        castAndResolve();
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId(), blade.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(aura.getId())
                        && host.getId().equals(p.getAttachedTo()));
        harness.assertOnBattlefield(player1, "Stormbeacon Blade");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new UnitedBattlefront()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
