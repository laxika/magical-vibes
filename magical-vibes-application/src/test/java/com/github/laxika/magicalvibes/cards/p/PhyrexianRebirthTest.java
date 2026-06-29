package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PhyrexianRebirthTest extends BaseCardTest {

    // ===== Basic functionality =====

    @Test
    @DisplayName("Phyrexian Rebirth destroys all creatures and creates a token with P/T equal to destroyed count")
    void destroysAllCreaturesAndCreatesToken() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SerraAngel());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new PhyrexianRebirth()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // All creatures destroyed
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Serra Angel");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        // Token created with P/T = 3 (three creatures destroyed)
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);

        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getName()).isEqualTo("Phyrexian Horror");
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(3);
    }

    // ===== Token properties =====

    @Test
    @DisplayName("Token is a colorless Phyrexian Horror artifact creature")
    void tokenHasCorrectProperties() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new PhyrexianRebirth()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .findFirst().orElseThrow();

        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(token.getCard().getSubtypes())
                .contains(CardSubtype.PHYREXIAN, CardSubtype.HORROR);
        assertThat(token.getCard().isToken()).isTrue();
    }

    // ===== No creatures =====

    @Test
    @DisplayName("With no creatures on battlefield, creates a 0/0 token")
    void noCreaturesCreatesZeroZeroToken() {
        harness.setHand(player1, List.of(new PhyrexianRebirth()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // 0/0 token is created but dies to SBAs — should not be on battlefield
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();
        assertThat(tokens).isEmpty();
    }

    // ===== Indestructible =====

    @Test
    @DisplayName("Indestructible creatures survive and are not counted for token P/T")
    void indestructibleCreaturesNotCounted() {
        GrizzlyBears indestructibleBears = new GrizzlyBears();
        indestructibleBears.setKeywords(Set.of(Keyword.INDESTRUCTIBLE));
        harness.addToBattlefield(player1, indestructibleBears);

        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new SerraAngel());

        harness.setHand(player1, List.of(new PhyrexianRebirth()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Indestructible creature survives
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        // Opponent creatures destroyed
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Serra Angel");

        // Token P/T = 2 (only 2 creatures were actually destroyed)
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
    }

    // ===== Destroyed creatures go to graveyard =====

    @Test
    @DisplayName("Destroyed creatures go to their owners' graveyards")
    void destroyedCreaturesGoToGraveyard() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new SerraAngel());

        harness.setHand(player1, List.of(new PhyrexianRebirth()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Serra Angel");
    }
}
