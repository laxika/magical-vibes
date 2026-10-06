package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HedgeTroll;
import com.github.laxika.magicalvibes.cards.p.PoulticeSliver;
import com.github.laxika.magicalvibes.cards.t.TenebTheHarvester;
import com.github.laxika.magicalvibes.cards.u.UrborgTombOfYawgmoth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Saltblast.class, HedgeTroll.class, PoulticeSliver.class, UrborgTombOfYawgmoth.class,
        TenebTheHarvester.class})
class SaltblastTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target nonwhite permanent")
    void destroysTargetNonwhitePermanent() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new HedgeTroll()).getId();
        harness.setHand(player1, List.of(new Saltblast()));
        addSaltblastMana();

        harness.castAndResolveSorcery(player1, 0, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Hedge Troll");
        harness.assertInGraveyard(player2, "Hedge Troll");
    }

    @Test
    @DisplayName("Destroys a nonwhite noncreature permanent")
    void destroysTargetNonwhiteNoncreaturePermanent() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new UrborgTombOfYawgmoth()).getId();
        harness.setHand(player1, List.of(new Saltblast()));
        addSaltblastMana();

        harness.castAndResolveSorcery(player1, 0, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Urborg, Tomb of Yawgmoth");
        harness.assertInGraveyard(player2, "Urborg, Tomb of Yawgmoth");
    }

    @Test
    @DisplayName("Cannot target a white permanent")
    void cannotTargetWhitePermanent() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new PoulticeSliver()).getId();
        harness.setHand(player1, List.of(new Saltblast()));
        addSaltblastMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonwhite permanent");
    }

    @Test
    @DisplayName("Cannot target a multicolor permanent that is white")
    void cannotTargetMulticolorWhitePermanent() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new TenebTheHarvester()).getId();
        harness.setHand(player1, List.of(new Saltblast()));
        addSaltblastMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonwhite permanent");
    }

    @Test
    @DisplayName("Can destroy your own nonwhite permanent")
    void destroysOwnNonwhitePermanent() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new HedgeTroll()).getId();
        harness.setHand(player1, List.of(new Saltblast()));
        addSaltblastMana();

        harness.castAndResolveSorcery(player1, 0, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Hedge Troll");
        harness.assertInGraveyard(player1, "Hedge Troll");
    }

    @Test
    @DisplayName("A nonwhite creature can regenerate from Saltblast")
    void regenerationPreventsDestruction() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new HedgeTroll()).getId();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Saltblast()));
        addSaltblastMana();

        harness.castAndResolveSorcery(player1, 0, 0, targetId);

        harness.assertOnBattlefield(player1, "Hedge Troll");
        harness.assertNotInGraveyard(player1, "Hedge Troll");
        harness.assertInGraveyard(player1, "Saltblast");
    }

    private void addSaltblastMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);
    }
}
