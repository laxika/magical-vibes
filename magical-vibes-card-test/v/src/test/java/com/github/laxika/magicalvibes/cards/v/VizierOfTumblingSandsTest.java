package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AncientCrab;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WindsOfRebuke;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VizierOfTumblingSands.class, AncientCrab.class, Forest.class, WindsOfRebuke.class})
class VizierOfTumblingSandsTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps a tapped target permanent")
    void untapsTargetPermanent() {
        addReadyVizier(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AncientCrab());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Activating the untap ability taps Vizier as its cost")
    void activatingTapsVizier() {
        Permanent vizier = addReadyVizier(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(vizier.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot untap itself — target must be another permanent")
    void cannotTargetItself() {
        Permanent vizier = addReadyVizier(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, vizier.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another permanent");
    }

    @Test
    @DisplayName("Cycling untaps the target permanent and draws a card")
    void cyclingUntapsTargetAndDraws() {
        harness.setHand(player1, List.of(new VizierOfTumblingSands()));
        harness.setLibrary(player1, List.of(new AncientCrab()));
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        target.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Vizier of Tumbling Sands");

        harness.passBothPriorities();

        harness.assertInHand(player1, "Ancient Crab");
    }

    @Test
    @DisplayName("Cycling draws even when no permanent exists to target")
    void cyclesWithoutAnyLegalUntapTarget() {
        harness.setHand(player1, List.of(new VizierOfTumblingSands()));
        harness.setLibrary(player1, List.of(new AncientCrab()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.assertInGraveyard(player1, "Vizier of Tumbling Sands");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Ancient Crab");
    }

    @Test
    @DisplayName("Cycling still draws when the untap target leaves the battlefield")
    void drawsWhenUntapTargetBecomesIllegal() {
        harness.setHand(player1, List.of(new VizierOfTumblingSands()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new AncientCrab()));
        harness.setHand(player2, List.of(new WindsOfRebuke()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VizierOfTumblingSands());
        target.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Vizier of Tumbling Sands");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Ancient Crab");
        harness.assertInGraveyard(player1, "Vizier of Tumbling Sands");
    }

    @Test
    @DisplayName("A summoning-sick Vizier cannot activate its tap ability")
    void cannotActivateTapAbilityWhileSummoningSick() {
        Permanent vizier = harness.addToBattlefieldAndReturn(player1, new VizierOfTumblingSands());
        vizier.setSummoningSick(true);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        target.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(vizier.isTapped()).isFalse();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The tap ability can untap a different Vizier")
    void canTargetAnotherVizier() {
        Permanent source = addReadyVizier(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new VizierOfTumblingSands());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cycling requires blue mana and does not discard when its cost cannot be paid")
    void cyclingRequiresBlueMana() {
        harness.setHand(player1, List.of(new VizierOfTumblingSands()));
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Vizier of Tumbling Sands");
        harness.assertNotInGraveyard(player1, "Vizier of Tumbling Sands");
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyVizier(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new VizierOfTumblingSands());
        perm.setSummoningSick(false);
        return perm;
    }
}
