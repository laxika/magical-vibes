package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GaeasAnthem;
import com.github.laxika.magicalvibes.cards.s.SerraSphinx;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Pongify.class, SerraSphinx.class, GaeasAnthem.class})
class PongifyTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target creature and creates an Ape token for its controller")
    void destroysCreatureAndCreatesTokenForController() {
        harness.addToBattlefield(player2, new SerraSphinx());
        harness.setHand(player1, List.of(new Pongify()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Serra Sphinx"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Serra Sphinx");
        harness.assertInGraveyard(player2, "Serra Sphinx");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Ape")
                        && permanent.getCard().hasType(CardType.CREATURE)
                        && permanent.getCard().getColor() == CardColor.GREEN
                        && permanent.getCard().getPower() == 3
                        && permanent.getCard().getToughness() == 3
                        && permanent.getCard().getSubtypes().contains(CardSubtype.APE));
    }

    @Test
    @DisplayName("Destroys the target despite a regeneration shield")
    void cannotBeRegenerated() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player2, new SerraSphinx());
        sphinx.setRegenerationShield(1);
        harness.setHand(player1, List.of(new Pongify()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, sphinx.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Serra Sphinx");
        harness.assertInGraveyard(player2, "Serra Sphinx");
    }

    @Test
    @DisplayName("Creates an Ape even when the target is indestructible")
    void createsTokenWhenTargetIsIndestructible() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player2, new SerraSphinx());
        sphinx.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.setHand(player1, List.of(new Pongify()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, sphinx.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Serra Sphinx");
        harness.assertNotInGraveyard(player2, "Serra Sphinx");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Ape"));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new SerraSphinx());
        harness.addToBattlefield(player2, new GaeasAnthem());
        harness.setHand(player1, List.of(new Pongify()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Gaea's Anthem")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }
}
