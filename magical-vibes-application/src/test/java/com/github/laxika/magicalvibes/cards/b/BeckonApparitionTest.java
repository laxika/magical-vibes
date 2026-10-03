package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BeckonApparition.class, GrizzlyBears.class, Shock.class})
class BeckonApparitionTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles target card and creates a 1/1 white and black Spirit with flying")
    void exilesAndCreatesToken() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));
        harness.setHand(player1, List.of(new BeckonApparition()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Spirit")
                        && p.getCard().getSubtypes().contains(CardSubtype.SPIRIT)
                        && p.getCard().getKeywords().contains(Keyword.FLYING)
                        && p.getCard().getColors().contains(CardColor.WHITE)
                        && p.getCard().getColors().contains(CardColor.BLACK)
                        && p.getCard().getPower() == 1
                        && p.getCard().getToughness() == 1);
    }

    @Test
    @DisplayName("Can exile any card type, not just creatures")
    void exilesNonCreatureCard() {
        Card shock = new Shock();
        harness.setGraveyard(player2, new ArrayList<>(List.of(shock)));
        harness.setHand(player1, List.of(new BeckonApparition()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, shock.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
    }

    @Test
    @DisplayName("Fizzles with no token if the target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));
        harness.setHand(player1, List.of(new BeckonApparition()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, bears.getId());
        gd.playerGraveyards.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertNotOnBattlefield(player1, "Spirit");
    }

    @Test
    @DisplayName("Can exile a card from your own graveyard using black mana")
    void exilesFromOwnGraveyard() {
        Card target = new BeckonApparition();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new BeckonApparition()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(token -> assertThat(token.getCard().getName()).isEqualTo("Spirit"));
        harness.assertNotOnBattlefield(player2, "Spirit");
    }

    @Test
    @DisplayName("Cannot cast without a graveyard target")
    void requiresTarget() {
        harness.setHand(player1, List.of(new BeckonApparition()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertNotOnBattlefield(player1, "Spirit");
    }

    @Test
    @DisplayName("A responding Beckon Apparition exiles the shared target and only creates one token")
    void respondingSpellMakesOriginalTargetIllegal() {
        Card target = new BeckonApparition();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new BeckonApparition()));
        harness.setHand(player2, List.of(new BeckonApparition()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(target);
        harness.assertNotOnBattlefield(player1, "Spirit");
        assertThat(gd.playerBattlefields.get(player2.getId())).singleElement()
                .satisfies(token -> assertThat(token.getCard().getName()).isEqualTo("Spirit"));
    }
}
