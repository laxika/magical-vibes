package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MishrasBauble;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TegwyllsScouring.class, GrizzlyBears.class, MishrasBauble.class, SuntailHawk.class})
class TegwyllsScouringTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all creatures, creates three flying Faerie Rogue tokens, and leaves noncreatures alone")
    void destroysCreaturesAndCreatesFaeries() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new MishrasBauble());
        harness.setHand(player1, List.of(new TegwyllsScouring()));
        addMana();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Mishra's Bauble");
        assertThat(faerieTokens()).hasSize(3);
    }

    @Test
    @DisplayName("Can be cast on an opponent's turn by tapping three flying creatures")
    void castsWithFlashByTappingFlyers() {
        List<UUID> flyerIds = List.of(addFlyer().getId(), addFlyer().getId(), addFlyer().getId());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new TegwyllsScouring()));
        addMana();

        harness.castWithAlternateCost(player1, 0, flyerIds);

        assertThat(gd.playerBattlefields.get(player1).stream()
                .filter(permanent -> flyerIds.contains(permanent.getId())))
                .allMatch(Permanent::isTapped);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(faerieTokens()).hasSize(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot use the flash casting option without three flying creatures")
    void alternateCostRequiresThreeFlyers() {
        Permanent flyer = addFlyer();
        Permanent groundCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new TegwyllsScouring()));
        addMana();

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0,
                List.of(flyer.getId(), groundCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addFlyer() {
        return harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private List<Permanent> faerieTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
