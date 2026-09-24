package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.m.MyrMatrix;
import com.github.laxika.magicalvibes.cards.m.MyrMoonvessel;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.laxika.magicalvibes.model.ManaColor.COLORLESS;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GenesisChamber.class, MyrMoonvessel.class, DarksteelCitadel.class, MyrMatrix.class})
class GenesisChamberTest extends BaseCardTest {

    @Test
    @DisplayName("A nontoken creature entering gives its controller a Myr token")
    void nontokenCreatureEnteringGivesItsControllerMyrToken() {
        addChamber(player1);
        harness.setHand(player1, List.of(new MyrMoonvessel()));
        harness.addMana(player1, COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(myrTokens(player1)).hasSize(1);
        Permanent token = myrTokens(player1).getFirst();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.MYR);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(myrTokens(player2)).isEmpty();
    }

    @Test
    @DisplayName("A nontoken creature entering under an opponent's control gives that player a Myr token")
    void opponentCreatureEnteringGivesOpponentMyrToken() {
        addChamber(player1);
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new MyrMoonvessel()));
        harness.addMana(player2, COLORLESS, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(myrTokens(player1)).isEmpty();
        assertThat(myrTokens(player2)).hasSize(1);
    }

    @Test
    @DisplayName("A tapped Genesis Chamber does not trigger")
    void tappedChamberDoesNotTrigger() {
        addChamber(player1).tap();
        harness.setHand(player1, List.of(new MyrMoonvessel()));
        harness.addMana(player1, COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(myrTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("A noncreature permanent entering does not trigger Genesis Chamber")
    void noncreaturePermanentEnteringDoesNotTrigger() {
        addChamber(player1);
        harness.setHand(player1, List.of(new DarksteelCitadel()));

        harness.playLand(player1, 0);

        assertThat(myrTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("A token creature entering does not trigger Genesis Chamber")
    void tokenCreatureEnteringDoesNotTrigger() {
        addChamber(player1);
        harness.addToBattlefield(player1, new MyrMatrix());
        harness.addMana(player1, COLORLESS, 5);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(myrTokens(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Tapping Genesis Chamber after the trigger is created stops token creation")
    void tappingChamberAfterTriggerIsCreatedStopsTokenCreation() {
        Permanent chamber = addChamber(player1);
        harness.setHand(player1, List.of(new MyrMoonvessel()));
        harness.addMana(player1, COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        chamber.tap();
        harness.passBothPriorities();

        assertThat(myrTokens(player1)).isEmpty();
    }

    private Permanent addChamber(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GenesisChamber());
    }

    private List<Permanent> myrTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Myr"))
                .toList();
    }
}
