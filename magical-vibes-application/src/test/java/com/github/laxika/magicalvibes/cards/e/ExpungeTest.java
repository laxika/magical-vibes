package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpungeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a nonartifact, nonblack creature and prevents regeneration")
    void destroysValidCreatureWithoutRegeneration() {
        Permanent creature = addCreature(player2, "Target Creature", CardColor.GREEN, false);
        creature.setRegenerationShield(1);

        castExpunge(creature);

        harness.assertNotOnBattlefield(player2, "Target Creature");
        harness.assertInGraveyard(player2, "Target Creature");
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        Permanent creature = addCreature(player2, "Black Creature", CardColor.BLACK, false);

        assertThatThrownBy(() -> castExpunge(creature))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an artifact creature")
    void cannotTargetArtifactCreature() {
        Permanent creature = addCreature(player2, "Artifact Creature", CardColor.GREEN, true);

        assertThatThrownBy(() -> castExpunge(creature))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling discards Expunge and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new Expunge()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Expunge");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private void castExpunge(Permanent target) {
        harness.setHand(player1, List.of(new Expunge()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addCreature(Player player, String name, CardColor color, boolean artifact) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        if (artifact) {
            card.setAdditionalTypes(Set.of(CardType.ARTIFACT));
        }
        card.setColor(color);
        card.setPower(2);
        card.setToughness(2);
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
