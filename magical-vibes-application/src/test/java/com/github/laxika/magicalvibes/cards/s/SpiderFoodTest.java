package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpiderFood.class, FountainOfYouth.class, AngelicChorus.class, AirElemental.class, GrizzlyBears.class})
class SpiderFoodTest extends BaseCardTest {

    @Test
    void destroysArtifactAndCreatesFood() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        castSpiderFood(target);

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Fountain of Youth");
        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    void destroysEnchantmentAndCreatesFood() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelicChorus());

        castSpiderFood(target);

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    void destroysCreatureWithFlyingAndCreatesFood() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castSpiderFood(target);

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    void cannotTargetCreatureWithoutFlying() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpiderFood()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact, enchantment, or creature with flying");
    }

    @Test
    void mayBeCastWithoutTargetToCreateFood() {
        harness.setHand(player1, List.of(new SpiderFood()));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Food");
    }

    private void castSpiderFood(Permanent target) {
        harness.setHand(player1, List.of(new SpiderFood()));
        addMana();
        harness.castSorcery(player1, 0, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
