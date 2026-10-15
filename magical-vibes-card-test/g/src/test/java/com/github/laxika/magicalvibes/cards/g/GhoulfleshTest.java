package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Ghoulflesh.class, GrizzlyBears.class, FountainOfYouth.class})
class GhoulfleshTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Ghoulflesh attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bearsPerm.setSummoningSick(false);

        harness.setHand(player1, List.of(new Ghoulflesh()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Ghoulflesh")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    @Test
    @DisplayName("Enchanted creature gets -1/-1")
    void shrinksEnchantedCreature() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Ghoulflesh());
        aura.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(1);
    }

    @Test
    @DisplayName("Enchanted creature is black in addition to its other colors")
    void grantsBlackColor() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Ghoulflesh());
        aura.setAttachedTo(bearsPerm.getId());

        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, bearsPerm);
        assertThat(bonus.grantedColors()).contains(CardColor.BLACK);
    }

    @Test
    @DisplayName("Enchanted creature is a Zombie in addition to its other types")
    void grantsZombieSubtype() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Ghoulflesh());
        aura.setAttachedTo(bearsPerm.getId());

        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, bearsPerm);
        assertThat(bonus.grantedSubtypes()).contains(CardSubtype.ZOMBIE);
    }

    @Test
    @DisplayName("Removing Ghoulflesh restores the creature's original P/T")
    void removalRestoresOriginalState() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Ghoulflesh());
        aura.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.computeStaticBonus(gd, bearsPerm).grantedSubtypes())
                .doesNotContain(CardSubtype.ZOMBIE);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Ghoulflesh")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Ghoulflesh()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        Permanent artifactPerm = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifactPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
    @Test
    @DisplayName("Resolved Ghoulflesh preserves original colors and creature types and affects only its target")
    void preservesOriginalCharacteristicsAndOnlyAffectsTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Ghoulflesh()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.BLACK);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactlyInAnyOrder(CardSubtype.BEAR, CardSubtype.ZOMBIE);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, other)).containsExactly(CardColor.GREEN);
        assertThat(gqs.effectiveCreatureSubtypes(gd, other)).containsExactly(CardSubtype.BEAR);
    }

    @Test
    @DisplayName("Two Ghoulflesh auras stack and send a zero-toughness creature and both auras to their owners' graveyards")
    void multipleAurasStackAndKillCreatureAtZeroToughness() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Ghoulflesh(), new Ghoulflesh()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Ghoulflesh");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof Ghoulflesh)
                .hasSize(2);
    }
}
