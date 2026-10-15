package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({MysticVeil.class, Boomerang.class, FountainOfYouth.class, GrizzlyBears.class})
class MysticVeilTest extends BaseCardTest {

    private Permanent attachVeil(Permanent host) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MysticVeil());
        aura.setAttachedTo(host.getId());
        return aura;
    }

    @Test
    @DisplayName("Enchanted creature has shroud")
    void enchantedCreatureHasShroud() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachVeil(bears);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Shroud is lost when Mystic Veil leaves the battlefield")
    void shroudLostWhenRemoved() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = attachVeil(bears);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.SHROUD)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Enchanted creature cannot be targeted (shroud)")
    void enchantedCreatureCannotBeTargeted() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachVeil(bears);

        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Cast at sorcery speed, it stays on the battlefield through cleanup")
    void castAtSorcerySpeedSurvivesCleanup() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MysticVeil()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        harness.assertOnBattlefield(player1, "Mystic Veil");
    }

    @Test
    @DisplayName("Cast when a sorcery couldn't be cast, its controller sacrifices it at cleanup")
    void castAtInstantSpeedIsSacrificedAtCleanup() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MysticVeil()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mystic Veil");

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);
        harness.assertOnBattlefield(player1, "Mystic Veil");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mystic Veil");
        harness.assertInGraveyard(player1, "Mystic Veil");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new MysticVeil()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Casting in response grants shroud before the opposing spell resolves")
    void castInResponseMakesOpposingTargetIllegal() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, bears.getId());
        harness.setHand(player1, List.of(new MysticVeil()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.SHROUD)).isTrue();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Boomerang");

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Mystic Veil");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Can enchant an opponent's creature without granting shroud to other creatures")
    void canEnchantOpposingCreature() {
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MysticVeil()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, opposingBears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mystic Veil");
        assertThat(gqs.hasKeyword(gd, opposingBears, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownBears, Keyword.SHROUD)).isFalse();
        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, opposingBears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }
}
