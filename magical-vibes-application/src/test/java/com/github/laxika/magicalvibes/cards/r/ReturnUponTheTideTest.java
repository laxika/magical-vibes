package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReturnUponTheTide.class, ElvishMystic.class, GrizzlyBears.class, HolyDay.class})
class ReturnUponTheTideTest extends BaseCardTest {

    @Test
    @DisplayName("Returns an Elf and creates two Elf Warrior tokens")
    void returnsElfAndCreatesTokens() {
        Card elf = new ElvishMystic();
        harness.setGraveyard(player1, List.of(elf));
        harness.setHand(player1, List.of(new ReturnUponTheTide()));
        addNormalMana();

        harness.castSorcery(player1, 0, elf.getId());
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(3);
        assertThat(battlefield).anyMatch(permanent -> permanent.getCard().getId().equals(elf.getId()));

        List<Permanent> tokens = battlefield.stream()
                .filter(permanent -> !permanent.getCard().getId().equals(elf.getId()))
                .toList();
        assertThat(tokens).hasSize(2).allSatisfy(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ELF, CardSubtype.WARRIOR);
            assertThat(token.getEffectivePower()).isEqualTo(1);
            assertThat(token.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Returns a non-Elf without creating tokens")
    void returnsNonElfWithoutTokens() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new ReturnUponTheTide()));
        addNormalMana();

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement()
                .extracting(Permanent::getCard)
                .extracting(Card::getId)
                .isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Can be foretold and cast for its foretell cost")
    void foretellsAndCastsOnALaterTurn() {
        ReturnUponTheTide spell = new ReturnUponTheTide();
        Card elf = new ElvishMystic();
        harness.setGraveyard(player1, List.of(elf));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);
        ExiledCardEntry entry = gd.findExiledCard(spell.getId());
        assertThat(entry).isNotNull();

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromExile(player1, spell.getId(), elf.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Cannot target a non-creature card in the graveyard")
    void cannotTargetNonCreatureCard() {
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new ReturnUponTheTide()));
        addNormalMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addNormalMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
