package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.d.DarkthicketWolf;
import com.github.laxika.magicalvibes.cards.o.OrchardSpirit;
import com.github.laxika.magicalvibes.cards.i.IntangibleVirtue;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.r.RangersGuile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UrgentExorcism.class, OrchardSpirit.class, IntangibleVirtue.class,
        DarkthicketWolf.class, RangersGuile.class})
class UrgentExorcismTest extends BaseCardTest {

    

    @Test
    @DisplayName("Casting Urgent Exorcism puts it on stack with target")
    void castingPutsOnStack() {
        harness.addToBattlefield(player2, new OrchardSpirit());
        harness.setHand(player1, List.of(new UrgentExorcism()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Orchard Spirit");
        harness.castInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard()).isInstanceOf(UrgentExorcism.class);
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving destroys target Spirit")
    void resolvesDestroySpirit() {
        harness.addToBattlefield(player2, new OrchardSpirit());
        harness.setHand(player1, List.of(new UrgentExorcism()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Orchard Spirit");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Orchard Spirit");
        harness.assertInGraveyard(player2, "Orchard Spirit");
    }

    @Test
    @DisplayName("Resolving destroys target enchantment")
    void resolvesDestroyEnchantment() {
        harness.addToBattlefield(player2, new IntangibleVirtue());
        harness.setHand(player1, List.of(new UrgentExorcism()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Intangible Virtue");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Intangible Virtue");
        harness.assertInGraveyard(player2, "Intangible Virtue");
    }

    @Test
    @DisplayName("Cannot target a non-Spirit creature")
    void cannotTargetNonSpiritCreature() {
        harness.addToBattlefield(player2, new DarkthicketWolf());
        harness.setHand(player1, List.of(new UrgentExorcism()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID creatureId = harness.getPermanentId(player2, "Darkthicket Wolf");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can destroy a Spirit controlled by the caster")
    void destroysOwnSpirit() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new OrchardSpirit()).getId();
        harness.setHand(player1, List.of(new UrgentExorcism()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Orchard Spirit");
        harness.assertInGraveyard(player1, "Orchard Spirit");
        harness.assertInGraveyard(player1, "Urgent Exorcism");
    }

    @Test
    @DisplayName("Does not destroy a Spirit that gains hexproof in response")
    void targetGainingHexproofSurvives() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new OrchardSpirit()).getId();
        harness.setHand(player1, List.of(new UrgentExorcism()));
        harness.setHand(player2, List.of(new RangersGuile()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, targetId);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Orchard Spirit");
        harness.assertInGraveyard(player1, "Urgent Exorcism");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Fizzles if target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player2, new OrchardSpirit());
        harness.setHand(player1, List.of(new UrgentExorcism()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Orchard Spirit");
        harness.castInstant(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Urgent Exorcism");
    }
}
