package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.IntrepidRabbit;
import com.github.laxika.magicalvibes.cards.p.PatchworkBanner;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FeatherOfFlight.class, PatchworkBanner.class, IntrepidRabbit.class})
class FeatherOfFlightTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Feather of Flight attaches it and draws a card")
    void resolvingAttachesAndDraws() {
        Permanent rabbit = harness.addToBattlefieldAndReturn(player1, new IntrepidRabbit());
        harness.setHand(player1, List.of(new FeatherOfFlight()));
        harness.setLibrary(player1, List.of(new IntrepidRabbit()));
        addFeatherMana();

        harness.castEnchantment(player1, 0, rabbit.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof FeatherOfFlight
                        && rabbit.getId().equals(permanent.getAttachedTo()));
        harness.assertInHand(player1, "Intrepid Rabbit");
    }

    @Test
    @DisplayName("Enchanted creature gets +1/+0 and has flying")
    void enchantedCreatureBoostedAndFlying() {
        Permanent rabbit = harness.addToBattlefieldAndReturn(player1, new IntrepidRabbit());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FeatherOfFlight());
        aura.setAttachedTo(rabbit.getId());

        assertThat(gqs.getEffectivePower(gd, rabbit)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rabbit)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, rabbit, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Removing Feather of Flight removes its boost and flying")
    void effectsStopWhenRemoved() {
        Permanent rabbit = harness.addToBattlefieldAndReturn(player1, new IntrepidRabbit());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FeatherOfFlight());
        aura.setAttachedTo(rabbit.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, rabbit)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, rabbit)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, rabbit, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Feather of Flight fizzles if its target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent rabbit = harness.addToBattlefieldAndReturn(player1, new IntrepidRabbit());
        harness.setHand(player1, List.of(new FeatherOfFlight()));
        addFeatherMana();

        harness.castEnchantment(player1, 0, rabbit.getId());
        gd.playerBattlefields.get(player1.getId()).remove(rabbit);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Feather of Flight");
        harness.assertNotOnBattlefield(player1, "Feather of Flight");
    }

    @Test
    @DisplayName("Feather of Flight cannot enchant a noncreature permanent")
    void cannotEnchantNoncreature() {
        harness.addToBattlefield(player1, new PatchworkBanner());
        harness.setHand(player1, List.of(new FeatherOfFlight()));
        addFeatherMana();

        var artifactId = harness.getPermanentId(player1, "Patchwork Banner");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifactId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Flash allows enchanting an opponent's creature during their upkeep")
    void flashOnOpponentsTurn() {
        Permanent rabbit = harness.addToBattlefieldAndReturn(player2, new IntrepidRabbit());
        harness.setHand(player1, List.of(new FeatherOfFlight()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new IntrepidRabbit()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        addFeatherMana();

        harness.castEnchantment(player1, 0, rabbit.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, rabbit)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rabbit)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, rabbit, Keyword.FLYING)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        harness.assertInHand(player1, "Intrepid Rabbit");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The enter trigger still draws after the Aura leaves the battlefield")
    void drawTriggerSurvivesAuraLeaving() {
        Permanent rabbit = harness.addToBattlefieldAndReturn(player1, new IntrepidRabbit());
        harness.setHand(player1, List.of(new FeatherOfFlight()));
        harness.setLibrary(player1, List.of(new IntrepidRabbit()));
        addFeatherMana();

        harness.castEnchantment(player1, 0, rabbit.getId());
        harness.passBothPriorities();
        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof FeatherOfFlight)
                .findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());

        harness.passBothPriorities();

        harness.assertInHand(player1, "Intrepid Rabbit");
        assertThat(gqs.getEffectivePower(gd, rabbit)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, rabbit, Keyword.FLYING)).isFalse();
    }

    private void addFeatherMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
