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

import static org.assertj.core.api.Assertions.assertThat;
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
        harness.castFromHand(player1, new SpiderFood(), "{2}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    void foodCanBeActivatedImmediatelyAndIsSacrificedAsACost() {
        harness.setLife(player1, 10);
        harness.castFromHand(player1, new SpiderFood(), "{2}{G}");
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Food");
        harness.assertLife(player1, 10);
        harness.passBothPriorities();
        harness.assertLife(player1, 13);
        harness.assertLife(player2, 20);
    }

    @Test
    void foodCannotBeActivatedWithoutTwoMana() {
        harness.castFromHand(player1, new SpiderFood(), "{2}{G}");
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Food");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedFoodCannotBeActivated() {
        harness.castFromHand(player1, new SpiderFood(), "{2}{G}");
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).getFirst().tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");

        harness.assertOnBattlefield(player1, "Food");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void createsNoFoodWhenChosenTargetIsSacrificedBeforeResolution() {
        harness.castFromHand(player1, new SpiderFood(), "{2}{G}");
        harness.passBothPriorities();
        Permanent target = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new SpiderFood()));
        addMana();
        harness.castSorcery(player1, 0, 0, target.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertLife(player1, 23);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Food");
        harness.assertInGraveyard(player1, "Spider Food");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canChooseNoTargetEvenWhenALegalTargetExists() {
        harness.addToBattlefield(player2, new FountainOfYouth());

        harness.castFromHand(player1, new SpiderFood(), "{2}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Fountain of Youth");
        harness.assertOnBattlefield(player1, "Food");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Food");
    }

    private void castSpiderFood(Permanent target) {
        harness.setHand(player1, List.of(new SpiderFood()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
