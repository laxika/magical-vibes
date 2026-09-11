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
    @DisplayName("Choosing an invalid mode is rejected while Catastrophe is cast")
    void invalidModeIsRejected() {
        harness.setHand(player1, List.of(new Catastrophe()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 99))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid mode index");
    }

    private void castCatastrophe(int mode) {
        harness.setHand(player1, List.of(new Catastrophe()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castSorcery(player1, 0, mode);
        harness.passBothPriorities();
    }
}
