package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RiverBoa;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpiderClimb.class, RiverBoa.class, SisaysRing.class})
class SpiderClimbTest extends BaseCardTest {

    private Permanent enchant(Permanent host) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SpiderClimb());
        aura.setAttachedTo(host.getId());
        return aura;
    }

    @Test
    @DisplayName("Enchanted creature gets +0/+3 and has reach")
    void enchantedCreatureGetsBoostAndReach() {
        Permanent riverBoa = addCreatureReady(player1, new RiverBoa());
        enchant(riverBoa);

        assertThat(gqs.getEffectivePower(gd, riverBoa)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, riverBoa)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, riverBoa, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Boost and reach are lost when Spider Climb leaves the battlefield")
    void effectsLostWhenRemoved() {
        Permanent riverBoa = addCreatureReady(player1, new RiverBoa());
        Permanent aura = enchant(riverBoa);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectiveToughness(gd, riverBoa)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, riverBoa, Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Cast at sorcery speed, it stays on the battlefield through cleanup")
    void castAtSorcerySpeedSurvivesCleanup() {
        Permanent riverBoa = addCreatureReady(player1, new RiverBoa());
        harness.setHand(player1, List.of(new SpiderClimb()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, riverBoa.getId());
        harness.passBothPriorities();

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        harness.assertOnBattlefield(player1, "Spider Climb");
    }

    @Test
    @DisplayName("Cast when a sorcery couldn't be cast, its controller sacrifices it at cleanup")
    void castAtInstantSpeedIsSacrificedAtCleanup() {
        Permanent riverBoa = addCreatureReady(player1, new RiverBoa());
        harness.setHand(player1, List.of(new SpiderClimb()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.castEnchantment(player1, 0, riverBoa.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spider Climb");

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        harness.assertNotOnBattlefield(player1, "Spider Climb");
        harness.assertInGraveyard(player1, "Spider Climb");
    }

    @Test
    @DisplayName("Cast from exile at instant speed is sacrificed at cleanup")
    void castFromExileAtInstantSpeedIsSacrificedAtCleanup() {
        Permanent riverBoa = addCreatureReady(player1, new RiverBoa());
        SpiderClimb spiderClimb = new SpiderClimb();
        gd.addToExile(player1.getId(), spiderClimb);
        gd.exilePlayPermissions.put(spiderClimb.getId(), player1.getId());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castFromExile(player1, spiderClimb.getId(), riverBoa.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spider Climb");

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        harness.assertNotOnBattlefield(player1, "Spider Climb");
        harness.assertInGraveyard(player1, "Spider Climb");
    }

    @Test
    @DisplayName("Cast during a main phase while another spell is on the stack, it is sacrificed at cleanup")
    void castWithAnotherSpellOnStackIsSacrificedAtCleanup() {
        Permanent riverBoa = addCreatureReady(player1, new RiverBoa());
        harness.setHand(player1, List.of(new SpiderClimb(), new SpiderClimb()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0, riverBoa.getId());
        harness.castEnchantment(player1, 0, riverBoa.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spider Climb");

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        assertThat(countPermanents(player1, "Spider Climb")).isEqualTo(1);
        harness.assertInGraveyard(player1, "Spider Climb");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SisaysRing());
        harness.setHand(player1, List.of(new SpiderClimb()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
