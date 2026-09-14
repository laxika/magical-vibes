package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(HedronArchive.class)
class HedronArchiveTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Hedron Archive produces two colorless mana")
    void tappingProducesTwoColorlessMana() {
        Permanent archive = addReadyArchive();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(archive.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying two mana and sacrificing Hedron Archive draws two cards")
    void sacrificingDrawsTwoCards() {
        Permanent archive = addReadyArchive();
        HedronArchive drawnCardOne = new HedronArchive();
        HedronArchive drawnCardTwo = new HedronArchive();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCardOne, drawnCardTwo));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(archive);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(archive.getCard());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCardOne, drawnCardTwo);
    }

    @Test
    @DisplayName("Hedron Archive cannot be sacrificed without paying its mana cost")
    void sacrificeAbilityRequiresTwoMana() {
        Permanent archive = addReadyArchive();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(archive);
        assertThat(archive.isTapped()).isFalse();
        harness.assertNotInGraveyard(player1, "Hedron Archive");
    }

    private Permanent addReadyArchive() {
        Permanent archive = harness.addToBattlefieldAndReturn(player1, new HedronArchive());
        archive.setSummoningSick(false);
        return archive;
    }
}
