package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.u.UnworthyDead;
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

@CardUsed({Catastrophe.class, Forest.class, Plains.class, UnworthyDead.class})
class CatastropheTest extends BaseCardTest {

    @Test
    @DisplayName("The land mode destroys all lands but leaves creatures intact")
    void destroysAllLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player1, new UnworthyDead());
        harness.addToBattlefield(player2, new UnworthyDead());

        castCatastrophe(0);

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Plains");
        harness.assertOnBattlefield(player1, "Unworthy Dead");
        harness.assertOnBattlefield(player2, "Unworthy Dead");
    }

    @Test
    @DisplayName("The creature mode destroys all creatures but leaves lands intact")
    void destroysAllCreatures() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player1, new UnworthyDead());
        harness.addToBattlefield(player2, new UnworthyDead());

        castCatastrophe(1);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Plains");
        harness.assertNotOnBattlefield(player1, "Unworthy Dead");
        harness.assertNotOnBattlefield(player2, "Unworthy Dead");
    }

    @Test
    @DisplayName("Creatures destroyed by the creature mode cannot be regenerated")
    void creaturesCannotBeRegenerated() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new UnworthyDead());
        creature.setRegenerationShield(1);

        castCatastrophe(1);

        harness.assertInGraveyard(player2, "Unworthy Dead");
    }

    @Test
    @DisplayName("Indestructible creatures survive the creature mode")
    void indestructibleCreaturesSurvive() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new UnworthyDead());
        creature.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        castCatastrophe(1);

        harness.assertOnBattlefield(player2, "Unworthy Dead");
    }

    @Test
    @DisplayName("An invalid destruction choice is rejected during resolution")
    void invalidChoiceIsRejected() {
        harness.setHand(player1, List.of(new Catastrophe()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.handleListChoice(player1, "Destroy all artifacts"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("The destruction choice is made during resolution, after players can respond")
    void choosesDuringResolution() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new UnworthyDead());
        harness.setHand(player1, List.of(new Catastrophe()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Unworthy Dead");
        harness.handleListChoice(player1, "Destroy all creatures");

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player2, "Unworthy Dead");
    }

    @Test
    @DisplayName("A noncreature land with a regeneration shield survives land destruction")
    void noncreatureLandCanRegenerate() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        land.setRegenerationShield(1);

        castCatastrophe(0);

        harness.assertOnBattlefield(player2, "Forest");
        assertThat(land.isTapped()).isTrue();
        assertThat(land.getRegenerationShield()).isZero();
    }

    private void castCatastrophe(int mode) {
        harness.setHand(player1, List.of(new Catastrophe()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleListChoice(player1, mode == 0 ? "Destroy all lands" : "Destroy all creatures");
    }
}
