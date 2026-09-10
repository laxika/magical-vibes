package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CinderCrawler;
import com.github.laxika.magicalvibes.cards.s.SabertoothWyvern;
import com.github.laxika.magicalvibes.cards.t.TyphoidRats;
import com.github.laxika.magicalvibes.cards.w.WelkinHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DizzyingGaze.class, CinderCrawler.class, SabertoothWyvern.class, TyphoidRats.class, WelkinHawk.class})
class DizzyingGazeTest extends BaseCardTest {

    private Permanent addAuraToCreature(Card creatureCard) {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, creatureCard);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DizzyingGaze());
        aura.setAttachedTo(creature.getId());
        return aura;
    }

    @Test
    void enchantedCreatureDealsDamageToFlyingCreature() {
        addAuraToCreature(new TyphoidRats());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WelkinHawk());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Welkin Hawk");
    }

    @Test
    void cannotTargetCreatureWithoutFlying() {
        addAuraToCreature(new CinderCrawler());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CinderCrawler());

        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("required predicate");
    }

    @Test
    void canEnchantOnlyCreatureYouControl() {
        harness.addToBattlefield(player1, new CinderCrawler());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new CinderCrawler());
        harness.setHand(player1, List.of(new DizzyingGaze()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    void enchantsCreatureYouControl() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CinderCrawler());
        harness.setHand(player1, List.of(new DizzyingGaze()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Dizzying Gaze");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void dealsOnlyOneDamageToFlyingCreature() {
        addAuraToCreature(new CinderCrawler());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SabertoothWyvern());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Sabertooth Wyvern");
        harness.assertNotInGraveyard(player2, "Sabertooth Wyvern");
    }

    @Test
    void doesNothingIfAuraIsUnattachedWhenAbilityResolves() {
        Permanent aura = addAuraToCreature(new CinderCrawler());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WelkinHawk());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 1, null, target.getId());
        aura.setAttachedTo(null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Welkin Hawk");
        harness.assertNotInGraveyard(player2, "Welkin Hawk");
    }
}
