package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecureTheSceneTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a nonland permanent and gives its controller a Soldier token")
    void exilesNonlandPermanentAndCreatesSoldierForItsController() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castSecureTheScene(player2, "Grizzly Bears");

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertSoldierToken(player2);
    }

    @Test
    @DisplayName("Gives the caster a Soldier token when exiling their own permanent")
    void createsSoldierForCasterWhenExilingOwnPermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castSecureTheScene(player1, "Grizzly Bears");

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertSoldierToken(player1);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new SecureTheScene()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Forest")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    @DisplayName("Fizzles when the target leaves before resolution")
    void fizzlesWhenTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SecureTheScene()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        var targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(targetId));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Soldier"));
    }

    private void castSecureTheScene(com.github.laxika.magicalvibes.model.Player targetController,
                                    String targetName) {
        harness.setHand(player1, List.of(new SecureTheScene()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castInstant(player1, 0, harness.getPermanentId(targetController, targetName));
        harness.passBothPriorities();
    }

    private void assertSoldierToken(com.github.laxika.magicalvibes.model.Player player) {
        assertThat(gd.playerBattlefields.get(player.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Soldier")
                        && permanent.getCard().getColor() == CardColor.WHITE
                        && permanent.getCard().hasType(CardType.CREATURE)
                        && permanent.getCard().getPower() == 1
                        && permanent.getCard().getToughness() == 1
                        && permanent.getCard().getSubtypes().contains(CardSubtype.SOLDIER));
    }
}
