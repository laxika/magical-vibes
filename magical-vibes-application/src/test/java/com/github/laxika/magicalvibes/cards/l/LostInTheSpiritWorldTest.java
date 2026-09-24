package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InnocenceKami;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LostInTheSpiritWorld.class, GrizzlyBears.class, InnocenceKami.class, Island.class})
class LostInTheSpiritWorldTest extends BaseCardTest {

    @Test
    void returnsTargetCreatureAndCreatesAColorlessSpirit() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(target.getId());

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Spirit"))
                .singleElement()
                .satisfies(token -> {
                    assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
                    assertThat(token.getCard().getColor()).isNull();
                    assertThat(token.getCard().getPower()).isEqualTo(1);
                    assertThat(token.getCard().getToughness()).isEqualTo(1);
                    assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
                });
    }

    @Test
    void createsSpiritWhenNoCreatureIsTargeted() {
        cast(null);

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    @Test
    void spiritCanBlockAndBeBlockedOnlyBySpiritCreatures() {
        cast(null);
        Permanent token = findPermanents(player1, "Spirit").getFirst();
        Permanent nonSpirit = addCreatureReady(player2, new GrizzlyBears());
        Permanent spirit = addCreatureReady(player2, new InnocenceKami());

        assertThat(bls.canBlockAttacker(gd, nonSpirit, token,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, spirit, token,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
        assertThat(bls.canBlockAttacker(gd, token, nonSpirit,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, token, spirit,
                gd.playerBattlefields.get(player1.getId()))).isTrue();
    }

    @Test
    void cannotTargetNonCreaturePermanents() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        prepareSpell();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private void cast(UUID targetId) {
        prepareSpell();
        if (targetId == null) {
            harness.castAndResolveSorcery(player1, 0, List.of());
        } else {
            harness.castAndResolveSorcery(player1, 0, 0, targetId);
        }
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new LostInTheSpiritWorld()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
