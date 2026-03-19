package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArvadTheCursedTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Arvad the Cursed has static boost effect for legendary creatures")
    void hasCorrectEffects() {
        ArvadTheCursed card = new ArvadTheCursed();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(StaticBoostEffect.class);
        StaticBoostEffect boost = (StaticBoostEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(boost.powerBoost()).isEqualTo(2);
        assertThat(boost.toughnessBoost()).isEqualTo(2);
        assertThat(boost.scope()).isEqualTo(GrantScope.OWN_CREATURES);
        assertThat(boost.filter()).isInstanceOf(PermanentHasSupertypePredicate.class);
        PermanentHasSupertypePredicate filter = (PermanentHasSupertypePredicate) boost.filter();
        assertThat(filter.supertype()).isEqualTo(CardSupertype.LEGENDARY);
    }

    // ===== Static effect: buffs other legendary creatures you control =====

    @Test
    @DisplayName("Other legendary creatures you control get +2/+2")
    void buffsOtherLegendaryCreatures() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.addToBattlefield(player1, new AdelizTheCinderWind());

        Permanent adeliz = findPermanent(player1, "Adeliz, the Cinder Wind");
        // Adeliz is 2/2 base + 2/2 from Arvad = 4/4
        assertThat(gqs.getEffectivePower(gd, adeliz)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, adeliz)).isEqualTo(4);
    }

    @Test
    @DisplayName("Arvad the Cursed does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new ArvadTheCursed());

        Permanent arvad = findPermanent(player1, "Arvad the Cursed");
        // 3/3 base, no self-buff
        assertThat(gqs.getEffectivePower(gd, arvad)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, arvad)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff non-legendary creatures")
    void doesNotBuffNonLegendaryCreatures() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff opponent's legendary creatures")
    void doesNotBuffOpponentLegendaryCreatures() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.addToBattlefield(player2, new AdelizTheCinderWind());

        Permanent opponentAdeliz = findPermanent(player2, "Adeliz, the Cinder Wind");
        // Adeliz is 2/2 base, no buff from opponent's Arvad
        assertThat(gqs.getEffectivePower(gd, opponentAdeliz)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentAdeliz)).isEqualTo(2);
    }

    // ===== Bonus removed when Arvad leaves =====

    @Test
    @DisplayName("Bonus is removed when Arvad the Cursed leaves the battlefield")
    void bonusRemovedWhenArvadLeaves() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.addToBattlefield(player1, new AdelizTheCinderWind());

        Permanent adeliz = findPermanent(player1, "Adeliz, the Cinder Wind");
        assertThat(gqs.getEffectivePower(gd, adeliz)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Arvad the Cursed"));

        assertThat(gqs.getEffectivePower(gd, adeliz)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, adeliz)).isEqualTo(2);
    }

    // ===== Two Arvads stack =====

    @Test
    @DisplayName("Two Arvads buff each other and stack bonuses on legendary creatures")
    void twoArvadsStackBonuses() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.addToBattlefield(player1, new AdelizTheCinderWind());

        Permanent adeliz = findPermanent(player1, "Adeliz, the Cinder Wind");
        // 2/2 base + 2/2 from each Arvad = 6/6
        assertThat(gqs.getEffectivePower(gd, adeliz)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, adeliz)).isEqualTo(6);

        // Each Arvad buffs the other (both are Legendary)
        List<Permanent> arvads = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Arvad the Cursed"))
                .toList();
        assertThat(arvads).hasSize(2);
        for (Permanent arvad : arvads) {
            // 3/3 base + 2/2 from the other Arvad = 5/5
            assertThat(gqs.getEffectivePower(gd, arvad)).isEqualTo(5);
            assertThat(gqs.getEffectiveToughness(gd, arvad)).isEqualTo(5);
        }
    }

    // ===== Helpers =====

    private Permanent findPermanent(com.github.laxika.magicalvibes.model.Player player, String cardName) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(cardName))
                .findFirst().orElseThrow();
    }
}
