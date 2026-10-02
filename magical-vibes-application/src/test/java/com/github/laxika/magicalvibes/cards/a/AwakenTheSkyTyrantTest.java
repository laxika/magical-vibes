package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AwakenTheSkyTyrant.class, LightningBolt.class, Disenchant.class})
class AwakenTheSkyTyrantTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent damage sacrifices the enchantment and creates a Dragon token")
    void opponentDamageCreatesDragonToken() {
        var tyrant = harness.addToBattlefieldAndReturn(player1, new AwakenTheSkyTyrant());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(tyrant);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }

    @Test
    @DisplayName("Damage from your own source does not trigger the enchantment")
    void ownDamageDoesNotTrigger() {
        var tyrant = harness.addToBattlefieldAndReturn(player1, new AwakenTheSkyTyrant());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(tyrant);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .isEmpty();
    }

    @Test
    @DisplayName("Removing the enchantment before its trigger resolves creates no token")
    void removedSourceCreatesNoToken() {
        var tyrant = harness.addToBattlefieldAndReturn(player1, new AwakenTheSkyTyrant());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        assertThat(gd.stack).hasSize(1);
        harness.castAndResolveInstant(player1, 0, tyrant.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(tyrant);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).isEmpty();
    }

    @Test
    @DisplayName("Two pending damage triggers can only sacrifice the enchantment once")
    void repeatedDamageCreatesOnlyOneDragon() {
        var tyrant = harness.addToBattlefieldAndReturn(player1, new AwakenTheSkyTyrant());
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        assertThat(gd.stack).hasSize(1);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(tyrant);
        harness.assertInGraveyard(player1, "Awaken the Sky Tyrant");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).singleElement()
                .satisfies(dragon -> {
                    assertThat(dragon.getCard().getPower()).isEqualTo(5);
                    assertThat(dragon.getCard().getToughness()).isEqualTo(5);
                    assertThat(dragon.getCard().getColor()).isEqualTo(CardColor.RED);
                    assertThat(dragon.getCard().getSubtypes()).contains(CardSubtype.DRAGON);
                    assertThat(dragon.getCard().getKeywords()).contains(Keyword.FLYING);
                });
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).isEmpty();
    }
}
