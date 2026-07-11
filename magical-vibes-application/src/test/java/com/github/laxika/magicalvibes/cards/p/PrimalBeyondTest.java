package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Smokebraider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrimalBeyondTest extends BaseCardTest {

    private static Card createCreature(String name, String manaCost, CardColor color, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost(manaCost);
        card.setColor(color);
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(subtypes));
        return card;
    }

    // ===== Enters tapped / reveal choice =====

    @Test
    @DisplayName("Enters tapped when you have no Elemental card in hand")
    void entersTappedWithoutElemental() {
        harness.setHand(player1, List.of(new PrimalBeyond(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        assertThat(findLand(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Revealing an Elemental lets it enter untapped")
    void entersUntappedWhenRevealing() {
        harness.setHand(player1, List.of(new PrimalBeyond(), new Smokebraider()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining to reveal makes it enter tapped even with an Elemental in hand")
    void entersTappedWhenDeclining() {
        harness.setHand(player1, List.of(new PrimalBeyond(), new Smokebraider()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findLand(player1).isTapped()).isTrue();
    }

    // ===== Colorless mana ability =====

    @Test
    @DisplayName("First ability taps for one colorless mana")
    void tappingProducesColorless() {
        addLandReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findLand(player1).isTapped()).isTrue();
    }

    // ===== Restricted any-color mana ability =====

    @Test
    @DisplayName("Second ability adds one mana of a chosen color, Elemental-restricted")
    void tappingProducesRestrictedAnyColorMana() {
        Permanent land = addLandReady(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.BLUE)).isEqualTo(0);
        assertThat(pool.getSubtypeSpellOrAbilityManaForColor(Set.of(CardSubtype.ELEMENTAL), ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Restricted mana can cast an Elemental spell but not a non-Elemental spell")
    void restrictedManaOnlyForElementals() {
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSubtypeSpellOrAbilityMana(CardSubtype.ELEMENTAL, ManaColor.RED, 1);

        Card goblin = createCreature("Test Goblin", "{R}", CardColor.RED, CardSubtype.GOBLIN);
        harness.setHand(player1, List.of(goblin));
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        Card elemental = createCreature("Test Elemental", "{R}", CardColor.RED, CardSubtype.ELEMENTAL);
        harness.setHand(player1, List.of(elemental));
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Test Elemental");
    }

    // ===== Helpers =====

    private Permanent addLandReady(Player player) {
        Permanent perm = new Permanent(new PrimalBeyond());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent findLand(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Primal Beyond"))
                .findFirst().orElseThrow();
    }
}
