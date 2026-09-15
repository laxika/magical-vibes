package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Afterlife.class, FreshVolunteers.class, Forest.class})
class AfterlifeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target creature and gives its controller a 1/1 white flying Spirit")
    void destroysCreatureAndCreatesTokenForController() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers()).getId();

        castAfterlife(targetId);

        harness.assertNotOnBattlefield(player2, "Fresh Volunteers");
        harness.assertInGraveyard(player2, "Fresh Volunteers");
        assertSpiritToken(player2);
    }

    @Test
    @DisplayName("Target creature can't be regenerated")
    void targetCannotBeRegenerated() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers()).getId();
        Permanent target = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(targetId)).findFirst().orElseThrow();
        target.setRegenerationShield(1);

        castAfterlife(targetId);

        harness.assertNotOnBattlefield(player2, "Fresh Volunteers");
        harness.assertInGraveyard(player2, "Fresh Volunteers");
        assertSpiritToken(player2);
    }

    @Test
    @DisplayName("Can target own creature — its controller gets the Spirit token")
    void canTargetOwnCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers()).getId();

        castAfterlife(targetId);

        harness.assertNotOnBattlefield(player1, "Fresh Volunteers");
        assertSpiritToken(player1);
    }

    @Test
    @DisplayName("Creates the Spirit token even when the target is indestructible")
    void createsTokenWhenTargetIsIndestructible() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        target.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        castAfterlife(target.getId());

        harness.assertOnBattlefield(player2, "Fresh Volunteers");
        assertSpiritToken(player2);
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
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers()).getId();

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

        harness.castAndResolveInstant(player1, 0, targetId);
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
