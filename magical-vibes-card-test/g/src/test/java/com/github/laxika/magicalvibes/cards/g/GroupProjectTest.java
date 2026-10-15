package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.t.ThaliaGuardianOfThraben;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GroupProject.class, GeometersArthropod.class})
class GroupProjectTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving creates a 2/2 Spirit token")
    void createsSpiritToken() {
        harness.setHand(player1, List.of(new GroupProject()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Spirit")
                        && p.getEffectivePower() == 2
                        && p.getEffectiveToughness() == 2);
    }

    @Test
    @DisplayName("Flashback taps three creatures you control and creates another Spirit")
    void flashbackTapsThreeCreaturesAndCreatesToken() {
        Permanent c1 = addCreatureReady(player1, new GeometersArthropod());
        Permanent c2 = addCreatureReady(player1, new GeometersArthropod());
        Permanent c3 = addCreatureReady(player1, new GeometersArthropod());
        harness.setGraveyard(player1, List.of(new GroupProject()));

        harness.castFlashbackWithTapCost(player1, 0, List.of(c1.getId(), c2.getId(), c3.getId()));
        harness.passBothPriorities();

        assertThat(c1.isTapped()).isTrue();
        assertThat(c2.isTapped()).isTrue();
        assertThat(c3.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Spirit");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Group Project"));
    }

    @Test
    void tokenHasBothColorsAndNoKeywords() {
        harness.setHand(player1, List.of(new GroupProject()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent token = findPermanent(player1, "Spirit");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.RED, CardColor.WHITE);
        assertThat(token.getCard().getKeywords()).isEmpty();
        assertThat(token.isTapped()).isFalse();
    }

    @Test
    void flashbackCanTapSummoningSickCreaturesWithoutMana() {
        Permanent c1 = harness.addToBattlefieldAndReturn(player1, new GeometersArthropod());
        Permanent c2 = harness.addToBattlefieldAndReturn(player1, new GeometersArthropod());
        Permanent c3 = harness.addToBattlefieldAndReturn(player1, new GeometersArthropod());
        harness.setGraveyard(player1, List.of(new GroupProject()));

        harness.castFlashbackWithTapCost(player1, 0, List.of(c1.getId(), c2.getId(), c3.getId()));

        assertThat(List.of(c1, c2, c3)).allMatch(Permanent::isTapped);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
    }

    @Test
    void flashbackRejectsSelectingSameCreatureTwice() {
        Permanent c1 = addCreatureReady(player1, new GeometersArthropod());
        Permanent c2 = addCreatureReady(player1, new GeometersArthropod());
        addCreatureReady(player1, new GeometersArthropod());
        harness.setGraveyard(player1, List.of(new GroupProject()));

        assertThatThrownBy(() -> harness.castFlashbackWithTapCost(player1, 0,
                List.of(c1.getId(), c2.getId(), c1.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(c1.isTapped()).isFalse();
        assertThat(c2.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void flashbackRejectsAlreadyTappedCreature() {
        Permanent c1 = addCreatureReady(player1, new GeometersArthropod());
        Permanent c2 = addCreatureReady(player1, new GeometersArthropod());
        Permanent c3 = addCreatureReady(player1, new GeometersArthropod());
        addCreatureReady(player1, new GeometersArthropod());
        c3.tap();
        harness.setGraveyard(player1, List.of(new GroupProject()));

        assertThatThrownBy(() -> harness.castFlashbackWithTapCost(player1, 0,
                List.of(c1.getId(), c2.getId(), c3.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(c1.isTapped()).isFalse();
        assertThat(c2.isTapped()).isFalse();
    }

    @Test
    void flashbackRejectsOpponentsCreature() {
        Permanent c1 = addCreatureReady(player1, new GeometersArthropod());
        Permanent c2 = addCreatureReady(player1, new GeometersArthropod());
        addCreatureReady(player1, new GeometersArthropod());
        Permanent opponentCreature = addCreatureReady(player2, new GeometersArthropod());
        harness.setGraveyard(player1, List.of(new GroupProject()));

        assertThatThrownBy(() -> harness.castFlashbackWithTapCost(player1, 0,
                List.of(c1.getId(), c2.getId(), opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(c1.isTapped()).isFalse();
        assertThat(c2.isTapped()).isFalse();
        assertThat(opponentCreature.isTapped()).isFalse();
    }

    @Test
    @CardUsed({ThaliaGuardianOfThraben.class})
    void flashbackRequiresManaForCostIncrease() {
        Permanent c1 = addCreatureReady(player1, new GeometersArthropod());
        Permanent c2 = addCreatureReady(player1, new GeometersArthropod());
        Permanent c3 = addCreatureReady(player1, new GeometersArthropod());
        harness.addToBattlefield(player2, new ThaliaGuardianOfThraben());
        harness.setGraveyard(player1, List.of(new GroupProject()));

        assertThatThrownBy(() -> harness.castFlashbackWithTapCost(player1, 0,
                List.of(c1.getId(), c2.getId(), c3.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(List.of(c1, c2, c3)).noneMatch(Permanent::isTapped);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @CardUsed({ThaliaGuardianOfThraben.class})
    void flashbackPaysManaForCostIncrease() {
        Permanent c1 = addCreatureReady(player1, new GeometersArthropod());
        Permanent c2 = addCreatureReady(player1, new GeometersArthropod());
        Permanent c3 = addCreatureReady(player1, new GeometersArthropod());
        harness.addToBattlefield(player2, new ThaliaGuardianOfThraben());
        harness.setGraveyard(player1, List.of(new GroupProject()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashbackWithTapCost(player1, 0, List.of(c1.getId(), c2.getId(), c3.getId()));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }
}
