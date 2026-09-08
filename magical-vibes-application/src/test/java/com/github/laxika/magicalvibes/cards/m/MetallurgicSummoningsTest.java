package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MetallurgicSummoningsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant or sorcery creates a Construct token with that spell's mana value")
    void instantOrSorceryCreatesConstructWithSpellManaValue() {
        harness.addToBattlefield(player1, new MetallurgicSummonings());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent construct = findPermanent(player1, "Construct");
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(3);
        assertThat(construct.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(construct.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Exiling the enchantment returns all instants and sorceries from the graveyard")
    void activationReturnsAllInstantsAndSorceries() {
        harness.addToBattlefield(player1, new MetallurgicSummonings());
        addArtifacts(player1, 6);

        Card shock = new Shock();
        Card divination = new Divination();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(shock, divination, creature)));
        harness.addMana(player1, ManaColor.BLUE, 5);

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(
                findPermanent(player1, "Metallurgic Summonings"));
        harness.activateAbility(player1, sourceIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .contains(shock.getId(), divination.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .containsExactly(creature.getId());
        assertThat(countPermanents(player1, "Metallurgic Summonings")).isZero();
    }

    @Test
    @DisplayName("The recursion ability cannot be activated without six artifacts")
    void activationRequiresSixArtifacts() {
        harness.addToBattlefield(player1, new MetallurgicSummonings());
        addArtifacts(player1, 5);
        harness.addMana(player1, ManaColor.BLUE, 5);

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(
                findPermanent(player1, "Metallurgic Summonings"));
        assertThatThrownBy(() -> harness.activateAbility(player1, sourceIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addArtifacts(com.github.laxika.magicalvibes.model.Player player, int count) {
        for (int i = 0; i < count; i++) {
            Card artifact = new Card();
            artifact.setName("Artifact " + i);
            artifact.setType(CardType.ARTIFACT);
            harness.addToBattlefield(player, artifact);
        }
    }
}
