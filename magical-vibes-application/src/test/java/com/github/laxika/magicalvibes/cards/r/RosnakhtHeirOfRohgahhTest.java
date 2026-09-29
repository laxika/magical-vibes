package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RosnakhtHeirOfRohgahh.class, GiantGrowth.class, GrizzlyBears.class})
class RosnakhtHeirOfRohgahhTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell that targets Rosnakht creates a Kobold token")
    void targetingRosnakhtCreatesKoboldToken() {
        Permanent rosnakht = addCreatureReady(player1, new RosnakhtHeirOfRohgahh());

        castGiantGrowth(player1, rosnakht);

        Permanent token = findPermanent(player1, "Kobolds of Kher Keep");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getEffectivePower()).isZero();
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("A spell targeting another creature does not trigger Rosnakht")
    void targetingAnotherCreatureDoesNotCreateToken() {
        addCreatureReady(player1, new RosnakhtHeirOfRohgahh());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        castGiantGrowth(player1, bears);

        assertThat(findPermanents(player1, "Kobolds of Kher Keep")).isEmpty();
    }

    @Test
    @DisplayName("An opponent's spell targeting Rosnakht does not trigger it")
    void opponentsSpellDoesNotCreateToken() {
        Permanent rosnakht = addCreatureReady(player1, new RosnakhtHeirOfRohgahh());

        castGiantGrowth(player2, rosnakht);

        assertThat(findPermanents(player1, "Kobolds of Kher Keep")).isEmpty();
    }

    @Test
    @DisplayName("Battle cry boosts other attacking creatures")
    void battleCryBoostsOtherAttackingCreatures() {
        addCreatureReady(player1, new RosnakhtHeirOfRohgahh());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    private void castGiantGrowth(Player player, Permanent target) {
        harness.setHand(player, List.of(new GiantGrowth()));
        harness.addMana(player, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 1);
        harness.forceActivePlayer(player);
        harness.castInstant(player, 0, target.getId());
        resolveAllTriggers();
    }
}
