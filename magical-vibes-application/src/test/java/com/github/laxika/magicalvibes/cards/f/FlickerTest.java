package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlickerTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles and immediately returns any nontoken permanent under its owner's control")
    void flickersTargetPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Flicker()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castSorcery(player1, 0, bearsId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(harness.getPermanentId(player2, "Grizzly Bears")).isNotEqualTo(bearsId);
        Permanent returned = findPermanent(player2, "Grizzly Bears");
        assertThat(returned.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a token permanent")
    void cannotTargetTokenPermanent() {
        Permanent token = harness.addToBattlefieldAndReturn(player1, token("Soldier Token"));
        harness.setHand(player1, List.of(new Flicker()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, token.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if the target leaves the battlefield before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Flicker()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castSorcery(player1, 0, bearsId);

        Permanent bears = gqs.findPermanentById(gd, bearsId);
        gd.playerBattlefields.get(player1.getId()).remove(bears);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    private static Card token(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
