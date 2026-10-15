package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
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

    @Test
    @DisplayName("Heroic creates its token before the targeting spell resolves")
    void heroicResolvesBeforeTargetingSpell() {
        Permanent rosnakht = addCreatureReady(player1, new RosnakhtHeirOfRohgahh());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, rosnakht.getId());

        assertThat(findPermanents(player1, "Kobolds of Kher Keep")).isEmpty();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Kobolds of Kher Keep")).hasSize(1);
        assertThat(rosnakht.getEffectivePower()).isZero();
        resolveAllTriggers();
        assertThat(rosnakht.getEffectivePower()).isEqualTo(3);
        assertThat(findPermanents(player1, "Kobolds of Kher Keep")).hasSize(1);
    }

    @Test
    @DisplayName("Each targeting spell creates a separate Kobold")
    void eachTargetingSpellCreatesToken() {
        Permanent rosnakht = addCreatureReady(player1, new RosnakhtHeirOfRohgahh());

        castGiantGrowth(player1, rosnakht);
        castGiantGrowth(player1, rosnakht);

        assertThat(findPermanents(player1, "Kobolds of Kher Keep")).hasSize(2);
    }

    @Test
    @DisplayName("Battle cry excludes Rosnakht and creatures that are not attacking")
    void battleCryExcludesSourceAndNonattackers() {
        Permanent rosnakht = addCreatureReady(player1, new RosnakhtHeirOfRohgahh());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonattacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(rosnakht.getEffectivePower()).isZero();
        assertThat(rosnakht.getEffectiveToughness()).isEqualTo(1);
        assertThat(attacker.getEffectivePower()).isEqualTo(3);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
        assertThat(nonattacker.getEffectivePower()).isEqualTo(2);
        assertThat(opponent.getEffectivePower()).isEqualTo(2);
    }
}
