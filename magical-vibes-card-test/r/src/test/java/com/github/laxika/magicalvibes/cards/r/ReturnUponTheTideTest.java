package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.j.JasperaSentinel;
import com.github.laxika.magicalvibes.cards.f.FearlessPup;
import com.github.laxika.magicalvibes.cards.d.DemonicGifts;
import com.github.laxika.magicalvibes.cards.g.GoldveinPick;
import com.github.laxika.magicalvibes.cards.m.MoritteOfTheFrost;
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

@CardUsed({ReturnUponTheTide.class, JasperaSentinel.class, FearlessPup.class, DemonicGifts.class,
        MoritteOfTheFrost.class, GoldveinPick.class})
class ReturnUponTheTideTest extends BaseCardTest {

    @Test
    @DisplayName("Returns an Elf and creates two Elf Warrior tokens")
    void returnsElfAndCreatesTokens() {
        Card elf = new JasperaSentinel();
        harness.setGraveyard(player1, List.of(elf));
        harness.setHand(player1, List.of(new ReturnUponTheTide()));
        addNormalMana();

        harness.castAndResolveSorcery(player1, 0, elf.getId());

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
        Card creature = new FearlessPup();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new ReturnUponTheTide()));
        addNormalMana();

        harness.castAndResolveSorcery(player1, 0, creature.getId());

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
        Card elf = new JasperaSentinel();
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
        Card instant = new DemonicGifts();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new ReturnUponTheTide()));
        addNormalMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot return a creature from an opponent's graveyard")
    void cannotTargetOpponentsCreature() {
        Card elf = new JasperaSentinel();
        harness.setGraveyard(player2, List.of(elf));
        harness.setHand(player1, List.of(new ReturnUponTheTide()));
        addNormalMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, elf.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creates no tokens when the target leaves the graveyard before resolution")
    void removedTargetCreatesNoTokens() {
        Card elf = new JasperaSentinel();
        harness.setGraveyard(player1, List.of(elf));
        harness.setHand(player1, List.of(new ReturnUponTheTide()));
        addNormalMana();
        harness.castSorcery(player1, 0, elf.getId());

        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(elf));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Return Upon the Tide");
    }

    @Test
    @DisplayName("A foretold card cannot be cast on the turn it was foretold")
    void cannotCastOnForetellTurn() {
        ReturnUponTheTide spell = new ReturnUponTheTide();
        Card elf = new JasperaSentinel();
        harness.setGraveyard(player1, List.of(elf));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId(), elf.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Checks Elf type after Moritte enters as a noncreature copy")
    void moritteCopyingEquipmentDoesNotCreateElfTokens() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new GoldveinPick());
        Card moritte = new MoritteOfTheFrost();
        harness.setGraveyard(player1, List.of(moritte));
        harness.setHand(player1, List.of(new ReturnUponTheTide()));
        addNormalMana();

        harness.castAndResolveSorcery(player1, 0, moritte.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, equipment.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        harness.assertNotInGraveyard(player1, "Moritte of the Frost");
    }

    private void addNormalMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
