package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SaprolingMigrationTest extends BaseCardTest {

    @Test
    @DisplayName("Cast without kicker — creates two 1/1 green Saproling tokens")
    void castWithoutKickerCreatesTwoTokens() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new SaprolingMigration()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(2);

        List<Permanent> tokens = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Saproling"))
                .toList();
        assertThat(tokens).hasSize(2);

        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Cast without kicker — sorcery goes to graveyard after resolving")
    void castWithoutKickerGoesToGraveyard() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new SaprolingMigration()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Saproling Migration"));
    }

    @Test
    @DisplayName("Cast with kicker — creates four 1/1 green Saproling tokens")
    void castWithKickerCreatesFourTokens() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new SaprolingMigration()));
        // Base cost {1}{G} + kicker {4} = 6 mana total
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castKickedSorcery(player1, 0);
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(4);

        List<Permanent> tokens = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Saproling"))
                .toList();
        assertThat(tokens).hasSize(4);

        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Cast with kicker — sorcery goes to graveyard after resolving")
    void castWithKickerGoesToGraveyard() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new SaprolingMigration()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castKickedSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Saproling Migration"));
    }
}
