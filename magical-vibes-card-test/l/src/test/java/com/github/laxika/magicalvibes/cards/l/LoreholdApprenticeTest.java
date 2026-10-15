package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OrchardSpirit;
import com.github.laxika.magicalvibes.cards.t.ThrillingDiscovery;
import com.github.laxika.magicalvibes.cards.x.Xenograft;
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

@CardUsed({LoreholdApprentice.class, OrchardSpirit.class, GrizzlyBears.class,
        GiantGrowth.class, BarkshellBlessing.class, ThrillingDiscovery.class, Xenograft.class})
class LoreholdApprenticeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant grants Spirits the tap damage ability")
    void castingInstantGrantsSpiritAbility() {
        addCreatureReady(player1, new LoreholdApprentice());
        Permanent spirit = addCreatureReady(player1, new OrchardSpirit());
        Permanent nonSpirit = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, spirit.getId());

        int lifeBefore = gd.getLife(player2.getId());
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(spirit), null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(nonSpirit), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Copying an instant creates a separate Magecraft grant")
    void copyingInstantGrantsAnotherAbility() {
        addCreatureReady(player1, new LoreholdApprentice());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent spirit = addCreatureReady(player1, new OrchardSpirit());
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithConspire(player1, 0, spirit.getId(), List.of(
                gd.playerBattlefields.get(player1.getId()).get(1).getId(),
                gd.playerBattlefields.get(player1.getId()).get(2).getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        for (int i = 0; i < 6 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }

        assertThat(spirit.getTemporaryActivatedAbilities()).hasSize(2);
    }

    @Test
    @DisplayName("The granted tap ability wears off at end of turn")
    void grantedAbilityWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new LoreholdApprentice());
        Permanent spirit = addCreatureReady(player1, new OrchardSpirit());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, spirit.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(spirit), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Lorehold Apprentice gains its own ability when it is a Spirit")
    void apprenticeThatIsASpiritGainsAbility() {
        Permanent apprentice = addCreatureReady(player1, new LoreholdApprentice());
        harness.setHand(player1, List.of(new Xenograft(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "SPIRIT");

        harness.castAndResolveInstant(player1, 0, apprentice.getId());
        int lifeBefore = gd.getLife(player2.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Casting a sorcery grants the ability only to your existing Spirits")
    void castingSorceryGrantsOnlyExistingFriendlySpirits() {
        addCreatureReady(player1, new LoreholdApprentice());
        Permanent spirit = addCreatureReady(player1, new OrchardSpirit());
        Permanent opposingSpirit = addCreatureReady(player2, new OrchardSpirit());
        harness.setHand(player1, List.of(new ThrillingDiscovery()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        Permanent laterSpirit = addCreatureReady(player1, new OrchardSpirit());
        int lifeBefore = gd.getLife(player2.getId());
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
        assertThat(spirit.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(laterSpirit), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
        assertThatThrownBy(() -> harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(opposingSpirit), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }
}
