package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GriffinGuide.class, DoomBlade.class, GrizzlyBears.class})
class GriffinGuideTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+2 and flying")
    void grantsBoostAndFlying() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GriffinGuide());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Creates a 2/2 white Griffin with flying when the enchanted creature dies")
    void createsGriffinWhenEnchantedCreatureDies() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GriffinGuide());
        aura.setAttachedTo(bears.getId());

        destroyCreature(bears);

        List<Permanent> griffins = findPermanents(player1, "Griffin");
        assertThat(griffins).hasSize(1);
        Permanent griffin = griffins.getFirst();
        assertThat(griffin.getCard().getPower()).isEqualTo(2);
        assertThat(griffin.getCard().getToughness()).isEqualTo(2);
        assertThat(griffin.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(griffin.getCard().getSubtypes()).contains(CardSubtype.GRIFFIN);
        assertThat(griffin.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(griffin.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Does not create a Griffin when a different creature dies")
    void doesNotTriggerForDifferentCreature() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GriffinGuide());
        aura.setAttachedTo(enchanted.getId());

        destroyCreature(other);

        assertThat(findPermanents(player1, "Griffin")).isEmpty();
        assertThat(findPermanents(player1, "Grizzly Bears")).contains(enchanted);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new GriffinGuide()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void destroyCreature(Permanent creature) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
