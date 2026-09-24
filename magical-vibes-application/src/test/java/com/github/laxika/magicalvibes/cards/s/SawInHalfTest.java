package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SawInHalf.class, GrizzlyBears.class, Forest.class})
class SawInHalfTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature and gives its controller two copies with rounded-up half stats")
    void destroysCreatureAndCreatesTwoHalfSizedCopiesForItsController() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setPowerModifier(1);
        target.setToughnessModifier(3);
        castSawInHalf(target);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(findPermanents(player2, "Grizzly Bears"))
                .hasSize(2)
                .allSatisfy(copy -> {
                    assertThat(copy.getCard().isToken()).isTrue();
                    assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(2);
                    assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(3);
                });
    }

    @Test
    @DisplayName("Does not create copies when the creature cannot be destroyed")
    void doesNotCreateCopiesForIndestructibleCreature() {
        Card indestructibleBears = new GrizzlyBears();
        indestructibleBears.setKeywords(Set.of(Keyword.INDESTRUCTIBLE));
        Permanent target = addCreatureReady(player2, indestructibleBears);
        castSawInHalf(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(findPermanents(player2, "Grizzly Bears")).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new SawInHalf()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, harness.getPermanentId(player2, "Forest")))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castSawInHalf(Permanent target) {
        harness.setHand(player1, List.of(new SawInHalf()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
