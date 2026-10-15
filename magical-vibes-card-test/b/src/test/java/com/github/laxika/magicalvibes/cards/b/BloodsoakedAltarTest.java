package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodsoakedAltar.class, GreenwoodSentinel.class, Mountain.class})
class BloodsoakedAltarTest extends BaseCardTest {

    @Test
    @DisplayName("Pays all costs and creates a 5/5 flying black Demon token")
    void paysCostsAndCreatesDemonToken() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent altar = harness.addToBattlefieldAndReturn(player1, new BloodsoakedAltar());
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent demon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .findFirst().orElseThrow();

        assertThat(altar.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(demon.isTapped()).isFalse();
        assertThat(demon.getEffectivePower()).isEqualTo(5);
        assertThat(demon.getEffectiveToughness()).isEqualTo(5);
        assertThat(demon.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(demon.getCard().getSubtypes()).contains(CardSubtype.DEMON);
        assertThat(demon.getCard().getKeywords()).contains(Keyword.FLYING);
        harness.assertInGraveyard(player1, "Greenwood Sentinel");
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("Cannot activate without a creature to sacrifice")
    void requiresCreatureToSacrifice() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new BloodsoakedAltar());
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Can activate only at sorcery speed")
    void onlyAtSorcerySpeed() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new BloodsoakedAltar());
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new Mountain()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void paysCostsBeforeTokenResolves() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent altar = harness.addToBattlefieldAndReturn(player1, new BloodsoakedAltar());
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(altar.isTapped()).isTrue();
        harness.assertLife(player1, 18);
        harness.assertInGraveyard(player1, "Mountain");
        harness.assertInGraveyard(player1, "Greenwood Sentinel");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(altar);

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    void cannotActivateWithEmptyHand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent altar = harness.addToBattlefieldAndReturn(player1, new BloodsoakedAltar());
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(altar.isTapped()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Greenwood Sentinel");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithInsufficientLife() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent altar = harness.addToBattlefieldAndReturn(player1, new BloodsoakedAltar());
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLife(player1, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(altar.isTapped()).isFalse();
        harness.assertLife(player1, 1);
        harness.assertInHand(player1, "Mountain");
        harness.assertOnBattlefield(player1, "Greenwood Sentinel");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateDuringCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new BloodsoakedAltar());
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new Mountain()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithAbilityOnStack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new BloodsoakedAltar());
        Permanent secondAltar = harness.addToBattlefieldAndReturn(player1, new BloodsoakedAltar());
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new Mountain(), new Mountain()));
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Greenwood Sentinel"));
        assertThat(gd.stack).hasSize(1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(secondAltar.isTapped()).isFalse();
        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void cannotActivateTappedAltar() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent altar = harness.addToBattlefieldAndReturn(player1, new BloodsoakedAltar());
        altar.tap();
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 20);
        harness.assertInHand(player1, "Mountain");
        harness.assertOnBattlefield(player1, "Greenwood Sentinel");
        assertThat(gd.stack).isEmpty();
    }
}
