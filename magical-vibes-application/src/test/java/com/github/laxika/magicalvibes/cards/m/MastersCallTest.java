package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MastersCall.class})
class MastersCallTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving creates two 1/1 colorless Myr artifact creature tokens")
    void resolvingCreatesTwoMyrTokens() {
        harness.setHand(player1, List.of(new MastersCall()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        List<Permanent> myrs = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Myr"))
                .toList();
        assertThat(myrs).hasSize(2);

        for (Permanent myr : myrs) {
            assertThat(myr.getCard().getPower()).isEqualTo(1);
            assertThat(myr.getCard().getToughness()).isEqualTo(1);
            assertThat(myr.getCard().getColor()).isNull();
            assertThat(myr.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(myr.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
            assertThat(myr.getCard().getSubtypes()).contains(CardSubtype.MYR);
        }
    }

    @Test
    @DisplayName("Spell goes to graveyard after resolution")
    void spellGoesToGraveyardAfterResolution() {
        harness.setHand(player1, List.of(new MastersCall()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Master's Call");
    }

    @Test
    @DisplayName("Tokens are created only on resolution under the nonactive caster's control")
    void nonactiveCasterControlsTokensCreatedOnResolution() {
        harness.setHand(player2, List.of(new MastersCall()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.passPriority(player1);

        harness.castInstant(player2, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2).allSatisfy(myr -> {
            assertThat(myr.getCard().isToken()).isTrue();
            assertThat(myr.isTapped()).isFalse();
            assertThat(myr.isAttacking()).isFalse();
        });
        harness.assertInGraveyard(player2, "Master's Call");
    }
}
