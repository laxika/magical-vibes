package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HedgeTroll;
import com.github.laxika.magicalvibes.cards.p.PoulticeSliver;
import com.github.laxika.magicalvibes.cards.u.UrborgTombOfYawgmoth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Saltblast.class, HedgeTroll.class, PoulticeSliver.class, UrborgTombOfYawgmoth.class})
class SaltblastTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target nonwhite permanent")
    void destroysTargetNonwhitePermanent() {
        harness.addToBattlefield(player2, new HedgeTroll());
        UUID targetId = harness.getPermanentId(player2, "Hedge Troll");
        harness.setHand(player1, List.of(new Saltblast()));
        addSaltblastMana();

        harness.castAndResolveSorcery(player1, 0, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Hedge Troll");
        harness.assertInGraveyard(player2, "Hedge Troll");
    }

    @Test
    @DisplayName("Destroys a nonwhite noncreature permanent")
    void destroysTargetNonwhiteNoncreaturePermanent() {
        harness.addToBattlefield(player2, new UrborgTombOfYawgmoth());
        UUID targetId = harness.getPermanentId(player2, "Urborg, Tomb of Yawgmoth");
        harness.setHand(player1, List.of(new Saltblast()));
        addSaltblastMana();

        harness.castAndResolveSorcery(player1, 0, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Urborg, Tomb of Yawgmoth");
        harness.assertInGraveyard(player2, "Urborg, Tomb of Yawgmoth");
    }

    @Test
    @DisplayName("Cannot target a white permanent")
    void cannotTargetWhitePermanent() {
        harness.addToBattlefield(player2, new PoulticeSliver());
        UUID targetId = harness.getPermanentId(player2, "Poultice Sliver");
        harness.setHand(player1, List.of(new Saltblast()));
        addSaltblastMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonwhite permanent");
    }

    private void addSaltblastMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);
    }
}
