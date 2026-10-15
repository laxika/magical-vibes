package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VituGhaziGuildmage.class})
class VituGhaziGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("First ability creates a 3/3 green Centaur token")
    void firstAbilityCreatesCentaurToken() {
        addReadyGuildmage();
        addMana(4, 1, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        List<Permanent> tokens = creatureTokensNamed(player1, "Centaur");
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getCard().getPower()).isEqualTo(3);
        assertThat(tokens.getFirst().getCard().getToughness()).isEqualTo(3);
        assertThat(tokens.getFirst().getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(tokens.getFirst().getCard().getSubtypes()).contains(CardSubtype.CENTAUR);
    }

    @Test
    @DisplayName("Second ability populates a creature token you control")
    void secondAbilityPopulates() {
        addReadyGuildmage();
        addCreatureToken(player1, "Soldier Token");
        addMana(2, 1, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(creatureTokensNamed(player1, "Soldier Token")).hasSize(2);
    }

    @Test
    @DisplayName("Second ability creates nothing without a creature token")
    void secondAbilityWithoutTokenCreatesNothing() {
        addReadyGuildmage();
        addMana(2, 1, 1);

        int battlefieldSize = gd.playerBattlefields.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldSize);
    }

    @Test
    @DisplayName("Populate copies the Centaur created by the first ability")
    void populatesItsOwnCentaur() {
        addReadyGuildmage();
        addMana(6, 2, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        Permanent original = creatureTokensNamed(player1, "Centaur").getFirst();
        original.tap();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        List<Permanent> tokens = creatureTokensNamed(player1, "Centaur");
        assertThat(tokens).hasSize(2);
        Permanent copy = tokens.stream()
                .filter(p -> !p.getId().equals(original.getId()))
                .findFirst().orElseThrow();
        assertThat(copy.isTapped()).isFalse();
        assertThat(copy.getCard().getPower()).isEqualTo(3);
        assertThat(copy.getCard().getToughness()).isEqualTo(3);
        assertThat(copy.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(copy.getCard().getSubtypes()).contains(CardSubtype.CENTAUR);
    }

    @Test
    @DisplayName("Populate does not copy an opponent's creature token")
    void doesNotPopulateOpposingToken() {
        addReadyGuildmage();
        addCreatureToken(player2, "Soldier Token");
        addMana(2, 1, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(creatureTokensNamed(player2, "Soldier Token")).hasSize(1);
    }

    @Test
    @DisplayName("Both abilities can be used while the Guildmage is tapped and summoning sick")
    void abilitiesDoNotRequireTappingOrHaste() {
        Permanent guildmage = harness.addToBattlefieldAndReturn(player1, new VituGhaziGuildmage());
        guildmage.setSummoningSick(true);
        guildmage.tap();
        addMana(6, 2, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(creatureTokensNamed(player1, "Centaur")).hasSize(2);
        assertThat(guildmage.isTapped()).isTrue();
    }
    private void addReadyGuildmage() {
        addCreatureReady(player1, new VituGhaziGuildmage());
    }

    private void addMana(int colorless, int green, int white) {
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
        harness.addMana(player1, ManaColor.GREEN, green);
        harness.addMana(player1, ManaColor.WHITE, white);
    }

    private List<Permanent> creatureTokensNamed(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals(name))
                .toList();
    }

    private void addCreatureToken(Player player, String name) {
        Card card = new Card();
        card.setToken(true);
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(CardSubtype.SOLDIER));
        addCreatureReady(player, card);
    }
}
