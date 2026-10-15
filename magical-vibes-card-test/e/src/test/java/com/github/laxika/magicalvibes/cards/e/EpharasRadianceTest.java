package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EpharasRadiance.class, GrizzlyBears.class, FountainOfYouth.class})
class EpharasRadianceTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature can tap to gain 3 life for {1}{W}")
    void enchantedCreatureGainsLife() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setSummoningSick(false);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new EpharasRadiance());
        aura.setAttachedTo(creature.getId());

        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature cannot use the ability while summoning sick")
    void summoningSickCreatureCannotActivate() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new EpharasRadiance());
        aura.setAttachedTo(creature.getId());

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNoncreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new EpharasRadiance()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Casting the Aura on an opposing creature grants life to that creature's controller")
    void opposingCreatureControllerGainsLife() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setSummoningSick(false);
        harness.setHand(player1, List.of(new EpharasRadiance()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Ephara's Radiance");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, null, null);

        harness.assertLife(player2, 10);
        assertThat(creature.isTapped()).isTrue();
        assertThat(aura.isTapped()).isFalse();

        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 13);
    }

    @Test
    @DisplayName("An activated ability still resolves after the Aura leaves the battlefield")
    void activatedAbilitySurvivesAuraLeaving() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setSummoningSick(false);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new EpharasRadiance());
        aura.setAttachedTo(creature.getId());
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        creature.untap();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 13);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The granted ability requires both generic and white mana")
    void insufficientManaCannotActivate() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setSummoningSick(false);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new EpharasRadiance());
        aura.setAttachedTo(creature.getId());
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(creature.isTapped()).isFalse();
        harness.assertLife(player1, 10);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An already tapped creature cannot pay the tap cost")
    void tappedCreatureCannotActivate() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setSummoningSick(false);
        creature.tap();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new EpharasRadiance());
        aura.setAttachedTo(creature.getId());
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        harness.assertLife(player1, 10);
        assertThat(gd.stack).isEmpty();
    }
}
