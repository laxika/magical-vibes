package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.CardPowerToughnessModifier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TerritorialStrike.class, GrizzlyBears.class, Plains.class, ShivanDragon.class})
class TerritorialStrikeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a nonland permanent without beholding a Dragon")
    void destroysNonlandPermanentWithoutBehold() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TerritorialStrike()));
        addMana();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(card -> card.getId())
                .contains(target.getCard().getId());
        assertThat(gd.perpetualCardPowerToughnessModifiers).doesNotContainKey(target.getCard().getId());
    }

    @Test
    @DisplayName("A beheld Dragon gives the destroyed permanent a perpetual +2/+2")
    void beholdingDragonPerpetuallyBoostsDestroyedPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TerritorialStrike(), new ShivanDragon()));
        addMana();

        harness.castInstantWithBehold(player1, 0, target.getId(), List.of(), List.of(1));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.perpetualCardPowerToughnessModifiers)
                .containsEntry(target.getCard().getId(), new CardPowerToughnessModifier(2, 2));
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new TerritorialStrike()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
