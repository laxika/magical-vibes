package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RollingTemblor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SorceresssSchemes.class, HolyDay.class, RollingTemblor.class, GrizzlyBears.class})
class SorceresssSchemesTest extends BaseCardTest {

    @Test
    @DisplayName("Returns an instant or sorcery from the graveyard and adds red mana")
    void returnsSpellFromGraveyardAndAddsRedMana() {
        Card target = new HolyDay();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new SorceresssSchemes()));
        addSorceresssSchemesMana(player1);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Returns an owned exiled card with flashback")
    void returnsOwnedExiledFlashbackCard() {
        Card target = new RollingTemblor();
        harness.setExile(player1, List.of(target));
        harness.setHand(player1, List.of(new SorceresssSchemes()));
        addSorceresssSchemesMana(player1);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(target);
        assertThat(gd.getPlayerExiledCards(player1.getId())).noneMatch(card -> card.getId().equals(target.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Flashback can return an owned exiled flashback card")
    void flashbackReturnsOwnedExiledFlashbackCard() {
        Card source = new SorceresssSchemes();
        Card target = new RollingTemblor();
        harness.setGraveyard(player1, List.of(source));
        harness.setExile(player1, List.of(target));
        addFlashbackMana(player1);

        harness.castFlashback(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(target);
        assertThat(gd.getPlayerExiledCards(player1.getId())).noneMatch(card -> card.getId().equals(target.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(card -> card.getId().equals(source.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Rejects an invalid graveyard or exile target")
    void rejectsInvalidTarget() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new SorceresssSchemes()));
        addSorceresssSchemesMana(player1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addSorceresssSchemesMana(Player player) {
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 3);
    }

    private void addFlashbackMana(Player player) {
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 4);
    }
}
