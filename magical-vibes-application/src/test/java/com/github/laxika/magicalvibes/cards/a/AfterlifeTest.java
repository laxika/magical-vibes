package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AfterlifeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target creature and gives its controller a 1/1 white flying Spirit")
    void destroysCreatureAndCreatesTokenForController() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        castAfterlife(targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertSpiritToken(player2);
    }

    @Test
    @DisplayName("Target creature can't be regenerated")
    void targetCannotBeRegenerated() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(targetId)).findFirst().orElseThrow();
        bears.setRegenerationShield(1);

        castAfterlife(targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can target own creature — its controller gets the Spirit token")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        castAfterlife(targetId);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertSpiritToken(player1);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        UUID targetId = harness.getPermanentId(player2, "Forest");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Afterlife()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles when target leaves the battlefield before resolution — no token created")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Afterlife()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castInstant(player1, 0, targetId);

        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(targetId));
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertNotOnBattlefield(player2, "Spirit");
    }

    private void castAfterlife(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Afterlife()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void assertSpiritToken(com.github.laxika.magicalvibes.model.Player owner) {
        assertThat(gd.playerBattlefields.get(owner.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Spirit")
                        && p.getCard().isToken()
                        && p.getCard().hasType(CardType.CREATURE)
                        && p.getCard().getColor() == CardColor.WHITE
                        && p.getCard().getPower() == 1
                        && p.getCard().getToughness() == 1
                        && p.getCard().getSubtypes().contains(CardSubtype.SPIRIT)
                        && p.getCard().getKeywords().contains(Keyword.FLYING));
    }
}
