package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DreamstoneHedron;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HangarbackWalker;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TroyanGutsyExplorer.class, DreamstoneHedron.class, HangarbackWalker.class,
        HillGiant.class, Island.class, GrizzlyBears.class})
class TroyanGutsyExplorerTest extends BaseCardTest {

    @Test
    @DisplayName("Troyan adds green and blue mana restricted to expensive or X spells")
    void manaAbilityAddsRestrictedMana() {
        addReadyTroyan(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool manaPool = gd.playerManaPools.get(player1.getId());
        assertThat(manaPool.getManaValueAtLeastFiveOrXOnlyMana(ManaColor.GREEN)).isEqualTo(1);
        assertThat(manaPool.getManaValueAtLeastFiveOrXOnlyMana(ManaColor.BLUE)).isEqualTo(1);
        assertThat(manaPool.get(ManaColor.GREEN)).isZero();
        assertThat(manaPool.get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Troyan's mana casts a spell with mana value at least five")
    void manaCastsExpensiveSpell() {
        addReadyTroyan(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setHand(player1, List.of(new DreamstoneHedron()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof DreamstoneHedron);
    }

    @Test
    @DisplayName("Troyan's mana does not cast a spell with mana value four")
    void manaCannotCastManaValueFourSpell() {
        addReadyTroyan(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new HillGiant()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Troyan's mana casts a spell with X in its mana cost")
    void manaCastsXSpell() {
        addReadyTroyan(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.setHand(player1, List.of(new HangarbackWalker()));

        harness.castArtifact(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof HangarbackWalker);
    }

    @Test
    @DisplayName("Troyan draws a card then discards a card")
    void loots() {
        addReadyTroyan(player1);
        setDeck(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    private void addReadyTroyan(Player player) {
        Permanent permanent = new Permanent(new TroyanGutsyExplorer());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
