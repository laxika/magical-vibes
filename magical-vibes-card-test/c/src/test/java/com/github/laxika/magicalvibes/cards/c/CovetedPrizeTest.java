package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.e.ExpeditionDiviner;
import com.github.laxika.magicalvibes.cards.e.ExpeditionHealer;
import com.github.laxika.magicalvibes.cards.e.ExpeditionSkulker;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MesaLynx;
import com.github.laxika.magicalvibes.cards.s.SeaGateColossus;
import com.github.laxika.magicalvibes.cards.s.StoneworkPackbeast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CovetedPrize.class, CliffhavenSellSword.class, ExpeditionSkulker.class,
        Forest.class, ExpeditionDiviner.class, MesaLynx.class, SeaGateColossus.class, ExpeditionHealer.class,
        Disenchant.class, StoneworkPackbeast.class})
class CovetedPrizeTest extends BaseCardTest {

    @Test
    @DisplayName("A full party reduces Coveted Prize's generic cost by four")
    void fullPartyReducesCostByFour() {
        addFullParty();
        harness.setHand(player1, List.of(new CovetedPrize()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Coveted Prize cannot be cast with insufficient mana without a party")
    void insufficientManaWithoutParty() {
        harness.setHand(player1, List.of(new CovetedPrize()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A full party offers a spell with mana value four or less from hand")
    void fullPartyOffersLowManaValueSpell() {
        addFullParty();
        MesaLynx bears = new MesaLynx();
        harness.setHand(player1, List.of(new CovetedPrize(), bears));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(bears.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Without a full party, Coveted Prize does not offer a free spell")
    void noFullPartyDoesNotOfferFreeCast() {
        MesaLynx bears = new MesaLynx();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new CovetedPrize(), bears));
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(bears, forest).hasSize(2);
    }

    @Test
    @DisplayName("A full party does not offer a spell with mana value greater than four")
    void fullPartyDoesNotOfferHighManaValueSpell() {
        addFullParty();
        SeaGateColossus angel = new SeaGateColossus();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new CovetedPrize(), angel));
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(angel, forest).hasSize(2);
    }

    @Test
    void searchedManaValueFourSpellCanBeCastForFree() {
        addFullParty();
        ExpeditionDiviner diviner = new ExpeditionDiviner();
        harness.setHand(player1, List.of(new CovetedPrize()));
        harness.setLibrary(player1, List.of(diviner));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(diviner.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void mayDeclineFreeSpell() {
        addFullParty();
        MesaLynx lynx = new MesaLynx();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new CovetedPrize(), lynx));
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lynx, forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mayCastOnlyOneSpell() {
        addFullParty();
        MesaLynx first = new MesaLynx();
        MesaLynx second = new MesaLynx();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new CovetedPrize(), first, second));
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second, forest);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(first.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(second.getId()));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryStillAllowsFreeSpell() {
        addFullParty();
        MesaLynx lynx = new MesaLynx();
        harness.setHand(player1, List.of(new CovetedPrize(), lynx));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(lynx.getId()));
    }

    @Test
    void oneCreatureWithAllPartyTypesCountsOnlyOnce() {
        harness.addToBattlefield(player1, new StoneworkPackbeast());
        MesaLynx lynx = new MesaLynx();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new CovetedPrize(), lynx));
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lynx, forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void spellWithoutLegalTargetsStaysInHand() {
        addFullParty();
        Disenchant disenchant = new Disenchant();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new CovetedPrize(), disenchant));
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(disenchant, forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(disenchant);
        assertThat(gd.stack).isEmpty();
    }

    private void addFullParty() {
        harness.addToBattlefield(player1, new ExpeditionHealer());
        harness.addToBattlefield(player1, new ExpeditionSkulker());
        harness.addToBattlefield(player1, new CliffhavenSellSword());
        harness.addToBattlefield(player1, new ExpeditionDiviner());
    }
}
