package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TwistedEmbrace.class, ChandraNalaar.class, FountainOfYouth.class, GrizzlyBears.class})
class TwistedEmbraceTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts the enchanted creature and destroys an opposing creature")
    void boostsEnchantedCreatureAndDestroysOpposingCreature() {
        Permanent enchantedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castTwistedEmbrace(enchantedCreature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, enchantedCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enchantedCreature)).isEqualTo(3);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The ETB trigger destroys an opposing planeswalker")
    void destroysOpposingPlaneswalker() {
        Permanent enchantedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);

        castTwistedEmbrace(enchantedCreature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Chandra Nalaar");
        harness.assertInGraveyard(player2, "Chandra Nalaar");
        assertThat(gqs.getEffectivePower(gd, enchantedCreature)).isEqualTo(3);
    }

    @Test
    @DisplayName("The Aura can enchant an artifact you control")
    void canEnchantArtifactYouControl() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castTwistedEmbrace(artifact.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The Aura cannot enchant an artifact or creature controlled by an opponent")
    void cannotEnchantOpponentPermanent() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TwistedEmbrace()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature you control");
    }

    @Test
    @DisplayName("The ETB trigger cannot target a permanent controlled by the Aura's controller")
    void rejectsOwnEtbTarget() {
        Permanent enchantedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castTwistedEmbrace(enchantedCreature.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownTarget.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    private void castTwistedEmbrace(java.util.UUID enchantTargetId) {
        harness.setHand(player1, List.of(new TwistedEmbrace()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, enchantTargetId);
    }
}
